package top.limbang.mcmod.network.model

/**
 * ### 服务器实体
 * @param iconUrl 服务器图标
 * @param name 服务器名称
 * @param publisher 发布人
 * @param version 服务器版本
 * @param onlineUsers 服务器在线人数
 * @param type 服务器类型
 * @param qqGroup qq群
 * @param officialWebsite 服务器官网
 * @param score 服务器评分
 * @param introduction 服务器介绍
 */
data class Server(
    val iconUrl: String,
    val name: String,
    val publisher: String,
    val version: String,
    val onlineUsers: String,
    val type: String,
    val qqGroup: String,
    val officialWebsite: String,
    val score: String,
    val introduction: String
)