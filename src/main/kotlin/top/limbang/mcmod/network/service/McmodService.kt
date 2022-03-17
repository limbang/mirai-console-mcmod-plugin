package top.limbang.mcmod.network.service

import okhttp3.ResponseBody
import retrofit2.http.*
import top.limbang.mcmod.network.model.*

interface McmodService {

    /**
     * ### 搜索
     * @param key 关键字
     * @param filter 搜索过滤,参考[SearchFilter],默认为[SearchFilter.ALL.ordinal]
     * @param page 页码,默认为 1
     * @return [SearchResult] 结果列表
     */
    @GET("s")
    suspend fun search(
        @Query("key") key: String,
        @Query("filter") filter: Int = SearchFilter.ALL.ordinal,
        @Query("page") page: Int = 1
    ): List<SearchResult>

    /**
     * ### 获取物品
     */
    @GET
    suspend fun getItem(@Url url: String): Item

    /**
     * ### 获取模组
     */
    @GET
    suspend fun getModule(@Url url: String): Module

    /**
     * ### 获取整合包
     */
    @GET
    suspend fun getModulePackage(@Url url: String): ModulePackage

    /**
     * ### 获取教程
     */
    @GET
    suspend fun getCourse(@Url url: String): Course

    /**
     * ### 搜索服务器
     */
    @POST
    suspend fun searchServer(@Url url: String = "https://play.mcmod.cn/frame/serverList/",@Body body:SearchServer): List<SearchResult>
    /**
     * ### 获取服务器
     */
    @GET
    suspend fun getServer(@Url url: String): Server

    /**
     * ### 下载文件,一次性下载完,大文件会有卡顿
     */
    @GET
    suspend fun downloadFile(@Url url: String): ResponseBody
}