package top.limbang.mirai.mcmod.service

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import top.limbang.mirai.mcmod.extension.substringBetween
import java.io.File
import java.util.*
import kotlin.math.min

object MessageHandle {
    /**
     * 模组消息处理
     */
    suspend fun moduleHandle(url: String, event: GroupMessageEvent) {
        val module = MinecraftMod.parseModule(url)

        val forwardMessageBuilder = ForwardMessageBuilder(event.group)
        forwardMessageBuilder.add(event.bot, readImage(module.iconUrl, event))
        var name = ""
        if (module.shortName.isNotEmpty()) name += "缩写:${module.shortName}\n"
        if (module.cnName.isNotEmpty()) name += "中文:${module.cnName}\n"
        if (module.enName.isNotEmpty()) name += "英文:${module.enName}"
        forwardMessageBuilder.add(event.bot, PlainText(name))
        forwardMessageBuilder.add(event.bot, PlainText(url))
        introductionMessage(forwardMessageBuilder, module.introduction, event)
        event.group.sendMessage(forwardMessageBuilder.build())
    }

    /**
     * 资料消息处理
     */
    suspend fun dataHandle(url: String, event: GroupMessageEvent) {
        val item = MinecraftMod.parseItem(url)

        val forwardMessageBuilder = ForwardMessageBuilder(event.group)
        forwardMessageBuilder.add(event.bot, readImage(item.iconUrl, event))
        forwardMessageBuilder.add(event.bot, PlainText(item.name))
        forwardMessageBuilder.add(event.bot, PlainText(url))
        introductionMessage(forwardMessageBuilder, item.introduction, event)
        forwardMessageBuilder.add(event.bot, PlainText("合成表:${item.tabUrl}"))

        event.group.sendMessage(forwardMessageBuilder.build())
    }

    /**
     * 教程消息处理
     */
    suspend fun courseOfStudyHandle(url: String, event: GroupMessageEvent) {
        val courseOfStudy = MinecraftMod.parseCourseOfStudy(url)

        val forwardMessageBuilder = ForwardMessageBuilder(event.group)
        forwardMessageBuilder.add(event.bot, PlainText(courseOfStudy.name))
        forwardMessageBuilder.add(event.bot, PlainText(url))
        introductionMessage(forwardMessageBuilder, courseOfStudy.introduction, event)

        event.group.sendMessage(forwardMessageBuilder.build())
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
                val maxLength = 1500
                val n = it.length / maxLength + if (it.length % maxLength != 0) 1 else 0
                for (i in 0 until n)
                    forwardMessageBuilder.add(event.bot, PlainText(it.substring(i*maxLength, min((i+1)*maxLength, it.length))))
                // forwardMessageBuilder.add(event.bot, PlainText(it))
            }
            if (i < imgList.size) {
                forwardMessageBuilder.add(event.bot, imgList[i])
            }
            i++
        }
    }


    /**
     * 读取图片
     */
    private suspend fun readImage(url: String, event: GroupMessageEvent): Image {
        val base64Prefix = "data:image/png;base64,"
        val imageExternalResource = if (url.startsWith(base64Prefix)) { // 处理base64情况
            Base64.getDecoder().decode(url.substring(base64Prefix.length)).toExternalResource()
        } else {
            val imgFileName = url.substringAfterLast("/").substringBefore("?")
            val file = File("data/top.limbang.mirai-console-mcmod-plugin/img/$imgFileName")
            if (file.exists()) {
                file.readBytes().toExternalResource()
            } else {
                HttpUtil.downloadImage(when {
                    url.startsWith("//") -> "https:$url" // 处理双斜杠开头情况"//i.mcmod.cn/..."
                    url.startsWith('/') -> "https://www.mcmod.cn$url" // 处理单斜杠开头情况"/xxx/xxx"
                    else -> url
                }, file).toExternalResource()
            }
        }
        val uploadImage = event.group.uploadImage(imageExternalResource)
        imageExternalResource.close()
        return uploadImage
    }
}
