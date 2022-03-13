package top.limbang.mcmod.mirai.service

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.jsoup.select.Elements
import top.limbang.mcmod.mirai.extension.substringBetween
import top.limbang.mcmod.mirai.service.Filter.*

object MinecraftMod {

    private const val URL = "https://www.mcmod.cn"
    private const val SEARCH_URL = "https://search.mcmod.cn"
    private const val SERVER_URL = "https://play.mcmod.cn"
    private const val SEARCH_RESULT = ".result-item > .head > a"
    private const val INTRODUCTION_TEXT = "[class=text-area common-text font14]"
    private const val INTRODUCTION_ITEM = "[class=item-content common-text font14]"
    private const val INTRODUCTION_POST = "[class=post-content common-text font14]"
    private const val NAME = ".name"
    private const val SHORT_NAME = ".short-name"
    private const val H3_NAME = ".class-title > h3"
    private const val H4_NAME = ".class-title > h4"
    private const val ICON_URL = ".class-cover-image > img"
    private const val TAB_URL = "[class=common-icon-text item-table] > a"

    /**
     * ### 搜索的结果列表
     * @param key 关键词
     * @param filer 分类过滤 参考:[Filter]
     * @param page 页数
     */
    fun search(key: String, filer: Filter, page: Int): MutableList<SearchResult> {
        return search(key, filer, SEARCH_RESULT, page)
    }


    /**
     * ### 解析百科模组
     */
    fun parseModule(url: String): Module {
        val document = HttpUtil.getDocument(url)
        return Module(
            document.select(ICON_URL).attr("src").run {
                if (this.contains("https")) this else "https:$this"
            },
            document.select(SHORT_NAME).text(),
            document.select(H3_NAME).text(),
            document.select(H4_NAME).text(),
            labelReplacement(document.select(INTRODUCTION_TEXT))
        )
    }

    /**
     * ### 解析百科整合包
     */
    fun parseIntegrationPackage(url: String): IntegrationPackage {
        val document = HttpUtil.getDocument(url)
        return IntegrationPackage(
            document.select(ICON_URL).attr("src").run {
                if (this.contains("https")) this else "https:$this"
            },
            document.select(SHORT_NAME).text().substringBetween("[", "]"),
            document.select(H3_NAME).text(),
            labelReplacement(document.select(INTRODUCTION_TEXT))
        )
    }

    /**
     * ### 解析百科物品
     */
    fun parseItem(url: String): Item {
        val document = HttpUtil.getDocument(url)
        return Item(
            document.select("td > img").attr("src").run {
                if (this.isNotEmpty()) "https:$this"
                else document.select("td > a > img").attr("src")
            },
            document.select(NAME).text(),
            labelReplacement(document.select(INTRODUCTION_ITEM)),
            URL + document.select(TAB_URL).attr("href")
        )
    }

    /**
     * ### 解析百科教程
     */
    fun parseCourseOfStudy(url: String): CourseOfStudy {
        val document = HttpUtil.getDocument(url)
        return CourseOfStudy(
            document.select(NAME).text(),
            labelReplacement(document.select(INTRODUCTION_POST))
        )
    }

    /**
     * ### 解析百科服务器
     */
    fun parseServer(url: String): Server {
        val document = HttpUtil.getDocument(url)
        val elements = document.select(".server-info-ext > li > span")

        return Server(
            document.select(".favicon > img").attr("src"),
            document.select(".link > h6").text(),
            elements[0].text(),
            elements[3].text(),
            elements[7].text(),
            document.select(".score-avg > .score-number").text(),
            labelReplacement(document.select(".common-text > .col-lg-12"))
        )
    }

    /**
     * ### 替换标签内容
     */
    private fun labelReplacement(elements: Elements): String {
        val whitelist = Safelist()
        whitelist.addTags("p")
        whitelist.addAttributes("img", "data-src")
        var body = Jsoup.clean(elements.html(), whitelist)
        body = body.replace("<p>".toRegex(), "")
        body = body.replace("</p>".toRegex(), "")
        body = body.replace("&nbsp;".toRegex(), " ")
        return body
    }

    /**
     * ### 搜索
     * @param key 关键词
     * @param filer 分类过滤 参考:[Filter]
     * @param cssQuery css查询语句
     * @param page 页数
     */
    private fun search(key: String, filer: Filter, cssQuery: String, page: Int): MutableList<SearchResult> {
        val searchResultsList = mutableListOf<SearchResult>()
        return if (filer == SERVER) {
            val url = "$SERVER_URL/frame/serverList/"
            val body = FormBody.Builder()
                .add(
                    "data",
                    "{\"0\":{\"type\":\"search\",\"id\":\"$key\",\"see\":0},\"page\":$page,\"showOffline\":1,\"showModonly\":0}"
                )
                .build()
            val responseBody = HttpUtil.post(url, body)
            val html = Json.parseToJsonElement(responseBody).jsonObject["html"]!!.jsonPrimitive.content
            val elements = HttpUtil.documentSelect(HttpUtil.parseBody(html), ".col-lg-12 > a")
            elements.forEach {
                if (it.text().isNotEmpty()) {
                    searchResultsList.add(SearchResult(it.text(), SERVER_URL + it.attr("href"), filer))
                }
            }
            searchResultsList
        } else {
            val url = "$SEARCH_URL/s?key=$key&filter=${filer.ordinal}&page=$page"
            val elements = HttpUtil.documentSelect(HttpUtil.getDocument(url), cssQuery)
            elements.forEach {
                if (it.text().isNotEmpty()) {
                    searchResultsList.add(SearchResult(it.text(), it.attr("href"), filer))
                }
            }
            searchResultsList
        }

    }
}


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
data class SearchResult(
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
 * ### 整合包实体
 */
data class IntegrationPackage(
    val iconUrl: String = "",
    val shortName: String = "",
    val name: String = "",
    val introduction: String = ""
)

/**
 * ### 服务器实体
 */
data class Server(
    val iconUrl: String = "",
    val name: String = "",
    val publisher: String = "",
    val type: String = "",
    val qqGroup: String = "",
    val score : String = "",
    val introduction: String = ""
)