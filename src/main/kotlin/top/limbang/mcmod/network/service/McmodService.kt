package top.limbang.mcmod.network.service

import retrofit2.http.GET
import retrofit2.http.Query
import top.limbang.mcmod.network.model.SearchFilter
import top.limbang.mcmod.network.model.SearchResult

interface McmodService {

    /**
     * ### 搜索
     * @param key 关键字
     * @param filter 搜索过滤,参考[SearchFilter],默认为[SearchFilter.All]
     * @param page 页码,默认为 1
     * @return [SearchResult] 结果列表
     */
    @GET("s")
    suspend fun search(
        @Query("key") key: String,
        @Query("filter") filter: SearchFilter = SearchFilter.ALL,
        @Query("page") page: Int = 1
    ): List<SearchResult>


}