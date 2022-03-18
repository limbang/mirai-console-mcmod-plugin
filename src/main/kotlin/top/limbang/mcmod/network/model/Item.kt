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
 * ### 物品实体
 * @param iconUrl 图标 url
 * @param name 名称
 * @param introduction 详情介绍
 * @param tabUrl 合成表 url
 */
data class Item(
    val iconUrl: String,
    val name: String,
    val introduction: String,
    val tabUrl: String
)