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
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * 保存 mcmod 下发的会话 Cookie.
 *
 * 新版安全验证使用 `MCMOD_SEED` 标识验证码会话, 验证成功后再通过
 * `.mcmod.cn` 域的 `_user_visit` Cookie 放行请求.
 */
internal class InMemoryCookieJar : CookieJar {
    private val cookies = mutableListOf<Cookie>()

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val now = System.currentTimeMillis()
        this.cookies.removeAll { stored ->
            stored.expiresAt <= now || cookies.any { incoming -> incoming.sameIdentityAs(stored) }
        }
        this.cookies.addAll(cookies.filter { it.expiresAt > now })
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        cookies.removeAll { it.expiresAt <= now }
        return cookies.filter { it.matches(url) }
    }

    private fun Cookie.sameIdentityAs(other: Cookie): Boolean =
        name == other.name && domain == other.domain && path == other.path
}
