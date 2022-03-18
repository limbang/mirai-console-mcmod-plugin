/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.mirai.utils

import org.junit.Test
import java.io.File


internal class ImageUtilsTest {

    @Test
    fun zoomBySize() {
        val img = File("data/top.limbang.mirai-console-mcmod-plugin/img/warmthdawn")
        img.zoomBySize(45, 45)
    }
}