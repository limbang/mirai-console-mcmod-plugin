/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.kind
import top.limbang.mcmod.PluginConfig.groupMessagesEnabled
import top.limbang.mcmod.PluginConfig.isFriendMessagesEnabled
import top.limbang.mcmod.PluginConfig.isGroupMessagesEnabled
import top.limbang.mcmod.PluginConfig.isNudgeEnabled
import top.limbang.mcmod.PluginConfig.isStrangerMessagesEnabled
import top.limbang.mcmod.PluginConfig.isTempMessagesEnabled
import top.limbang.mcmod.PluginData.queryCommand
import top.limbang.mcmod.network.model.SearchFilter
import top.limbang.mcmod.service.MiraiToMcmodService.toMcmodSearch


object Mcmod : KotlinPlugin(JvmPluginDescription(
    id = "top.limbang.mcmod",
    name = "Mcmod",
    version = "2.0.7",
) {
    author("limbang")
    info("""mc百科查询""")
}) {

    override fun onEnable() {
        PluginData.reload()
        PluginConfig.reload()
        PluginCompositeCommand.register()

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
                startsWith("$cmd ") {
                    // 根据配置过滤消息种类,默认只回复群消息
                    if (isNotReplyMessage(source)) {
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

        if (isNudgeEnabled) {
            // 监听戳一戳消息并回复帮助
            globalEventChannel().subscribeAlways<NudgeEvent> {
                if (target.id == bot.id) {
                    subject.sendMessage(
                        "Minecraft百科查询插件使用说明:\n" + "查询物品:${queryCommand[SearchFilter.ITEM]} <物品关键词>\n" + "查询模组:${queryCommand[SearchFilter.MODULE]} <模组关键词>\n" + "查询教程:${queryCommand[SearchFilter.COURSE]} <教程关键词>\n" + "查询整合包:${queryCommand[SearchFilter.MODULE_PACKAGE]} <整合包关键词>\n" + "查询服务器:${queryCommand[SearchFilter.SERVER]} <服务器关键词>\n" + "可私聊机器人查询，避免群内刷屏 :)\n" + "资料均来自:mcmod.cn"
                    )
                }
            }
        }
    }

    /**
     * ## 根据配置判断是否不回复消息
     * @param source 消息源
     */
    private fun isNotReplyMessage(source: MessageSource): Boolean {
        return when (source.kind) {
            MessageSourceKind.GROUP -> {
                return if (isGroupMessagesEnabled) {
                    !(groupMessagesEnabled[source.targetId] ?: false)
                } else true
            }
            MessageSourceKind.FRIEND -> !isFriendMessagesEnabled
            MessageSourceKind.TEMP -> !isTempMessagesEnabled
            MessageSourceKind.STRANGER -> !isStrangerMessagesEnabled
        }
    }

}

/**
 * ## 插件指令
 */
object PluginCompositeCommand : CompositeCommand(Mcmod, "mcmod") {
    @SubCommand
    @Description("配置查询指令")
    suspend fun CommandSender.setQueryCommand(type: SearchFilter, command: String) {
        sendMessage("The original `${type.name.lowercase()}` query command <${queryCommand[type]}> is changed to <$command> , please restart it to take effect.")
        queryCommand[type] = command
    }

    @SubCommand
    @Description("配置消息回复功能")
    suspend fun CommandSender.setConfig(kind: MessageSourceKind, enabled: Boolean = true) {
        when (kind) {
            MessageSourceKind.GROUP -> isGroupMessagesEnabled = enabled
            MessageSourceKind.TEMP -> isTempMessagesEnabled = enabled
            MessageSourceKind.FRIEND -> isFriendMessagesEnabled = enabled
            MessageSourceKind.STRANGER -> isStrangerMessagesEnabled = enabled
        }
        sendMessage("${if (enabled) "Enable" else "Disable"} ${kind.name.lowercase()} message reply function")
    }

    @SubCommand
    @Description("配置具体群消息回复功能")
    suspend fun CommandSender.setGroupConfig(enabled: Boolean, groupId: Long? = null) {
        if (groupId == null && this !is MemberCommandSenderOnMessage) {
            sendMessage("The group id cannot be empty.")
            return
        }
        if (enabled) isGroupMessagesEnabled = true
        val id = groupId ?: subject!!.id
        groupMessagesEnabled[id] = enabled
        sendMessage("Group $id ${if (enabled) "enable" else "disable"} message reply function")
    }
}

/**
 * ## mcmod 插件配置 "用户手动更改"
 */
object PluginConfig : AutoSavePluginConfig("mcmod") {

    @ValueDescription("具体群是否启用消息回复功能")
    val groupMessagesEnabled: MutableMap<Long, Boolean> by value()

    @ValueDescription("是否启用群消息回复功能,默认回复群消息 true:启用 false:禁用")
    var isGroupMessagesEnabled: Boolean by value(true)

    @ValueDescription("是否启用好友消息回复功能,默认禁用好友消息 true:启用 false:禁用")
    var isFriendMessagesEnabled: Boolean by value(false)

    @ValueDescription("是否启用临时消息回复功能,默认禁用临时消息 true:启用 false:禁用")
    var isTempMessagesEnabled: Boolean by value(false)

    @ValueDescription("是否启用陌生人消息回复功能,默认禁用陌生人消息 true:启用 false:禁用")
    var isStrangerMessagesEnabled: Boolean by value(false)

    @ValueDescription("是否启用戳一戳回复功能 true:启用 false:禁用")
    val isNudgeEnabled: Boolean by value(true)

    @ValueDescription("是否启用显示原Url功能,默认不启用 true:启用 false:禁用")
    val isShowOriginalUrlEnabled: Boolean by value(false)

    @ValueDescription("是否启用模组显示相关链接功能,默认不启用 true:启用 false:禁用")
    val isShowRelatedLinksEnabled: Boolean by value(false)

    @ValueDescription("是否启用模组显示支持版本功能,默认不启用 true:启用 false:禁用")
    val isShowSupportedVersionEnabled: Boolean by value(false)

    @ValueDescription("是否启用多次选择功能可以多次选择搜索的结果,默认不启用 true:启用 false:禁用")
    val isMultipleSelectEnabled: Boolean by value(false)

    @ValueDescription("每页显示多少条目,默认为 6")
    val pageSize: Int by value(6)
}

/**
 * ### 插件数据
 */
object PluginData : AutoSavePluginData("mcmod") {
    @ValueDescription("自定义查询指令存储")
    val queryCommand: MutableMap<SearchFilter, String> by value()
}