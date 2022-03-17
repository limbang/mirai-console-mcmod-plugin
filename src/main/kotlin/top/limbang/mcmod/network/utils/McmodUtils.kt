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
    whitelist.addAttributes("img", "data-src")
    var body = Jsoup.clean(html(), whitelist)
    body = body.replace("<p>".toRegex(), "")
    body = body.replace("</p>".toRegex(), "")
    body = body.replace("&nbsp;".toRegex(), " ")
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