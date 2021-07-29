package top.limbang.mirai.mcmod.service

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

class MinecraftModService {

    private val searchResultsList = mutableListOf<SearchResult>()
    private var page: Int = 1
    private var nextPage: Boolean = false

    private fun search(key: String, filer: Filter) {
        searchResultsList.addAll(MinecraftMod.search(key, filer, page))
        nextPage = searchResultsList.size == 30
    }

    fun getSearchList(key: String, filer: Filter, entry: Int): List<SearchResult> {
        if (page == 1) {
            search(key, filer);
            page++
        }
        val subList = if (entry <= searchResultsList.size) {
            searchResultsList.subList(0, entry)
        } else {
            if (nextPage) {
                search(key, filer)
                return getSearchList(key, filer, entry)
            } else {
                page = 1
                searchResultsList.subList(0, searchResultsList.size)
            }
        }
        val toList = subList.toList()
        subList.clear()
        return toList
    }

    fun searchListToString(list: List<SearchResult>, group: Group, sender: Member): Message {
        if (list.isEmpty()) return PlainText("未查询到此内容...\n")
        val builder = ForwardMessageBuilder(group)

        builder.add(sender,PlainText("30秒内回复编号查看"))

        for (i in list.indices) {
            val title = list[i].title
                .replace(Regex("\\([^()]*\\)"),"")
                .replace(Regex("\\[[^\\[\\]]*\\]"),"")
                .replace(Regex("\\s*-\\s*"),"-")
            builder.add(sender.id,i.toString(),PlainText(title))
        }
        if(searchResultsList.size > 0) builder.add(sender,PlainText("回复[P]下一页"))
        return builder.build()
    }

    fun getNextPage() = nextPage

    fun getSearchResultsListSize() = searchResultsList.size

    fun clear() {
        searchResultsList.clear()
        page = 1
        nextPage = false
    }

}
