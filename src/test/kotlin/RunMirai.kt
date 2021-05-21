import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import top.limbang.MiraiConsoleMcmodPlugin
import java.io.FileInputStream
import java.util.*

suspend fun main() {
    MiraiConsoleTerminalLoader.startAsDaemon()

    // 直接加载插件
    MiraiConsoleMcmodPlugin.load()
    MiraiConsoleMcmodPlugin.enable()

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