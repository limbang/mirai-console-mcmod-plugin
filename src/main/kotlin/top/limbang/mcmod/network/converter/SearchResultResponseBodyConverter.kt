package top.limbang.mcmod.network.converter

import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Converter
import top.limbang.mcmod.network.model.SearchResult

/**
 * ### 把 Html 转换成搜索结果
 */
class SearchResultResponseBodyConverter : Converter<ResponseBody, List<SearchResult>> {
    override fun convert(value: ResponseBody): List<SearchResult> {
        val document = Jsoup.parse(value.string())
        val elements = document.select(".result-item > .head > a")
        val list = mutableListOf<SearchResult>()
        elements.forEach {
            list.add(SearchResult(it.text(), it.attr("href")))
        }
        return list
    }
}