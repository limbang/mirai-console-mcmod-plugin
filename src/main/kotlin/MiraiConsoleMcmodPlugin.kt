package top.limbang

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.utils.info

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
        logger.info { "Plugin loaded" }
    }
}