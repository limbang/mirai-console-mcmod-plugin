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
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.File


object MiraiConsoleMcmodPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "top.limbang.mirai-console-mcmod-plugin",
        version = "1.0.2",
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
        val detail = McmodPluginData.detail

        println(detail)

        globalEventChannel().subscribeGroupMessages {
            startsWith(module) { handle(it, this, Filter.MODULE) }
            startsWith(data) { handle(it, this, Filter.DATA) }
            startsWith(courseOfStudy) { handle(it, this, Filter.COURSE_OF_STUDY) }
            startsWith(detail) { select(it, this) }
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


    private suspend fun handle(prefix: String, event: GroupMessageEvent, filter: Filter) {
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
            Filter.MODULE -> moduleHandle(searchResults.url, event)
            Filter.DATA -> dataHandle(searchResults.url, event)
            Filter.COURSE_OF_STUDY -> courseOfStudyHandle(searchResults.url, event)
            else -> return
        }
    }

    private suspend fun moduleHandle(url: String, event: GroupMessageEvent) {
        val module = MinecraftWiki.parseModule(url)

        val forwardMessageBuilder = ForwardMessageBuilder(event.group)
        forwardMessageBuilder.add(event.sender, readImage(module.iconUrl, event))
        var name = ""
        if (module.shortName.isNotEmpty()) name += "缩写:${module.shortName}\n"
        if (module.cnName.isNotEmpty()) name += "中文:${module.cnName}\n"
        if (module.enName.isNotEmpty()) name += "英文:${module.enName}"
        forwardMessageBuilder.add(event.sender, PlainText(name))
        forwardMessageBuilder.add(event.sender, PlainText(url))
        introductionMessage(forwardMessageBuilder, module.introduction, event)
        event.group.sendMessage(forwardMessageBuilder.build())
    }

    private suspend fun dataHandle(url: String, event: GroupMessageEvent) {
        val item = MinecraftWiki.parseItem(url)

        val forwardMessageBuilder = ForwardMessageBuilder(event.group)
        forwardMessageBuilder.add(event.sender, readImage(item.iconUrl, event))
        forwardMessageBuilder.add(event.sender, PlainText(item.name))
        forwardMessageBuilder.add(event.sender, PlainText(url))
        introductionMessage(forwardMessageBuilder, item.introduction, event)
        forwardMessageBuilder.add(event.sender, PlainText("合成表:${item.tabUrl}"))

        event.group.sendMessage(forwardMessageBuilder.build())
    }

    private suspend fun courseOfStudyHandle(url: String, event: GroupMessageEvent) {
        val courseOfStudy = MinecraftWiki.parseCourseOfStudy(url)

        val forwardMessageBuilder = ForwardMessageBuilder(event.group)
        forwardMessageBuilder.add(event.sender, PlainText(courseOfStudy.name))
        forwardMessageBuilder.add(event.sender, PlainText(url))
        introductionMessage(forwardMessageBuilder, courseOfStudy.introduction, event)

        event.group.sendMessage(forwardMessageBuilder.build())
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

    /**
     * 处理内容里面的图片，并上传图片
     */
    private suspend fun introductionMessage(
        forwardMessageBuilder: ForwardMessageBuilder,
        introductionHtml: String,
        event: GroupMessageEvent
    ) {
        var introduction = introductionHtml
        val strList: MutableList<String> = ArrayList()
        val imgList: MutableList<Image> = ArrayList()
        var start: Int
        while (introduction.indexOf("<img data-src=").also { start = it } != -1) {
            strList.add(introduction.substring(0, start))
            introduction = introduction.substring(start)
            val imgUrl = introduction.substringBetween("<img data-src=\"", "\">")
            imgList.add(readImage(imgUrl, event))
            introduction = introduction.substring(imgUrl.length + 17)
        }
        strList.add(introduction)
        var i = 0
        while (strList.size > i) {
            strList[i].split("\n\n").forEach {
                forwardMessageBuilder.add(event.sender, PlainText(it))
            }
            if (i < imgList.size) {
                forwardMessageBuilder.add(event.sender, imgList[i])
            }
            i++
        }
    }

    /**
     * 读取图片
     */
    private suspend fun readImage(url: String, event: GroupMessageEvent): Image {
        val imgFileName = url.substringAfterLast("/").substringBefore("?")
        val file = File("data/top.limbang.mirai-console-mcmod-plugin/img/$imgFileName")
        val imageExternalResource = if (file.exists()) {
            file.readBytes().toExternalResource()
        } else {
            RequestSupport.downloadImage(url, file).toExternalResource()
        }
        val uploadImage = event.group.uploadImage(imageExternalResource)
        imageExternalResource.close()
        return uploadImage
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