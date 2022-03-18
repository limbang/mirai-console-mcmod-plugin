/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.network.utils

import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist
import org.jsoup.select.Elements


/**
 * ### 替换标签内容
 */
fun Elements.labelReplacement(): String {
    val whitelist = Safelist()
    whitelist.addTags("p")
    whitelist.addTags("br")
    whitelist.addAttributes("img", "data-src")
    var body = Jsoup.clean(html(), whitelist)
    body = body.replace("<p>".toRegex(), "")
    body = body.replace("</p>".toRegex(), "")
    body = body.replace("&nbsp;".toRegex(), " ")
    body = body.replace("<br>".toRegex(), "\n")
    return body
}

/**
 * ### 将 [ResponseBody] 解析成  [Document]
 */
fun ResponseBody.parse(): Document = Jsoup.parse(string())

/**
 * ### 取字符串之间
 * @param startStr 开始
 * @param endStr 结束
 */
fun String.substringBetween(startStr: String, endStr: String): String {
    val start = this.indexOf(startStr)
    if (start != -1) {
        val end = this.indexOf(endStr, start + startStr.length)
        if (end != -1) {
            return this.substring(start + startStr.length, end)
        }
    }
    return ""
}