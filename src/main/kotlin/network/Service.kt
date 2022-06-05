/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import top.limbang.mcmod.network.converter.McmodConverterFactory
import top.limbang.mcmod.network.interceptor.UserAgentInterceptor
import top.limbang.mcmod.network.service.McmodService
import top.limbang.mcmod.network.service.UrlService
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

    /**
     * ### 获取 url 服务
     */
    val getUrlService: UrlService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://127.0.0.1/")
            .client(okHttpClient)
            .build()
        retrofit.create(UrlService::class.java)
    }
}