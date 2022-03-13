package top.limbang.mcmod.mirai.service

import org.junit.jupiter.api.Test

internal class MinecraftModTest {

    @Test
    fun parseServer() {
        println(MinecraftMod.parseServer("https://play.mcmod.cn/sv20184730.html"))
    }
}