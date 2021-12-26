package top.limbang.mirai.mcmod.service

import okhttp3.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.File


object HttpUtil {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AgentInterceptor())
        .build()

    /**
     * ### 下载图片
     */
    fun downloadImage(url: String, file: File): ByteArray {
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
    fun get(url: String): String {
        val request = Request.Builder().url(url).build()
        return okHttpClient.newCall(request).execute().body!!.string()
    }

    /**
     * ### 解析网页响应
     */
    fun parseBody(responseBody: String): Document {
        return Jsoup.parse(responseBody)
    }

    /**
     * ### 发送GET请求并解析
     */
    fun getDocument(url: String): Document {
        return parseBody(get(url))
    }

    /**
     * ### Document 元素选择
     */
    fun documentSelect(document: Document, cssQuery: String): Elements {
        return document.select(cssQuery)
    }
}

// user agent 拦截器
class AgentInterceptor : Interceptor{
    override fun intercept(chain: Interceptor.Chain): Response {
        // 拦截请求，移除默认 User-Agent
        val request = chain.request()
            .newBuilder()
            .removeHeader("User-Agent")
            .addHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.93 Safari/537.36")
            .build()
        return chain.proceed(request)
    }
}