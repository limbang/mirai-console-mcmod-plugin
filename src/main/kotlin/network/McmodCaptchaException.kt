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

/** mcmod 要求用户完成图片计数验证. */
class McmodCaptchaException(
    val requestUrl: String,
    val question: String,
    val imageBytes: ByteArray
) : IOException("mcmod captcha verification required")
