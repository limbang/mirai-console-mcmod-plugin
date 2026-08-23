/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.nextEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.IOException
import top.limbang.mcmod.Mcmod
import top.limbang.mcmod.PluginConfig
import top.limbang.mcmod.PluginConfig.isMultipleSelectEnabled
import top.limbang.mcmod.network.Service
import top.limbang.mcmod.network.model.SearchFilter
import top.limbang.mcmod.network.model.SearchFilter.*
import top.limbang.mcmod.network.model.SearchResult
import top.limbang.mcmod.network.model.SearchServer
import top.limbang.mcmod.utils.PagingStorage
import top.limbang.mcmod.utils.toMessage
import top.limbang.mcmod.utils.zoomBySize
import java.util.*
import javax.imageio.ImageIO

object MiraiToMcmodService {
    /** mcmod API 服务 */
    private val mcmodService = Service.getMcmodService
    private const val PAGE_PREFETCH_LIMIT = 3

    /**
     * ### 搜索 mcmod
     * @param key 关键字
     * @param filter 搜索过滤
     */
    suspend fun MessageEvent.toMcmodSearch(key: String, filter: SearchFilter): Message? {
        var pagingStoragePage = 1
        var serverPage = 1
        var isNextPage = true
        val initialResults = mutableListOf<SearchResult>()
        var prefetchedPages = 0

        // 初始拉取: 客户端过滤模式下首页可能全部不属于目标分类, 多拉几页避免误判为无结果
        runCatching {
            do {
                val (filtered, hasMore) = fetchSearchPage(filter, key, serverPage)
                initialResults.addAll(filtered)
                isNextPage = hasMore
                serverPage++
                prefetchedPages++
            } while (initialResults.isEmpty() && isNextPage && prefetchedPages < PAGE_PREFETCH_LIMIT)
        }.onFailure {
            return PlainText("请求失败：${it.message}")
        }

        if (initialResults.isEmpty()) return PlainText("未查找到相关内容")
        if (initialResults.size == 1) return parseSearchResult(filter, initialResults[0], this)

        val pagingStorage = PagingStorage<SearchResult>(PluginConfig.pageSize)
        pagingStorage.addAll(initialResults)

        do {
            val list = pagingStorage.getPageList(pagingStoragePage)
            val hasNextPage = pagingStorage.pageSizeOrZero(pagingStoragePage + 1) > 0 || isNextPage
            val forwardMessage = list.toMessage(this, pagingStoragePage == 1, hasNextPage)
            val listMessage = subject.sendMessage(forwardMessage)
            // 获取下一条消息事件
            val nextEvent: MessageEvent? = withTimeoutOrNull(30000) {
                GlobalEventChannel.nextEvent(EventPriority.MONITOR) { next -> next.sender == sender }
            }
            if (nextEvent == null) {
                listMessage.recall()
                return null
            }
            // 翻页控制
            val nextMessage = nextEvent.message.content
            val selectedIndex = nextMessage.toIntOrNull()
            val isContinue = when {
                // 判断是否向下翻页
                nextMessage.equals("n", true) -> {
                    val currentPageSize = list.size
                    var nextPageSize = pagingStorage.pageSizeOrZero(pagingStoragePage + 1)
                    var fetchedPages = 0

                    // 客户端过滤可能让本地下一页不足, 有界补拉服务端页直到可翻页或没有更多数据
                    while (
                        nextPageSize < PluginConfig.pageSize &&
                        isNextPage &&
                        fetchedPages < PAGE_PREFETCH_LIMIT
                    ) {
                        val (filtered, hasMore) = runCatching {
                            fetchSearchPage(filter, key, serverPage)
                        }.getOrElse { e ->
                            return PlainText("请求失败：${e.message}").also { listMessage.recall() }
                        }

                        isNextPage = hasMore
                        pagingStorage.addAll(filtered)
                        serverPage++
                        fetchedPages++
                        nextPageSize = pagingStorage.pageSizeOrZero(pagingStoragePage + 1)
                    }

                    when {
                        nextPageSize > 0 -> {
                            pagingStoragePage++
                            true
                        }
                        pagingStorage.pageSizeOrZero(pagingStoragePage) > currentPageSize -> {
                            isNextPage = false
                            true
                        }
                        else -> {
                            isNextPage = false
                            return PlainText("没有更多内容").also { listMessage.recall() }
                        }
                    }
                }
                // 判断是否向上翻页
                nextMessage.equals("p", true) -> {
                    // 页码大于 1 才能上翻
                    if (pagingStoragePage > 1) pagingStoragePage--
                    true
                }
                // 判断是否选择了序号
                selectedIndex != null -> {
                    if (selectedIndex !in list.indices) {
                        val error = if (selectedIndex < 0) "输入的序号过小" else "输入的序号过大"
                        return PlainText(error).also { listMessage.recall() }
                    }
                    val message = parseSearchResult(filter, list[selectedIndex], this)
                    if (!isMultipleSelectEnabled) return message.also { listMessage.recall() }
                    subject.sendMessage(message)
                    true
                }
                else -> false
            }
            // 撤回发出的列表消息
            listMessage.recall()
        } while (isContinue)
        return null
    }

