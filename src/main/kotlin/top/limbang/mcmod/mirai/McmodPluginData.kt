package top.limbang.mcmod.mirai

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.limbang.mcmod.mirai.service.Filter

/**
 * ### 插件数据
 */
object McmodPluginData : AutoSavePluginData("mcmod") {
    val queryCommand: MutableMap<Filter, String> by value()

    @ValueDescription("是否启用戳一戳回复功能 true:启用 false:禁用")
    var nudgeEnabled: Boolean by value(true)
}