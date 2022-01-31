package top.limbang.mirai.mcmod.service

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import top.limbang.mirai.mcmod.MiraiConsoleMcmodPlugin
import top.limbang.mirai.mcmod.extension.substringBetween
import java.io.File
import java.util.*
import kotlin.math.min

object MessageHandle {
    /**
     * 模组消息处理
     */
    suspend fun moduleHandle(url: String, event: MessageEvent) {
        val module = MinecraftMod.parseModule(url)

        val forwardMessageBuilder = ForwardMessageBuilder(event.subject)
        forwardMessageBuilder.add(event.bot, readImage(module.iconUrl, event))
        var name = ""
        if (module.shortName.isNotEmpty()) name += "缩写:${module.shortName}\n"
        if (module.cnName.isNotEmpty()) name += "中文:${module.cnName}\n"
        if (module.enName.isNotEmpty()) name += "英文:${module.enName}"
        forwardMessageBuilder.add(event.bot, PlainText(name))
        forwardMessageBuilder.add(event.bot, PlainText(url))
        introductionMessage(forwardMessageBuilder, module.introduction, event)
        event.subject.sendMessage(forwardMessageBuilder.build())
    }

    /**
     * 整合包消息处理
     */
    suspend fun integrationPackageHandle(url: String, event: MessageEvent) {
        val integrationPackage = MinecraftMod.parseIntegrationPackage(url)

        val forwardMessageBuilder = ForwardMessageBuilder(event.subject)
        forwardMessageBuilder.add(event.bot, readImage(integrationPackage.iconUrl, event))
        var name = ""
        if (integrationPackage.shortName.isNotEmpty()) name += "缩写:${integrationPackage.shortName}\n"
        if (integrationPackage.name.isNotEmpty()) name += "全称:${integrationPackage.name}\n"
        forwardMessageBuilder.add(event.bot, PlainText(name))
        forwardMessageBuilder.add(event.bot, PlainText(url))
        introductionMessage(forwardMessageBuilder, integrationPackage.introduction, event)
        event.subject.sendMessage(forwardMessageBuilder.build())
    }

    /**
     * 资料消息处理
     */
    suspend fun dataHandle(url: String, event: MessageEvent) {
        val item = MinecraftMod.parseItem(url)

        val forwardMessageBuilder = ForwardMessageBuilder(event.subject)
        if (item.iconUrl.isNotEmpty()) forwardMessageBuilder.add(event.bot, readImage(item.iconUrl, event))
        forwardMessageBuilder.add(event.bot, PlainText(item.name))
        forwardMessageBuilder.add(event.bot, PlainText(url))
        introductionMessage(forwardMessageBuilder, item.introduction, event)
        forwardMessageBuilder.add(event.bot, PlainText("合成表:${item.tabUrl}"))

        event.subject.sendMessage(forwardMessageBuilder.build())
    }

    /**
     * 教程消息处理
     */
    suspend fun courseOfStudyHandle(url: String, event: MessageEvent) {
        val courseOfStudy = MinecraftMod.parseCourseOfStudy(url)

        val forwardMessageBuilder = ForwardMessageBuilder(event.subject)
        forwardMessageBuilder.add(event.bot, PlainText(courseOfStudy.name))
        forwardMessageBuilder.add(event.bot, PlainText(url))
        introductionMessage(forwardMessageBuilder, courseOfStudy.introduction, event)

        event.subject.sendMessage(forwardMessageBuilder.build())
    }

    /**
     * 服务器消息处理
     */
    suspend fun service(url: String, event: MessageEvent) {
        val service = MinecraftMod.parseServer(url)

        val forwardMessageBuilder = ForwardMessageBuilder(event.subject)
        if (service.iconUrl.isNotEmpty()) forwardMessageBuilder.add(event.bot, readImage(service.iconUrl, event))
        forwardMessageBuilder.add(event.bot, PlainText(service.name))
        forwardMessageBuilder.add(
            event.bot,
            PlainText("发布人:${service.publisher}\n类型:${service.type}\nQQ群:${service.qqGroup}\n评分:${service.score}")
        )
        forwardMessageBuilder.add(event.bot, PlainText(url))
        introductionMessage(forwardMessageBuilder, service.introduction, event)

        event.subject.sendMessage(forwardMessageBuilder.build())
    }

    /**
     * 处理内容里面的图片，并上传图片
     */
    private suspend fun introductionMessage(
        forwardMessageBuilder: ForwardMessageBuilder,
        introductionHtml: String,
        event: MessageEvent
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
        
        var totalLength = 0
        var i = 0
        while (strList.size > i && i < 40) {
            strList[i].split("\n\n").forEach {
                totalLength += it.length
                // 增加总长度限制
                if (totalLength > 4500) {
                    forwardMessageBuilder.add(event.bot, "介绍内容过长，请通过访问原文链接查看完整内容！")
                    break
                }
                val maxLength = 1500
                val n = it.length / maxLength + if (it.length % maxLength != 0) 1 else 0
                for (j in 0 until n)
                    forwardMessageBuilder.add(
                        event.bot,
                        PlainText(it.substring(j * maxLength, min((j + 1) * maxLength, it.length)))
                    )
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
    private suspend fun readImage(url: String, event: MessageEvent): Image {
        val base64Prefix = "data:image/png;base64,"
        val imageExternalResource = if (url.startsWith(base64Prefix)) { // 处理base64情况
            Base64.getDecoder().decode(url.substring(base64Prefix.length)).toExternalResource()
        } else {
            val imgFileName = url.substringAfterLast("/").substringBefore("?")
            val file = MiraiConsoleMcmodPlugin.resolveDataFile("img/$imgFileName")
            if (file.exists()) {
                file.readBytes().toExternalResource()
            } else {
                HttpUtil.downloadImage(
                    when {
                        url.startsWith("//") -> "https:$url" // 处理双斜杠开头情况"//i.mcmod.cn/..."
                        url.startsWith('/') -> "https://www.mcmod.cn$url" // 处理单斜杠开头情况"/xxx/xxx"
                        else -> url
                    }, file
                ).toExternalResource()
            }
        }
        val uploadImage = event.subject.uploadImage(imageExternalResource)
        imageExternalResource.close()
        return uploadImage
    }

}
