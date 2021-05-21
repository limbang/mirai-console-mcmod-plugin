package top.limbang

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import top.limbang.mcmod.Mcmod

object MiraiConsoleMcmodPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "top.limbang.mirai-console-mcmod-plugin",
        version = "1.0-SNAPSHOT",
    ) {
        author("limbang")
        info("""mc百科查询""")
    }
) {
    override fun onEnable() {
        globalEventChannel().subscribeGroupMessages {
            startsWith("查询") {
               val menuSelect =  Mcmod.search("植物魔法", Filter.ALL)
                group.sendMessage(menuSelect)
            }
        }
    }
}