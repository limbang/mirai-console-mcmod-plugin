package top.limbang.mirai.mcmod

import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import org.jsoup.select.Elements
import top.limbang.mirai.mcmod.Filter.*

/**
 * ### 搜索过滤分类
 * - [ALL] 全部
 * - [MODULE] 模组
 * - [INTEGRATION_PACKAGE] 整合包
 * - [DATA] 资料
 * - [COURSE_OF_STUDY] 教程
 * - [AUTHOR] 作者
 * - [USER] 用户
 * - [COMMUNITY] 社群
 * - [SERVER] 服务器
 */
@Serializable
enum class Filter {
    ALL,
    MODULE,
    INTEGRATION_PACKAGE,
    DATA,
    COURSE_OF_STUDY,
    AUTHOR,
    USER,
    COMMUNITY,
    SERVER
}

/**
 * ### 搜索结果
 */
@Serializable
data class SearchResults(
    val title: String,
    val url: String,
    val filter: Filter
)

/**
 * ### 模组实体
 */
data class Module(
    val iconUrl: String = "",
    val shortName: String = "",
    val cnName: String = "",
    val enName: String = "",
    val introduction: String = ""
)

/**
 * ### 教程实体
 */
data class CourseOfStudy(
    val name: String = "",
    val introduction: String = "",
)

/**
 * ### 物品实体
 */
data class Item(
    val iconUrl: String = "",
    val name: String = "",
    val introduction: String = "",
    val tabUrl: String = ""
)

/**
 * ### Minecraft百科
 */
object MinecraftWiki {

    /**
     * ### 百科搜索
     */
    private fun search(key: String, filer: Filter, cssQuery: String): Elements {
        val url = "https://www.mcmod.cn/s?key=$key&filter=${filer.ordinal}"
        return RequestSupport.documentSelect(RequestSupport.sendGetRequestToParse(url), cssQuery)
    }

    /**
     * ### 获取各个分类的数量
     */
    fun searchMenu(key: String): List<SearchResults> {
        val cssQuery = ".search-menu-mcmod > ul > li > a"
        return getSearchResultsList(key, ALL, cssQuery)
    }

    /**
     * ### 搜索的结果列表
     */
    fun searchList(key: String, filer: Filter): List<SearchResults> {
        val cssQuery = ".result-item > .head > a"
        return getSearchResultsList(key, filer, cssQuery)
    }


    /**
     * ### 获取搜索结果列表
     */
    private fun getSearchResultsList(key: String, filer: Filter, cssQuery: String): List<SearchResults> {
        val elements = search(key, filer, cssQuery)
        val searchResultsList = mutableListOf<SearchResults>()
        elements.forEach {
            searchResultsList.add(SearchResults(it.text(), it.attr("href"),filer))
        }
        return searchResultsList
    }

    /**
     * ### 解析百科模组
     */
    fun parseModule(url: String): Module {
        val document = RequestSupport.sendGetRequestToParse(url)
        var iconUrl = document.select(".class-cover-image > img").attr("src")
        if (!iconUrl.contains("https")) iconUrl = "https:$iconUrl"
        val shortName = document.select(".short-name").text()
        val cnName = document.select(".class-title > h3").text()
        val enName = document.select(".class-title > h4").text()
        val introduction = labelReplacement(document.select("[class=text-area common-text font14]"))
        return Module(iconUrl, shortName, cnName, enName, introduction)
    }

    /**
     * ### 解析百科物品
     */
    fun parseItem(url: String): Item {
        val document = RequestSupport.sendGetRequestToParse(url)
        var iconUrl = document.select("td > img").attr("src")
        iconUrl = if (iconUrl.isNotEmpty()) {
            "https:$iconUrl"
        } else {
            document.select("td > a > img").attr("src")
        }
        val name = document.select(".name").text()
        val introduction = labelReplacement(document.select("[class=item-content common-text font14]"))
        val tabUrl = document.select("[class=common-icon-text item-table] > a").attr("href")
        return Item(iconUrl, name, introduction, "https://www.mcmod.cn$tabUrl")
    }

    /**
     * ### 解析百科教程
     */
    fun parseCourseOfStudy(url: String): CourseOfStudy {
        val document = RequestSupport.sendGetRequestToParse(url)
        val name = document.select(".name").text()
        val introduction = labelReplacement(document.select("[class=post-content common-text font14]"))
        return CourseOfStudy(name, introduction)
    }

    /**
     * ### 替换标签内容
     */
    private fun labelReplacement(elements: Elements): String {
        val whitelist = Whitelist()
        whitelist.addTags("p")
        whitelist.addAttributes("img", "data-src")
        var body = Jsoup.clean(elements.html(), whitelist)
        body = body.replace("<p>".toRegex(), "")
        body = body.replace("</p>".toRegex(), "")
        body = body.replace("&nbsp;".toRegex(), " ")
        return body
    }
}