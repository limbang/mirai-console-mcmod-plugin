/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.mirai

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.kind
import net.mamoe.mirai.message.data.source
import top.limbang.mcmod.mirai.McmodPluginData.queryCommand
import top.limbang.mcmod.mirai.service.MiraiToMcmodService.toMcmodSearch
import top.limbang.mcmod.network.model.SearchFilter


object McmodPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "top.limbang.mirai-console-mcmod-plugin",
        version = "2.0.1",
    ) {
        author("limbang")
        info("""mc百科查询""")
    }
) {

    override fun onEnable() {
        McmodPluginData.reload()
        McmodPluginConfig.reload()
        McmodPluginCompositeCommand.register()

        // 添加默认查询命令
        if (queryCommand[SearchFilter.MODULE] == null) queryCommand[SearchFilter.MODULE] = "ssm"
        if (queryCommand[SearchFilter.MODULE_PACKAGE] == null) queryCommand[SearchFilter.MODULE_PACKAGE] = "ssp"
        if (queryCommand[SearchFilter.ITEM] == null) queryCommand[SearchFilter.ITEM] = "ssi"
        if (queryCommand[SearchFilter.COURSE] == null) queryCommand[SearchFilter.COURSE] = "ssc"
        if (queryCommand[SearchFilter.SERVER] == null) queryCommand[SearchFilter.SERVER] = "sss"

        // 订阅所有来着 Bot 的消息
        globalEventChannel().subscribeMessages {
            // 处理查询命令开头的所有消息
            queryCommand.forEach { (filter, cmd) ->
                startsWith(cmd) {
                    // 根据配置过滤消息种类,默认只回复群消息
                    if (isMessageKindFilter(message.source.kind)) {
                        subject.sendMessage("未启用该方式查询,联系管理员更改配置")
                        return@startsWith
                    }
                    // 处理关键字为空的情况
                    if (it.isEmpty()) {
                        subject.sendMessage(message.quote() + "搜索关键字不能为空!")
                        return@startsWith
                    }
                    // 开始搜索
                    val searchMessage = toMcmodSearch(it, filter)
                    subject.sendMessage(searchMessage)
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

}

