package top.limbang.mcmod.network.converter

import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Converter
import top.limbang.mcmod.network.model.Item
import top.limbang.mcmod.network.utils.labelReplacement

/**
 * ### 把 html 响应解析成 [Item]
 */
class ItemResponseBodyConverter : Converter<ResponseBody, Item> {
    override fun convert(value: ResponseBody): Item {
        val document = Jsoup.parse(value.string())
        val iconUrl = document.select("td > img").attr("src")
            .run { if (this.isNotEmpty()) "https:$this" else document.select("td > a > img").attr("src") }
        val name = document.select(".name").text()
        val introduction = document.select("[class=item-content common-text font14]").labelReplacement()
        val tabUrl = "https://www.mcmod.cn" + document.select("[class=common-icon-text item-table] > a").attr("href")
        return Item(iconUrl, name, introduction, tabUrl)
    }
}