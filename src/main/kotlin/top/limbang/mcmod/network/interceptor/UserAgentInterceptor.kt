package top.limbang.mcmod.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * ### http 请求 UserAgent 拦截器
 * 伪装浏览器访问
 */
class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            // 拦截请求，移除默认 User-Agent
            .removeHeader("User-Agent")
            .addHeader(
                "User-Agent",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1"
            )
            .build()
        return chain.proceed(request)
    }
}