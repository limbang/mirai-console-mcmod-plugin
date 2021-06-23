package top.limbang.mirai.mcmod

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChainBuilder
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
        McmodPluginDatMca.reload()
        globalEventChannel().subscribeGroupMessages {
            startsWith("百科模组") { handle(it, this, Filter.MODULE) }
            startsWith("百科资料") { handle(it, this, Filter.DATA) }
            startsWith("百科教程") { handle(it, this, Filter.COURSE_OF_STUDY) }
            startsWith("查看") { select(it, this) }
        }
        globalEventChannel().subscribeAlways<NudgeEvent> {
            if (target.id == bot.id) {
                subject.sendMessage(
                    "Minecraft百科查询插件使用说明:\n" +
                            "查询物品:百科资料加物品名称\n" +
                            "查询模组:百科模组加模组名称\n" +
                            "查询教程:百科教程加教程名称\n"+
                            "资料均来自:mcmod.cn"
                )
            }
        }
    }

    private suspend fun handle(prefix: String, event: GroupMessageEvent, filter: Filter) {
        if(prefix.isEmpty()) return
        val list = MinecraftWiki.searchList(prefix, filter)
        McmodPluginDatMca.searchMap[event.group.id] = list
        event.group.sendMessage(message(list))
    }

    /**
     * ### 选择选项
     */
    private suspend fun select(prefix: String, event: GroupMessageEvent) {
        logger.debug(prefix)
        val serialNumber: Int
        try {
            serialNumber = prefix.toInt()
        } catch (e: NumberFormatException) {
            return
        }
        val list = McmodPluginDatMca.searchMap[event.group.id] ?: return
        if(serialNumber >= list.size) return

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

        val moduleMessageChain = MessageChainBuilder()
        moduleMessageChain.add(At(event.sender) + "\n")
        moduleMessageChain.add(readImage(module.iconUrl, event) + "\n")

        var name = ""
        if (module.shortName.isNotEmpty()) name += "缩写:${module.shortName}\n"
        if (module.cnName.isNotEmpty()) name += "中文:${module.cnName}\n"
        if (module.enName.isNotEmpty()) name += "英文:${module.enName}\n"
        moduleMessageChain.add(name)

        introductionMessage(moduleMessageChain, module.introduction, event)

        event.group.sendMessage(moduleMessageChain.build())
    }

    private suspend fun dataHandle(url: String, event: GroupMessageEvent) {
        val item = MinecraftWiki.parseItem(url)

        logger.debug(item.toString())

        val dataMessageChain = MessageChainBuilder()
        dataMessageChain.add(At(event.sender) + "\n")
        dataMessageChain.add(readImage(item.iconUrl, event) + "\n")
        dataMessageChain.add(item.name + "\n")
        introductionMessage(dataMessageChain, item.introduction, event)
        dataMessageChain.add("\n合成表:${item.tabUrl}\n")

        event.group.sendMessage(dataMessageChain.build())
    }

    private suspend fun courseOfStudyHandle(url: String, event: GroupMessageEvent) {
        val courseOfStudy = MinecraftWiki.parseCourseOfStudy(url)

        val courseOfStudyMessageChain = MessageChainBuilder()
        courseOfStudyMessageChain.add(At(event.sender) + "\n")
        courseOfStudyMessageChain.add("${courseOfStudy.name}\n")
        introductionMessage(courseOfStudyMessageChain, courseOfStudy.introduction, event)

        event.group.sendMessage(courseOfStudyMessageChain.build())
    }

    private fun message(searchResultsList: List<SearchResults>): String {
        var message = if(searchResultsList.isEmpty())
            "未查询到此内容...\n"
        else
            "请回复[]的内容来选择:\n"

        for (i in searchResultsList.indices) {
            message += "[查看$i]:${searchResultsList[i].title}\n"
        }
        return message
    }

    /**
     * 处理内容里面的图片，并上传图片
     */
    private suspend fun introductionMessage(
        messageChain: MessageChainBuilder,
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
            messageChain.add(strList[i])
            if (i < imgList.size) {
                messageChain.add(imgList[i])
            }
            i++
        }
    }

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
object McmodPluginDatMca : AutoSavePluginData("mcmod") {
    var searchMap: MutableMap<Long, List<SearchResults>> by value()
}