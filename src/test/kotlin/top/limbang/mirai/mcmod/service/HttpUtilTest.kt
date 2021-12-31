package top.limbang.mirai.mcmod.service

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
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

    @Test
    fun post() {

        val body = FormBody.Builder()
            .add(
                "data",
                "{\"0\":{\"type\":\"search\",\"id\":\"遗落\",\"see\":0},\"page\":1,\"showOffline\":1,\"showModonly\":0}"
            )
            .build()
        val url = "https://play.mcmod.cn/frame/serverList/"
        val html = Json.parseToJsonElement(HttpUtil.post(url, body)).jsonObject["html"]!!.jsonPrimitive!!.content
        println(html)
        val document = HttpUtil.parseBody(html)
        val elements = HttpUtil.documentSelect(document, ".col-lg-12 > a")

        elements.forEach {
            if(it.text().isNotEmpty()){
                println(it.attr("href"))
                println(it.text())
            }
        }

    }
}