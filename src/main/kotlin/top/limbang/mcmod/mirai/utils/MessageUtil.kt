package top.limbang.mcmod.mirai.utils

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.buildForwardMessage
import top.limbang.mcmod.mirai.service.MiraiToMcmodService.readImage
import top.limbang.mcmod.network.model.Item

/**
 * ### 将物品转成消息
 */
suspend fun Item.toMessage(event: MessageEvent) : Message {
    return with(event){
        buildForwardMessage {
            if (iconUrl.isNotEmpty()) bot says readImage(iconUrl)
            bot says name
            bot says "合成表:$tabUrl"
        }
    }
}