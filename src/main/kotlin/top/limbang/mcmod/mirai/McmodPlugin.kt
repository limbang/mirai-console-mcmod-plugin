package top.limbang.mcmod.mirai

import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import top.limbang.mcmod.mirai.McmodPluginData.queryCommand
import top.limbang.mcmod.mirai.utils.PagingStorage
import top.limbang.mcmod.network.Service
import top.limbang.mcmod.network.model.SearchFilter
import top.limbang.mcmod.network.model.SearchResult


object McmodPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "top.limbang.mirai-console-mcmod-plugin",
        version = "2.0.0",
    ) {
        author("limbang")
        info("""mc百科查询""")
    }
) {

    override fun PluginComponentStorage.onLoad() {
        // 添加默认查询命令
        if (queryCommand[SearchFilter.MODULE] == null) queryCommand[SearchFilter.MODULE] = "ssm"
        if (queryCommand[SearchFilter.ITEM] == null) queryCommand[SearchFilter.ITEM] = "ssi"
        if (queryCommand[SearchFilter.COURSE] == null) queryCommand[SearchFilter.COURSE] = "ssc"
        if (queryCommand[SearchFilter.MODULE_PACKAGE] == null) queryCommand[SearchFilter.MODULE_PACKAGE] = "ssp"
        if (queryCommand[SearchFilter.SERVER] == null) queryCommand[SearchFilter.SERVER] = "sss"
        McmodPluginData.save()
    }

    override fun onEnable() {
        McmodPluginData.reload()
        McmodPluginConfig.reload()
        McmodPluginCompositeCommand.register()

        // 订阅所有来着 Bot 的消息
        globalEventChannel().subscribeMessages {
            // 处理查询命令开头的所有消息
            queryCommand.forEach { (filter, cmd) ->
                startsWith(cmd) {
                    // 根据配置过滤消息种类,默认只回复群消息
                    if (isMessageKindFilter(message.source.kind)) {
                        subject.sendMessage("未启用该方式查询,联系管理员更改配置。")
                        return@startsWith
                    }
                    // 处理关键字为空的情况
                    if (it.isEmpty()) {
                        subject.sendMessage(message.quote() + "搜索关键字不能为空!")
                        return@startsWith
                    }
                    // 搜索关键字
                    val mcmodService = Service.getMcmodService
                    runCatching { mcmodService.search(it, filter, 1) }.onSuccess { searchResultList ->
                        // 未搜索到内容回复
                        if (searchResultList.isEmpty()) {
                            subject.sendMessage(message.quote() + "没有找到与“ $it ”有关的内容")
                            return@startsWith
                        }
                        // 判断搜索到的结果是否只有一条,是就直接返回具体内容
                        if (searchResultList.size == 1) {
                            // TODO
                            return@startsWith
                        }
                        var page = 1
                        var pagingStoragePage = 1
                        // 如果结果数等于30代表有下一页
                        var isNextPage = searchResultList.size == 30
                        // 创建分页存储
                        val pagingStorage = PagingStorage<SearchResult>(McmodPluginConfig.pageSize)
                        // 添加结果到存储里面
                        pagingStorage.addAll(searchResultList)

                        var nextEvent: MessageEvent
                        do {
                            val forwardMessage = pagingStorage.getPageList(pagingStoragePage).toMessage(this)
                            val sendMessage = subject.sendMessage(forwardMessage)
                            // 获取下一条消息事件
                            nextEvent = withTimeoutOrNull(30000) {
                                GlobalEventChannel.nextEvent(EventPriority.MONITOR) { next -> next.sender == sender }
                            } ?: return@onSuccess
                            // 翻页控制
                            val isContinue = when (nextEvent.message.content) {
                                "n", "N" -> {
                                    // 获取下一页的数据,大小如果小于页面设置的默认值且有下一页就获取下请求
                                    val size = pagingStorage.getPageList(pagingStoragePage + 1).size
                                    if (size < McmodPluginConfig.pageSize && isNextPage) {
                                        page++
                                        runCatching { mcmodService.search(it, filter, page) }.onSuccess { nextList ->
                                            isNextPage = nextList.size == 30
                                            pagingStorage.addAll(nextList)
                                        }.onFailure {
                                            subject.sendMessage("因为网络或服务器原因请求失败。")
                                            return@startsWith
                                        }
                                    }
                                    pagingStoragePage++
                                    true
                                }
                                "p", "P" -> {
                                    // 页码大于 1 才能上翻
                                    if (pagingStoragePage > 1) pagingStoragePage--
                                    true
                                }
                                else -> false
                            }
                            sendMessage.recall()
                        } while (isContinue)
                        subject.sendMessage("等待超时或输入错误。")
                    }.onFailure {
                        subject.sendMessage("因为网络或服务器原因请求失败。")
                    }
                }
            }
        }

        if (McmodPluginConfig.isNudgeEnabled) {
            // 监听戳一戳消息并回复帮助
            globalEventChannel().subscribeAlways<NudgeEvent> {
                if (target.id == bot.id) {
                    subject.sendMessage(
                        "Minecraft百科查询插件使用说明:\n" +
                                "查询物品:${queryCommand[SearchFilter.ITEM]} <物品关键词>\n" +
                                "查询模组:${queryCommand[SearchFilter.MODULE]} <模组关键词>\n" +
                                "查询教程:${queryCommand[SearchFilter.COURSE]} <教程关键词>\n" +
                                "查询整合包:${queryCommand[SearchFilter.MODULE_PACKAGE]} <整合包关键词>\n" +
                                "查询服务器:${queryCommand[SearchFilter.SERVER]} <服务器关键词>\n" +
                                "可私聊机器人查询，避免群内刷屏 :)\n" +
                                "资料均来自:mcmod.cn"
                    )
                }
            }
        }
    }

    /**
     * ### 根据配置过滤回复的消息类型
     * @param kind 消息类型
     * @return true:过滤 false:不过滤
     */
    private fun isMessageKindFilter(kind: MessageSourceKind): Boolean {
        return when (kind) {
            MessageSourceKind.GROUP -> !McmodPluginConfig.isGroupMessagesEnabled
            MessageSourceKind.FRIEND -> !McmodPluginConfig.isFriendMessagesEnabled
            MessageSourceKind.TEMP -> !McmodPluginConfig.isTempMessagesEnabled
            MessageSourceKind.STRANGER -> !McmodPluginConfig.isStrangerMessagesEnabled
        }
    }

    /**
     * ### 把搜索的结果转换成 [ForwardMessage] 消息
     * @param event 消息事件
     */
    private fun List<SearchResult>.toMessage(event: MessageEvent): ForwardMessage {
        return with(event) {
            buildForwardMessage {
                bot says "30秒内回复编号查看"
                for (i in this@toMessage.indices) {
                    val title = this@toMessage[i].title
                        .replace(Regex("\\([^()]*\\)"), "")
                        .replace(Regex("\\[[^\\[\\]]*\\]"), "")
                        .replace(Regex("\\s*-\\s*"), "-")
                    bot.id named i.toString() says title
                }
                bot says "回复:[P]上一页 [N]下一页"
            }
        }
    }
}

