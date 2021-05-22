package top.limbang

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages

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
            startsWith("百科模组") { search(it, this, Filter.MODULE) }
            startsWith("百科资料") { search(it, this, Filter.DATA) }
            startsWith("百科教程") { search(it, this, Filter.COURSE_OF_STUDY) }
        }
    }

    private suspend fun search(prefix: String, event: GroupMessageEvent, filter: Filter) {
        val list = MinecraftWiki.searchList(prefix, filter)
        //event.group.sendMessage(list)
    }
}