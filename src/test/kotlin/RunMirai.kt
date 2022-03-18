/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */


import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import top.limbang.mcmod.mirai.McmodPlugin
import java.io.FileInputStream
import java.util.*

suspend fun main() {
    MiraiConsoleTerminalLoader.startAsDaemon()

    // 直接加载插件
    McmodPlugin.load()
    McmodPlugin.enable()

    // 读取账号配置
    val pros = Properties()
    val file = FileInputStream("local.properties")
    pros.load(file)

    val username = (pros["username"] as String).toLong()
    val password = pros["password"] as String


    val bot = MiraiConsole.addBot(username, password) {
        fileBasedDeviceInfo()
    }.alsoLogin()

    MiraiConsole.job.join()
}