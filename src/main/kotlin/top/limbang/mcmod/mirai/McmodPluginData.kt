package top.limbang.mcmod.mirai

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import top.limbang.mcmod.mirai.service.SearchFilter

/**
 * ### 插件数据
 */
object McmodPluginData : AutoSavePluginData("mcmod") {
    val queryCommand: MutableMap<SearchFilter, String> by value()
}