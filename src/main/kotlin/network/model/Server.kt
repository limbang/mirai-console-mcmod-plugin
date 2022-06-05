/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

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