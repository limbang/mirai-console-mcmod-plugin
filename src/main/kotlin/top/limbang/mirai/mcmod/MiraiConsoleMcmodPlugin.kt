package top.limbang.mirai.mcmod

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages


object MiraiConsoleMcmodPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "top.limbang.mirai-console-mcmod-plugin",
        version = "1.0.3",
    ) {
        author("limbang")
        info("""mc百科查询""")
    }
) {
    override fun onEnable() {
        McmodPluginData.reload()
        McmodPluginCompositeCommand.register()
        // 读取查询自定义命令
        val module = McmodPluginData.queryCommand[Filter.MODULE] ?: "百科模组"
        val data = McmodPluginData.queryCommand[Filter.DATA] ?: "百科资料"
        val courseOfStudy = McmodPluginData.queryCommand[Filter.COURSE_OF_STUDY] ?: "百科教程"

        globalEventChannel().subscribeGroupMessages {
            startsWith(module) { search(it, this, Filter.MODULE) }
            startsWith(data) { search(it, this, Filter.DATA) }
            startsWith(courseOfStudy) { search(it, this, Filter.COURSE_OF_STUDY) }
            startsWith(McmodPluginData.detail) { select(it, this) }
        }
        // 监听戳一戳消息并回复帮助
        globalEventChannel().subscribeAlways<NudgeEvent> {
            if (target.id == bot.id) {
                subject.sendMessage(
                    "Minecraft百科查询插件使用说明:\n" +
                            "查询物品:$data 加物品名称\n" +
                            "查询模组:$module 加模组名称\n" +
                            "查询教程:$courseOfStudy 加教程名称\n" +
                            "资料均来自:mcmod.cn"
                )
            }
        }
    }

    /**
     * 搜索内容
     */
    private suspend fun search(prefix: String, event: GroupMessageEvent, filter: Filter) {
        if (prefix.isEmpty()) return
        val list = MinecraftWiki.searchList(prefix, filter)
        McmodPluginData.searchMap[event.group.id] = list
        event.group.sendMessage(message(list))
    }

    /**
     * ### 选择选项
     */
    private suspend fun select(prefix: String, event: GroupMessageEvent) {
        val serialNumber: Int
        try {
            serialNumber = prefix.toInt()
        } catch (e: NumberFormatException) {
            return
        }
        val list = McmodPluginData.searchMap[event.group.id] ?: return
        if (serialNumber >= list.size) return

        val searchResults = list[serialNumber]
        when (searchResults.filter) {
            Filter.MODULE -> MessageHandle.moduleHandle(searchResults.url, event)
            Filter.DATA -> MessageHandle.dataHandle(searchResults.url, event)
            Filter.COURSE_OF_STUDY -> MessageHandle.courseOfStudyHandle(searchResults.url, event)
            else -> return
        }
    }

    private fun message(searchResultsList: List<SearchResults>): String {
        var message = if (searchResultsList.isEmpty())
            "未查询到此内容...\n"
        else
            "请回复[]的内容来选择:\n"

        for (i in searchResultsList.indices) {
            message += "[${McmodPluginData.detail}$i]:${searchResultsList[i].title}\n"
        }
        return message
    }
}

/**
 * ### 插件数据
 */
object McmodPluginData : AutoSavePluginData("mcmod") {
    val searchMap: MutableMap<Long, List<SearchResults>> by value()
    val queryCommand: MutableMap<Filter, String> by value()
    var detail by value("查看")
}


/**
 * ### 插件指令
 */
object McmodPluginCompositeCommand : CompositeCommand(
    MiraiConsoleMcmodPlugin, "mcmod"
) {
    @SubCommand("queryCommand", "查询命令")
    suspend fun CommandSender.queryCommand(type: Filter, command: String) {
        sendMessage("原查询$type 命令<${McmodPluginData.queryCommand[type]}>更改为<$command>,重启后生效")
        McmodPluginData.queryCommand[type] = command
    }

    @SubCommand("detailCommand", "详情命令")
    suspend fun CommandSender.detailCommand(command: String) {
        sendMessage("原详情命令<${McmodPluginData.detail}>更改为<$command>,重启后生效")
        McmodPluginData.detail = command
    }
}