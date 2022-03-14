package top.limbang.mcmod.mirai.service

import com.sun.xml.internal.ws.commons.xmlutil.Converter.toMessage
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.nextEvent
import net.mamoe.mirai.message.data.*
import top.limbang.mcmod.mirai.McmodPluginConfig
import top.limbang.mcmod.mirai.utils.PagingStorage
import top.limbang.mcmod.network.Service
import top.limbang.mcmod.network.model.SearchFilter
import top.limbang.mcmod.network.model.SearchResult

object MiraiToMcmodService {
    /** mcmod API 服务 */
    private val mcmodService = Service.getMcmodService

    /**
     * ### 搜索 mcmod
     * @param key 关键字
     * @param filter 搜索过滤
     */
    suspend fun MessageEvent.toMcmodSearch(key: String, filter: SearchFilter): Message {
        var pagingStoragePage = 1
        runCatching { mcmodService.search(key, filter.ordinal, pagingStoragePage) }.onSuccess {
            // 未搜索到内容回复
            if (it.isEmpty()) return PlainText("没有找到与“ $key ”有关的内容")
            // 判断搜索到的结果是否只有一条,是就直接返回具体内容
            if (it.size == 1) {
                // TODO
                return PlainText("")
            }
            var page = 2
            // 如果结果数等于30代表有下一页
            var isNextPage = it.size == 30
            // 创建分页存储
            val pagingStorage = PagingStorage<SearchResult>(McmodPluginConfig.pageSize)
            // 添加结果到存储里面
            pagingStorage.addAll(it)

            var nextEvent: MessageEvent
            do {
                val list = pagingStorage.getPageList(pagingStoragePage)
                val forwardMessage = list.toMessage(this, pagingStoragePage == 1)
                val listMessage = subject.sendMessage(forwardMessage)
                // 获取下一条消息事件
                nextEvent = withTimeoutOrNull(30000) {
                    GlobalEventChannel.nextEvent(EventPriority.MONITOR) { next -> next.sender == sender }
                } ?: return PlainText("等待超时,请重新查询")
                // 翻页控制
                val nextMessage = nextEvent.message.content
                val isContinue = when {
                    // 判断是否向下翻页
                    nextMessage.equals("n", true) -> {
                        val size = try {
                            pagingStorage.getPageList(pagingStoragePage + 1).size
                            pagingStoragePage++
                        } catch (e: ArrayIndexOutOfBoundsException) {
                            McmodPluginConfig.pageSize
                        }
                        // 获取下一页的数据,大小如果小于页面设置的默认值且有下一页就获取下请求
                        if (size < McmodPluginConfig.pageSize && isNextPage) {
                            runCatching { mcmodService.search(key, filter.ordinal, page) }.onSuccess { nextList ->
                                isNextPage = nextList.size == 30
                                pagingStorage.addAll(nextList)
                                page++
                            }.onFailure { e ->
                                return PlainText("请求失败：${e.message}")
                            }
                        }
                        true
                    }
                    // 判断是否向上翻页
                    nextMessage.equals("p", true) -> {
                        // 页码大于 1 才能上翻
                        if (pagingStoragePage > 1) pagingStoragePage--
                        true
                    }
                    // 判断是否选择了序号
                    nextMessage.toIntOrNull() != null -> {
                        if (nextMessage.toInt() > list.size) return PlainText("输入的序号过大")
                        if (nextMessage.toInt() < 0) return PlainText("输入的序号过小")
                        // TODO 逻辑
                        return PlainText("")
                    }
                    else -> false
                }
                // 撤回发出的列表消息
                listMessage.recall()
            } while (isContinue)
        }.onFailure {
            return PlainText("请求失败：${it.message}")
        }
        return PlainText("请不要输入一些奇奇怪怪的东西")
    }

    /**
     * ### 把搜索的结果转换成 [ForwardMessage] 消息
     * @param event 消息事件
     * @param isFirst 是否是第一页
     */
    private fun List<SearchResult>.toMessage(event: MessageEvent, isFirst: Boolean): ForwardMessage {
        return with(event) {
            buildForwardMessage {
                bot says "30秒内回复编号查看"
                for (i in this@toMessage.indices) {
                    val title = this@toMessage[i].title
                        .replace("\\([^()]*\\)".toRegex(), "")
                        .replace("\\[[^\\[\\]]*\\]".toRegex(), "")
                        .replace("\\s*-\\s*".toRegex(), "-")
                    bot.id named i.toString() says title
                }
                when {
                    this@toMessage.size < McmodPluginConfig.pageSize && !isFirst -> bot says "回复:[P]上一页"
                    this@toMessage.size == McmodPluginConfig.pageSize && !isFirst -> bot says "回复:[P]上一页 [N]下一页"
                    this@toMessage.size == McmodPluginConfig.pageSize && isFirst -> bot says "回复:[N]下一页"
                }
            }
        }
    }
}