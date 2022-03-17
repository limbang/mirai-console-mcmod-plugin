package top.limbang.mcmod.network.converter

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Converter
import retrofit2.http.GET
import top.limbang.mcmod.network.model.SearchResult
import top.limbang.mcmod.network.utils.parse

/**
 * ### 把 Html 转换成搜索结果
 */
class SearchResultResponseBodyConverter(private val annotation: Annotation) :
    Converter<ResponseBody, List<SearchResult>> {
    override fun convert(value: ResponseBody): List<SearchResult> {
        val list = mutableListOf<SearchResult>()
        if (annotation is GET) {
            val document = value.parse()
            val elements = document.select(".result-item > .head > a")
            elements.forEach {
                list.add(SearchResult(it.text(), it.attr("href")))
            }
        } else {
            val html = Json.parseToJsonElement(value.string()).jsonObject["html"]!!.jsonPrimitive.content
            val elements = Jsoup.parse(html).select(".col-lg-12 > a")
            elements.forEach {
                if (it.text().isNotEmpty()) {
                    list.add(SearchResult(it.text(), "https://play.mcmod.cn${it.attr("href")}"))
                }
            }
        }
        return list
    }
}