package top.limbang.mcmod.network.model

/**
 * ### 物品实体
 * @param iconUrl 图标 url
 * @param name 名称
 * @param introduction 详情介绍
 * @param tabUrl 合成表 url
 */
data class Item(
    val iconUrl: String = "",
    val name: String = "",
    val introduction: String = "",
    val tabUrl: String = ""
)