package top.limbang.mcmod.network.model

/**
 * ### 搜索服务器用
 * @param key 关键字
 * @param page 页码
 */
data class SearchServer (val key: String,val page: Int = 1)