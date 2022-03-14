package top.limbang.mcmod.network.utils

import org.jsoup.Jsoup
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
