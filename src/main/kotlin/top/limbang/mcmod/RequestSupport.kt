package top.limbang.mcmod

import okhttp3.OkHttpClient
import okhttp3.Request


object RequestSupport {


    /**
     * ### 发送GET请求
     */
    fun sendGetRequest(url: String): String {
        val okHttpClient = OkHttpClient()
        val request = Request.Builder().url(url).build()
        return okHttpClient.newCall(request).execute().body!!.string()
    }
}