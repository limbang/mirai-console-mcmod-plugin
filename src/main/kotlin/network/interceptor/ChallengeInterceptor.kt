/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.Jsoup
import top.limbang.mcmod.network.McmodBlockedException
import top.limbang.mcmod.network.McmodCaptchaException
import java.util.Base64
import java.util.logging.Logger

/**
 * ### mcmod 反爬虫挑战拦截器
 *
 * mcmod 站点可能返回旧版 JS Cookie 挑战:
 * ```
 * <script>document.cookie = 'yxd_token=<token>'
 * window.location.href='<原路径>'</script>
 * ```
 * 也可能返回新版 Minecraft 物品计数验证码. 旧版挑战会自动重放请求,
 * 新版挑战则解析出题目和图片后交给消息层让用户作答.
 */
class ChallengeInterceptor : Interceptor {
    companion object {
        private const val LEGACY_CHALLENGE_BODY_MAX_BYTES = 4L * 1024L
        private const val CAPTCHA_BODY_MAX_BYTES = 1024L * 1024L
        private const val CAPTCHA_IMAGE_PREFIX = "data:image/png;base64,"
        private val TOKEN_REGEX = Regex("""yxd_token=([a-zA-Z0-9]+)""")
        private val LOGGER = Logger.getLogger(ChallengeInterceptor::class.java.name)

        internal fun parseCaptchaBody(body: String): CaptchaPayload? {
            val document = Jsoup.parse(body)
            if (document.selectFirst("input[name=cc_captcha_answer]") == null) return null

            val imageUrl = document.selectFirst("#captchaImage")?.attr("src").orEmpty()
            val question = document.selectFirst(".captcha-question")?.text().orEmpty()
            if (!imageUrl.startsWith(CAPTCHA_IMAGE_PREFIX) || question.isBlank()) return null

            val imageBytes = runCatching {
                Base64.getDecoder().decode(imageUrl.substring(CAPTCHA_IMAGE_PREFIX.length))
            }.getOrNull() ?: return null
            return CaptchaPayload(question, imageBytes)
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // 只对文本类响应做检测; 图片等二进制响应直接放行
        val contentType = response.body?.contentType()?.toString().orEmpty()
        if (!contentType.startsWith("text/")) return response

        // 只有 403 才需要读取完整 base64 验证码; 正常页面只检查旧版短挑战
        val peekBytes = if (response.code == 403) CAPTCHA_BODY_MAX_BYTES else LEGACY_CHALLENGE_BODY_MAX_BYTES
        val text = response.peekBody(peekBytes).string()
        if (response.code == 403) {
            throwCaptchaIfPresent(response, request.url.toString(), text)
            throwIfRateLimited(response, text)
        }

        val match = TOKEN_REGEX.find(text)
        if (match == null || !text.contains("window.location.href")) {
            return response
        }

        val token = match.groupValues[1]
        LOGGER.info("[Challenge] hit on ${request.url}, retrying with cookie")
        val existingCookie = request.header("Cookie")
        val newCookie = if (existingCookie.isNullOrBlank()) {
            "yxd_token=$token"
        } else {
            "$existingCookie; yxd_token=$token"
        }
        val newRequest = request.newBuilder()
            .header("Cookie", newCookie)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "zh-CN,zh;q=0.9")
            .header("Referer", request.url.toString())
            .build()
        response.close()
        val retryResponse = chain.proceed(newRequest)

        // 如果重放后仍然是挑战, 说明出口 IP 已被 mcmod 拉黑, 任何 cookie 都过不去
        val retryContentType = retryResponse.body?.contentType()?.toString().orEmpty()
        if (retryContentType.startsWith("text/")) {
            val retryPeekBytes = if (retryResponse.code == 403) {
                CAPTCHA_BODY_MAX_BYTES
            } else {
                LEGACY_CHALLENGE_BODY_MAX_BYTES
            }
            val retryText = retryResponse.peekBody(retryPeekBytes).string()
            if (retryResponse.code == 403) {
                throwCaptchaIfPresent(retryResponse, request.url.toString(), retryText)
                throwIfRateLimited(retryResponse, retryText)
            }
            if (TOKEN_REGEX.containsMatchIn(retryText) && retryText.contains("window.location.href")) {
                retryResponse.close()
                LOGGER.warning("[Challenge] retry still returns challenge for ${request.url}, IP likely blocked")
                throw McmodBlockedException()
            }
        }
        return retryResponse
    }

    private fun throwCaptchaIfPresent(response: Response, requestUrl: String, body: String) {
        val captcha = parseCaptchaBody(body) ?: return
        response.close()
        LOGGER.info("[Challenge] captcha required on $requestUrl")
        throw McmodCaptchaException(requestUrl, captcha.question, captcha.imageBytes)
    }

    private fun throwIfRateLimited(response: Response, body: String) {
        if (!Jsoup.parse(body).title().contains("访问间隔过短")) return
        response.close()
        throw McmodBlockedException("mcmod rejected requests sent too frequently")
    }
}

internal data class CaptchaPayload(val question: String, val imageBytes: ByteArray)
