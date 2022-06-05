/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import top.limbang.mcmod.network.model.SearchFilter

/**
 * ### 插件指令
 */
object McmodPluginCompositeCommand : CompositeCommand(
    McmodPlugin, "mcmod"
) {
    @SubCommand("setQueryCommand", "查询命令")
    suspend fun CommandSender.setQueryCommand(type: SearchFilter, command: String) {
        sendMessage("原查询$type 命令<${McmodPluginData.queryCommand[type]}>更改为<$command>,重启后生效")
        McmodPluginData.queryCommand[type] = command
    }
}