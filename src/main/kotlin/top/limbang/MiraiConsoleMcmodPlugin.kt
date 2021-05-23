package top.limbang

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
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
        version = "1.0-SNAPSHOT",
    ) {
        author("limbang")
        info("""mc百科查询""")
    }
) {
    override fun onEnable() {
        PluginData.reload()
        globalEventChannel().subscribeGroupMessages {
            startsWith("百科模组") { handle(it, this, Filter.MODULE) }
            startsWith("百科资料") { handle(it, this, Filter.DATA) }
            startsWith("百科教程") { handle(it, this, Filter.COURSE_OF_STUDY) }
            startsWith("#") { select(it, this) }
        }
    }

    private suspend fun handle(prefix: String, event: GroupMessageEvent, filter: Filter) {
        val list = MinecraftWiki.searchList(prefix, filter)
        PluginData.searchMap[event.group.id] = list
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
        val searchResults = PluginData.searchMap[event.group.id]?.get(serialNumber)
        when (searchResults?.filter) {
            Filter.MODULE -> moduleHandle(searchResults.url, event)
            Filter.DATA -> dataHandle(searchResults.url, event)
            Filter.COURSE_OF_STUDY -> courseOfStudyHandle(searchResults.url, event)
            else -> return
        }
    }

    private suspend fun moduleHandle(url: String, event: GroupMessageEvent) {
        val module = MinecraftWiki.parseModule(url)

        val moduleMessageChain = MessageChainBuilder()
        moduleMessageChain.add(At(event.sender))
        moduleMessageChain.add(readImage(module.iconUrl,event))

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

        val moduleMessageChain = MessageChainBuilder()
        moduleMessageChain.add(At(event.sender))
        moduleMessageChain.add(readImage(item.iconUrl,event))
        moduleMessageChain.add("${item.name}\n")
        introductionMessage(moduleMessageChain, item.introduction, event)
        moduleMessageChain.add("合成表:${item.tabUrl}")

        event.group.sendMessage(moduleMessageChain.build())
    }

    private suspend fun courseOfStudyHandle(url: String, event: GroupMessageEvent) {
        val courseOfStudy = MinecraftWiki.parseCourseOfStudy(url)

        val moduleMessageChain = MessageChainBuilder()
        moduleMessageChain.add(At(event.sender))
        moduleMessageChain.add("${courseOfStudy.name}\n")
        introductionMessage(moduleMessageChain, courseOfStudy.introduction, event)

        event.group.sendMessage(moduleMessageChain.build())
    }

    private fun message(searchResultsList: List<SearchResults>): String {
        var message = "请回复[]的内容来选择:\n"
        for (i in searchResultsList.indices) {
            message += "[#$i]:${searchResultsList[i].title}\n"
        }
        return message
    }

    /**
     * 处理内容里面的图片，并上传图片
     */
    private suspend fun introductionMessage(
        messageChain: MessageChainBuilder,
        introduction: String,
        event: GroupMessageEvent
    ) {
        var introduction = introduction
        val strList: MutableList<String> = ArrayList()
        val imgList: MutableList<Image> = ArrayList()
        var start: Int
        while (introduction.indexOf("<img data-src=").also { start = it } != -1) {
            strList.add(introduction.substring(0, start))
            introduction = introduction.substring(start)
            val imgUrl = introduction.substringBefore("<img data-src=\"", "\">")
            imgList.add(readImage(imgUrl,event))
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
        val imgFileName = url.substringAfterLast("/")
        val file = File("data/top.limbang.mirai-console-mcmod-plugin/img/$imgFileName")
        val imageExternalResource  = if (file.exists()) {
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
object PluginData : AutoSavePluginData("mcmod") {
    var searchMap: MutableMap<Long, List<SearchResults>> by value()
}