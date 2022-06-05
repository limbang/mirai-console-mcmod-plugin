/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod

import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import java.io.File
import java.util.*

suspend fun main() {
    MiraiConsoleTerminalLoader.startAsDaemon()

    val pluginInstance = McmodPlugin

    pluginInstance.load() // 主动加载插件, Console 会调用 MinecraftRemoteConsole.onLoad
    pluginInstance.enable() // 主动启用插件, Console 会调用 MinecraftRemoteConsole.onEnable

    val properties = Properties().apply { File("account.properties").inputStream().use { load(it) } }

    val bot = MiraiConsole.addBot(properties.getProperty("id").toLong(), properties.getProperty("password")).alsoLogin() // 登录一个测试环境的 Bot

    MiraiConsole.job.join()
}
