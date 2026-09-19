/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.network.service

import kotlinx.coroutines.runBlocking
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import kotlin.test.Test
import kotlin.test.assertEquals

class McmodServiceCaptchaTest {
    @Test
    fun submitsCaptchaAsFormToOriginalSearchUrl() {
        lateinit var capturedRequest: Request
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                capturedRequest = chain.request()
                Response.Builder()
                    .request(capturedRequest)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("<html></html>".toResponseBody())
                    .build()
            }
            .build()
        val service = Retrofit.Builder()
            .baseUrl("https://search.mcmod.cn/")
            .client(client)
            .build()
            .create(McmodService::class.java)

        runBlocking {
            service.solveCaptcha("https://search.mcmod.cn/s?key=ae2&filter=0&page=1", 2).close()
        }

        val body = capturedRequest.body as FormBody
        val fields = (0 until body.size).associate { body.name(it) to body.value(it) }
        assertEquals("POST", capturedRequest.method)
        assertEquals("https://search.mcmod.cn/s?key=ae2&filter=0&page=1", capturedRequest.url.toString())
        assertEquals(
            mapOf("cc_captcha_answer" to "2", "cc_captcha_submit" to "1"),
            fields
        )
    }
}
