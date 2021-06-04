package top.limbang.mirai.mcmod

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.File


object RequestSupport {

    /**
     * ### 下载图片
     */
    fun downloadImage(url: String, file: File): ByteArray {
        val okHttpClient = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val imageByte = okHttpClient.newCall(request).execute().body!!.bytes()
        val fileParent = file.parentFile
        if (!fileParent.exists()) fileParent.mkdirs()
        file.writeBytes(imageByte)
        return imageByte
    }

    /**
     * ### 发送GET请求
     */
    fun sendGetRequest(url: String): String {
        val okHttpClient = OkHttpClient()
        val request = Request.Builder().url(url).build()
        return okHttpClient.newCall(request).execute().body!!.string()
    }

    /**
     * ### 解析网页响应
     */
    fun parseResponseBody(responseBody: String): Document {
        return Jsoup.parse(responseBody)
    }

    /**
     * ### 发送GET请求并解析
     */
    fun sendGetRequestToParse(url: String): Document {
        return parseResponseBody(sendGetRequest(url))
    }

    /**
     * ### Document 元素选择
     */
    fun documentSelect(document: Document, cssQuery: String): Elements {
        return document.select(cssQuery)
    }

}