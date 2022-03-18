/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.mirai.utils

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*
import top.limbang.mcmod.mirai.McmodPluginConfig
import top.limbang.mcmod.mirai.McmodPluginConfig.isShowOriginalUrlEnabled
import top.limbang.mcmod.mirai.service.MiraiToMcmodService.readImage
import top.limbang.mcmod.network.model.*
import top.limbang.mcmod.network.utils.substringBetween


/**
 * ### 把搜索的结果转换成 [ForwardMessage] 消息
 * @param event 消息事件
 * @param isFirst 是否是第一页
 */
fun List<SearchResult>.toMessage(event: MessageEvent, isFirst: Boolean) = with(event) {
    buildForwardMessage {
        bot says "30秒内回复编号查看"
        for (i in this@toMessage.indices) {
            val title =
                this@toMessage[i].title.replace("\\([^()]*\\)".toRegex(), "").replace("\\[[^\\[\\]]*]".toRegex(), "")
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

/**
 * ### 将模组转成消息
 */
suspend fun Module.toMessage(event: MessageEvent, originalUrl: String) = with(event) {
    buildForwardMessage {
        if (iconUrl.isNotEmpty()) bot says imgUrlToMessage(iconUrl)
        var name = ""
        if (shortName.isNotEmpty()) name += "缩写:${shortName}\n"
        if (mainName.isNotEmpty()) name += "主要名称:${mainName}\n"
        if (secondaryName.isNotEmpty()) name += "次要名称:${secondaryName}"
        bot says name
        bot says buildMessageChain {
            +"作者/开发团队:\n"
            entity.forEach { entity ->
                runCatching { readImage(entity.avatarUrl, true) }.onSuccess { +it }.onFailure { +it.localizedMessage }
                +"${entity.name}:${entity.relation}\n"
            }
        }
        bot says introductionToMessage(introduction)
        if (isShowOriginalUrlEnabled) bot says "原文链接:$originalUrl"
    }
}

/**
 * ### 将整合包转成消息
 */
suspend fun ModulePackage.toMessage(event: MessageEvent, originalUrl: String) = with(event) {
    buildForwardMessage {
        if (iconUrl.isNotEmpty()) bot says imgUrlToMessage(iconUrl)
        var outName = ""
        if (shortName.isNotEmpty()) outName += "缩写:${shortName}\n"
        if (name.isNotEmpty()) outName += "全称:${name}"
        bot says outName
        bot says introductionToMessage(introduction)
        if (isShowOriginalUrlEnabled) bot says "原文链接:$originalUrl"
    }
}

/**
 * ### 将教程转成消息
 */
suspend fun Course.toMessage(event: MessageEvent, originalUrl: String) = with(event) {
    buildForwardMessage {
        bot says name
        bot says introductionToMessage(introduction)
        if (isShowOriginalUrlEnabled) bot says "原文链接:$originalUrl"
    }
}

/**
 * ### 将服务器转成消息
 */
suspend fun Server.toMessage(event: MessageEvent, originalUrl: String) = with(event) {
    buildForwardMessage {
        if (iconUrl.isNotEmpty()) bot says imgUrlToMessage(iconUrl)
        bot says "名称:$name\n发布人:$publisher\nMC版本:$version\n在线玩家:$onlineUsers\n类型:$type\nQQ群:$qqGroup\n官网:$officialWebsite\n评分:$score"
        bot says introductionToMessage(introduction)
        if (isShowOriginalUrlEnabled) bot says "原文链接:$originalUrl"
    }
}

/**
 * ### 将物品转成消息
 */
suspend fun Item.toMessage(event: MessageEvent, originalUrl: String) = with(event) {
    buildForwardMessage {
        if (iconUrl.isNotEmpty()) bot says imgUrlToMessage(iconUrl)
        bot says name
        bot says introductionToMessage(introduction)
        bot says "合成表:$tabUrl"
        if (isShowOriginalUrlEnabled) bot says "原文链接:$originalUrl"
    }
}

/**
 * ### 图片url转成消息
 */
suspend fun MessageEvent.imgUrlToMessage(url: String): Message {
    runCatching { readImage(url) }.onSuccess { return it }.onFailure { return PlainText(it.localizedMessage) }
    return PlainText("未知错误")
}

/** 图片特征 */
private const val IMG_FEATURE_START = "<img data-src=\""
private const val IMG_FEATURE_END = "\">"

/**
 * ### 把 introduction 转成消息链
 */
private suspend fun MessageEvent.introductionToMessage(introduction: String) = buildMessageChain {
    if (introduction.length < 4500) {
        var newIntroduction = introduction.trim()
        var start: Int
        while (newIntroduction.indexOf(IMG_FEATURE_START).also { start = it } != -1) { // 判断是否是图片
            if (start != 0) +"${newIntroduction.substring(0, start).trim()}\n"
            val url = newIntroduction.substringBetween(IMG_FEATURE_START, IMG_FEATURE_END)
            runCatching { readImage(url) }.onSuccess {
                +it.plus("\n")
            }.onFailure { +"图片:[${it.localizedMessage}]\n" }
            start += url.length + IMG_FEATURE_START.length + IMG_FEATURE_END.length
            newIntroduction = newIntroduction.substring(start).trim()
        }
        if (newIntroduction.isNotEmpty()) +newIntroduction
    } else {
        +"介绍内容过长，请通过访问原文链接查看完整内容！"
    }
}