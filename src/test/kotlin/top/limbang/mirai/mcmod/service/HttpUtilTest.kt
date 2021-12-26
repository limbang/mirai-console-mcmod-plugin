package top.limbang.mirai.mcmod.service

import org.junit.jupiter.api.Test

internal class HttpUtilTest {

    @Test
    fun downloadImage() {
    }

    @Test
    fun get() {
        val url = "https://search.mcmod.cn/s?key=火红莲&site=&filter=0&mold=0"
        val html = HttpUtil.get(url)
        println(html)
    }

    @Test
    fun parseBody() {
    }

    @Test
    fun getDocument() {
    }

    @Test
    fun documentSelect() {
    }
}