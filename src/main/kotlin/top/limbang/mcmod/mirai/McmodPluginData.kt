package top.limbang.mcmod.mirai

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import top.limbang.mcmod.network.model.SearchFilter

/**
 * ### 插件数据
 */
object McmodPluginData : AutoSavePluginData("mcmod") {
    @ValueDescription("自定义查询指令存储")
    val queryCommand: MutableMap<SearchFilter, String> by value()
}