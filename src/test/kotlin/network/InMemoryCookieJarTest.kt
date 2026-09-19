/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.network

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemoryCookieJarTest {
    @Test
    fun loadsHostAndParentDomainCookies() {
        val jar = InMemoryCookieJar()
        val searchUrl = "https://search.mcmod.cn/s".toHttpUrl()
        val mainUrl = "https://www.mcmod.cn/class/1.html".toHttpUrl()

        jar.saveFromResponse(
            searchUrl,
            listOf(
                Cookie.Builder()
                    .name("MCMOD_SEED")
                    .value("seed")
                    .hostOnlyDomain("search.mcmod.cn")
                    .path("/")
                    .build(),
                Cookie.Builder()
                    .name("_user_visit")
                    .value("visit")
                    .domain("mcmod.cn")
                    .path("/")
                    .secure()
                    .build()
            )
        )

        assertEquals(setOf("MCMOD_SEED", "_user_visit"), jar.loadForRequest(searchUrl).map { it.name }.toSet())
        assertEquals(listOf("_user_visit"), jar.loadForRequest(mainUrl).map { it.name })
    }

    @Test
    fun replacesCookieWithSameIdentity() {
        val jar = InMemoryCookieJar()
        val url = "https://search.mcmod.cn/s".toHttpUrl()
        fun seed(value: String) = Cookie.Builder()
            .name("MCMOD_SEED")
            .value(value)
            .hostOnlyDomain("search.mcmod.cn")
            .path("/")
            .build()

        jar.saveFromResponse(url, listOf(seed("old")))
        jar.saveFromResponse(url, listOf(seed("new")))

        assertEquals(listOf("new"), jar.loadForRequest(url).map { it.value })
    }
}
