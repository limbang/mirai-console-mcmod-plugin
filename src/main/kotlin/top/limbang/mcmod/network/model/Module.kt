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
 * ### 模组实体
 * @param iconUrl 模组图片
 * @param shortName 模组缩写
 * @param mainName 模组主要名称
 * @param secondaryName 模组次要名称
 * @param authors 作者或开发团队
 * @param introduction 模组介绍
 * @param relatedLinks 相关链接
 * @param versions 支持的版本
 */
data class Module(
    val iconUrl: String,
    val shortName: String,
    val mainName: String,
    val secondaryName: String,
    val authors: List<Author>,
    val introduction: String,
    val relatedLinks: List<String>,
    val versions: List<Version>
) {
    /**
     * ### 作者或开发团队
     * @param avatarUrl 头像 url
     * @param name 名称
     * @param relation 和模组的关系
     */
    data class Author(val avatarUrl: String, val name: String, val relation: String)

    /**
     * 支持的版本
     *
     * @property name api 名称
     * @property version 支持的版本列表
     */
    data class Version(val name: String, val version: List<String>)
}
