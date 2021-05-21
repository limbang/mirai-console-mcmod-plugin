package top.limbang.mcmod

import org.jsoup.Jsoup
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.limbang.Filter

object Mcmod {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * ### 搜索MC百科结果
     */
    fun search(key: String, filer: Filter): String {
        val url = "https://www.mcmod.cn/s?key=$key&filter=$filer"
        val searchResult = RequestSupport.sendGetRequest(url)
        val document = Jsoup.parse(searchResult)

        val menu = document.select(".search-menu-mcmod > ul > li > a")

        var filer = 0
        var menuSelect = "请回复需要查询的分类序号:\n"
        menu.forEach {
            menuSelect += "[$filer]:${it.text()}\n"
            filer++
        }

        log.info(menuSelect)

        return menuSelect
    }


}