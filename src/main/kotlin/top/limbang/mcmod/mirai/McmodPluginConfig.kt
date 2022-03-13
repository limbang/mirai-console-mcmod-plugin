package top.limbang.mcmod.mirai

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

/**
 * ### mcmod 插件配置 "用户手动更改"
 */
object McmodPluginConfig : AutoSavePluginConfig("mcmod"){

    @ValueDescription("是否启用戳一戳回复功能 true:启用 false:禁用")
    val isNudgeEnabled: Boolean by value(true)

    @ValueDescription("是否启用群消息回复功能,默认回复群消息 true:启用 false:禁用")
    val isGroupMessagesEnabled: Boolean by value(true)

    @ValueDescription("是否启用好友消息回复功能,默认回复群消息 true:启用 false:禁用")
    val isFriendMessagesEnabled: Boolean by value(false)

    @ValueDescription("是否启用临时消息回复功能,默认回复群消息 true:启用 false:禁用")
    val isTempMessagesEnabled: Boolean by value(false)

    @ValueDescription("是否启用陌生人消息回复功能,默认回复群消息 true:启用 false:禁用")
    val isStrangerMessagesEnabled: Boolean by value(false)

    @ValueDescription("每页显示多少条目,默认为 6")
    val pageSize: Int by value(6)
}