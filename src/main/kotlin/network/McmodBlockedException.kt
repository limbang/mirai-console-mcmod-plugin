/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.network

import java.io.IOException

/**
 * ### 被 mcmod 反爬虫机制拦截
 *
 * 表示请求被站点的反爬虫挑战阻断, 即使提交了挑战 cookie 仍然无法通过.
 * 通常意味着出口 IP 已被 mcmod 深度标记, 这一情况不是 "搜索无结果" 也不是 "网络错误".
 */
class McmodBlockedException(
    message: String = "mcmod anti-crawler challenge could not be passed"
) : IOException(message)
