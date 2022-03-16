package top.limbang.mcmod.network.service

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface UrlService {
    /**
     * ### 下载大文件
     */
    @Streaming
    @GET
    suspend fun downloadFile(@Url url:String): Response<ResponseBody>
}