    /**
     * ### 执行一次搜索请求
     *
     * 对 [SERVER] 走专用接口; [MODULE]、[MODULE_PACKAGE]、[ITEM] 和 [COURSE]
     * 使用 ALL 接口后按 URL 客户端筛选, 借此复用 mcmod 在 ALL 模式下更智能的排序;
     * 其他分类仍使用原有服务端过滤参数
     * (官方过滤接口对热门关键字的排序较差, 例如 "AE2" 会被附属模组淹没).
     *
     * @return 过滤后的结果列表 to 服务端是否还有下一页
     */
    private suspend fun fetchSearchPage(
        filter: SearchFilter,
        key: String,
        page: Int
    ): Pair<List<SearchResult>, Boolean> = when (filter) {
        SERVER -> {
            val list = mcmodService.searchServer(body = SearchServer(key, page))
            list to (list.size == 30)
        }
        ALL -> {
            val list = mcmodService.search(key, ALL.ordinal, page)
            list to (list.size == 30)
        }
        MODULE, MODULE_PACKAGE, ITEM, COURSE -> {
            val raw = mcmodService.search(key, ALL.ordinal, page)
            raw.filter { urlMatchesFilter(it.url, filter) } to (raw.size == 30)
        }
        else -> {
            val list = mcmodService.search(key, filter.ordinal, page)
            list to (list.size == 30)
        }
    }

    /**
     * ### 按 URL 判断搜索结果是否属于指定分类
     *
     * mcmod ALL 接口返回的链接通过路径反映分类:
     * `/class/` 模组, `/modpack/` 整合包, `/item/` 物品资料, `/post/` 教程.
     */
    private fun urlMatchesFilter(url: String, filter: SearchFilter): Boolean = when (filter) {
        MODULE -> url.contains("/class/")
        MODULE_PACKAGE -> url.contains("/modpack/")
        ITEM -> url.contains("/item/")
        COURSE -> url.contains("/post/")
        else -> false
    }

    private fun PagingStorage<SearchResult>.pageSizeOrZero(page: Int): Int =
        runCatching { getPageList(page).size }.getOrDefault(0)

    /**
     * ### 解析搜索的结果
     * @param filter 过滤
     * @param searchResult 待解析的搜索结果
     * @param event
     */
    private suspend fun parseSearchResult(
        filter: SearchFilter,
        searchResult: SearchResult,
        event: MessageEvent
    ): Message {
        runCatching {
            return when (filter) {
                ITEM -> mcmodService.getItem(searchResult.url).toMessage(event, searchResult.url)
                MODULE -> mcmodService.getModule(searchResult.url).toMessage(event, searchResult.url)
                MODULE_PACKAGE -> mcmodService.getModulePackage(searchResult.url).toMessage(event, searchResult.url)
                COURSE -> mcmodService.getCourse(searchResult.url).toMessage(event, searchResult.url)
                SERVER -> mcmodService.getServer(searchResult.url).toMessage(event, searchResult.url)
                else -> TODO()
            }
        }.onFailure {
            return PlainText("请求失败：${it.message}")
        }
        return PlainText("未实现的分类查询!")
    }

    /**
     * ### 读取图片
     * @param url
     * @param isZoomBySize 是否缩放图片
     */
    suspend fun MessageEvent.readImage(url: String, isZoomBySize: Boolean = false): Image {
        val base64Prefix = "data:image/png;base64,"
        val imageExternalResource = if (url.startsWith(base64Prefix)) { // 处理base64情况
            Base64.getDecoder().decode(url.substring(base64Prefix.length)).toExternalResource()
        } else {
            // 处理 url
            val imgUrl = when {
                url.startsWith("//") -> "https:$url" // 处理双斜杠开头情况"//i.mcmod.cn/..."
                url.startsWith('/') -> "https://www.mcmod.cn$url" // 处理单斜杠开头情况"/xxx/xxx"
                else -> url
            }

            val file = Mcmod.resolveDataFile("img/${imgUrl.toHttpUrl().encodedPath}")
            if (file.exists()) { // 判断本地是否已经存储
                if (isZoomBySize) file.zoomBySize(45)
                file.readBytes().toExternalResource()
            } else {
                // 判断文件夹是否存在,不存在就创建
                val fileParent = file.parentFile
                if (!fileParent.exists()) fileParent.mkdirs()

                // 下载图片
                val responseBody = mcmodService.downloadFile(imgUrl)
                val type = responseBody.contentType()
                val bytes = responseBody.bytes()

                when (type?.subtype) {
                    "jpeg" -> {
                        if (bytes[bytes.lastIndex].toUByte() != 0xD9.toUByte()) { //意外的JPG结尾
                            withContext(Dispatchers.IO) {
                                val bufferedImage = ImageIO.read(bytes.inputStream()) ?: throw IOException("不支持的格式")
                                ImageIO.write(bufferedImage, "png", file) // 都转成 png 格式
                            }
                        } else {
                            file.writeBytes(bytes)
                        }
                    }
                    // Mirai不支持WebP文件上传，因此将其转为png文件
                    "webp" -> withContext(Dispatchers.IO) {
                        Thread.currentThread().contextClassLoader = Mcmod::class.java.classLoader
                        val bufferedImage = ImageIO.read(bytes.inputStream()) ?: throw IOException("不支持的格式")
                        ImageIO.write(bufferedImage, "png", file) // 都转成 png 格式
                    }
                    else -> file.writeBytes(bytes)
                }
                if (isZoomBySize) file.zoomBySize(45)
                file.toExternalResource()
            }
        }
        val image = subject.uploadImage(imageExternalResource)
        withContext(Dispatchers.IO) {
            imageExternalResource.close()
        }
        return image
    }
}
