package top.limbang.mcmod.mirai

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import top.limbang.mcmod.mirai.service.SearchFilter

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