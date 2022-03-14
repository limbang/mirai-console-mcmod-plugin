package top.limbang.mcmod.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import top.limbang.mcmod.network.converter.McmodConverterFactory
import top.limbang.mcmod.network.interceptor.UserAgentInterceptor
import top.limbang.mcmod.network.service.McmodService
import java.util.concurrent.TimeUnit

object Service {
    /**
     * ### 创建 okhttp 客户端
     */
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(UserAgentInterceptor())
            .build()
    }

    /**
     * ### 获取 mcmod 服务
     */
    val getMcmodService: McmodService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://search.mcmod.cn/")
            .client(okHttpClient)
            .addConverterFactory(McmodConverterFactory.create())
            .build()
        retrofit.create(McmodService::class.java)
    }
}