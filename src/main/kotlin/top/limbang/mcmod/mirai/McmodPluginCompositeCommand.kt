package top.limbang.mcmod.mirai

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import top.limbang.mcmod.mirai.service.Filter

/**
 * ### 插件指令
 */
object McmodPluginCompositeCommand : CompositeCommand(
    MiraiConsoleMcmodPlugin, "mcmod"
) {
    @SubCommand("setQueryCommand", "查询命令")
    suspend fun CommandSender.setQueryCommand(type: Filter, command: String) {
        sendMessage("原查询$type 命令<${McmodPluginData.queryCommand[type]}>更改为<$command>,重启后生效")
        McmodPluginData.queryCommand[type] = command
    }

    @SubCommand("setNudgeEnabled", "戳一戳启用")
    suspend fun CommandSender.setNudgeEnabled(enabled: Boolean) {
        McmodPluginData.nudgeEnabled = enabled
        sendMessage("OK")
    }
}