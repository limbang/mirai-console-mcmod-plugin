/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.network.interceptor

import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import top.limbang.mcmod.network.McmodBlockedException
import top.limbang.mcmod.network.McmodCaptchaException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ChallengeInterceptorTest {
    private val captchaHtml = """
        <html><body>
          <form>
            <img id="captchaImage" src="data:image/png;base64,AQID" />
            <p class="captcha-question">图中有多少个<b>木炭 (Charcoal)</b>?</p>
            <input name="cc_captcha_answer" />
          </form>
        </body></html>
    """.trimIndent()

    @Test
    fun parsesCaptchaQuestionAndImage() {
        val captcha = requireNotNull(ChallengeInterceptor.parseCaptchaBody(captchaHtml))

        assertEquals("图中有多少个木炭 (Charcoal)?", captcha.question)
        assertContentEquals(byteArrayOf(1, 2, 3), captcha.imageBytes)
    }

    @Test
    fun ignoresOrdinaryHtml() {
        assertNull(ChallengeInterceptor.parseCaptchaBody("<html><title>search results</title></html>"))
    }

    @Test
    fun turnsCaptcha403IntoTypedException() {
        val client = OkHttpClient.Builder()
            .addInterceptor(ChallengeInterceptor())
            .addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(403)
                    .message("Forbidden")
                    .body(captchaHtml.toResponseBody("text/html; charset=UTF-8".toMediaType()))
                    .build()
            }
            .build()

        val exception = assertFailsWith<McmodCaptchaException> {
            client.newCall(Request.Builder().url("https://search.mcmod.cn/s?key=test").build()).execute()
        }

        assertEquals("图中有多少个木炭 (Charcoal)?", exception.question)
        assertContentEquals(byteArrayOf(1, 2, 3), exception.imageBytes)
    }

    @Test
    fun turnsRateLimitAfterLegacyChallengeIntoBlockedException() {
        var requestCount = 0
        val client = OkHttpClient.Builder()
            .addInterceptor(ChallengeInterceptor())
            .addInterceptor { chain ->
                requestCount++
                val isChallenge = requestCount == 1
                val body = if (isChallenge) {
                    """
                        <script>
                          document.cookie = 'yxd_token=abc123'
                          window.location.href='/s?key=test'
                        </script>
                    """.trimIndent()
                } else {
                    "<html><title>访问间隔过短</title></html>"
                }
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(if (isChallenge) 200 else 403)
                    .message(if (isChallenge) "OK" else "Forbidden")
                    .body(body.toResponseBody("text/html; charset=UTF-8".toMediaType()))
                    .build()
            }
            .build()

        val exception = assertFailsWith<McmodBlockedException> {
            client.newCall(Request.Builder().url("https://search.mcmod.cn/s?key=test").build()).execute()
        }

        assertEquals("mcmod rejected requests sent too frequently", exception.message)
        assertEquals(2, requestCount)
    }
}
