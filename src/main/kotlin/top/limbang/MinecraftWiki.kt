package top.limbang

import org.jsoup.Jsoup
import org.jsoup.select.Elements
import top.limbang.Filter.*

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
data class SearchResults(val title: String, val url: String)

object MinecraftWiki {

    private fun search(key: String, filer: Filter, cssQuery: String): Elements {
        val url = "https://www.mcmod.cn/s?key=$key&filter=${filer.ordinal}"
        return Jsoup.parse(RequestSupport.sendGetRequest(url)).select(cssQuery)
    }

    private fun message(searchResultsList: List<SearchResults>): String {
        var message = "请回复[]的内容来选择:\n"
        for (i in searchResultsList.indices) {
            message += "[#$i]:${searchResultsList[i].title}\n"
        }
        return message
    }

    /**
     * ### 获取搜索结果列表
     */
    private fun getSearchResultsList(key: String, filer: Filter, cssQuery: String): List<SearchResults> {
        val elements = search(key, filer, cssQuery)
        val searchResultsList = mutableListOf<SearchResults>()
        elements.forEach {
            searchResultsList.add(SearchResults(it.text(), it.attr("href")))
        }
        return searchResultsList
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


}