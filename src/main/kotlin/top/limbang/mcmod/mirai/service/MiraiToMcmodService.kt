/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.mirai.service

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
import top.limbang.mcmod.mirai.McmodPlugin
import top.limbang.mcmod.mirai.McmodPluginConfig
import top.limbang.mcmod.mirai.utils.PagingStorage
import top.limbang.mcmod.mirai.utils.toMessage
import top.limbang.mcmod.mirai.utils.zoomBySize
import top.limbang.mcmod.network.Service
import top.limbang.mcmod.network.model.SearchFilter
import top.limbang.mcmod.network.model.SearchFilter.*
import top.limbang.mcmod.network.model.SearchResult
import top.limbang.mcmod.network.model.SearchServer
import java.util.*
import javax.imageio.ImageIO

object MiraiToMcmodService {
    /** mcmod API 服务 */
    private val mcmodService = Service.getMcmodService

    /**
     * ### 搜索 mcmod
     * @param key 关键字
     * @param filter 搜索过滤
     */
    suspend fun MessageEvent.toMcmodSearch(key: String, filter: SearchFilter): Message {
        var pagingStoragePage = 1
        runCatching {
            if (filter == SERVER) mcmodService.searchServer(body = SearchServer(key, pagingStoragePage))
            else mcmodService.search(key, filter.ordinal, pagingStoragePage)
        }.onSuccess {
            // 未搜索到内容回复
            if (it.isEmpty()) return PlainText("没有找到与“ $key ”有关的内容")
            // 判断搜索到的结果是否只有一条,是就直接返回具体内容
            if (it.size == 1) return parseSearchResult(filter, it[0], this)

            var page = 2
            // 如果结果数等于30代表有下一页
            var isNextPage = it.size == 30
            // 创建分页存储
            val pagingStorage = PagingStorage<SearchResult>(McmodPluginConfig.pageSize)
            // 添加结果到存储里面
            pagingStorage.addAll(it)

            var nextEvent: MessageEvent
            do {
                val list = pagingStorage.getPageList(pagingStoragePage)
                val forwardMessage = list.toMessage(this, pagingStoragePage == 1)
                val listMessage = subject.sendMessage(forwardMessage)
                // 获取下一条消息事件
                nextEvent = withTimeoutOrNull(30000) {
                    GlobalEventChannel.nextEvent(EventPriority.MONITOR) { next -> next.sender == sender }
                } ?: return PlainText("等待超时,请重新查询")
                // 翻页控制
                val nextMessage = nextEvent.message.content
                val isContinue = when {
                    // 判断是否向下翻页
                    nextMessage.equals("n", true) -> {
                        val size = try {
                            pagingStorage.getPageList(pagingStoragePage + 1).size
                            pagingStoragePage++
                        } catch (e: ArrayIndexOutOfBoundsException) {
                            McmodPluginConfig.pageSize
                        }
                        // 获取下一页的数据,大小如果小于页面设置的默认值且有下一页就获取下请求
                        if (size < McmodPluginConfig.pageSize && isNextPage) {
                            runCatching {
                                if (filter == SERVER) mcmodService.searchServer(body = SearchServer(key, page))
                                else mcmodService.search(key, filter.ordinal, page)
                            }.onSuccess { nextList ->
                                isNextPage = nextList.size == 30
                                pagingStorage.addAll(nextList)
                                page++
                            }.onFailure { e ->
                                return PlainText("请求失败：${e.message}")
                            }
                        }
                        true
                    }
                    // 判断是否向上翻页
                    nextMessage.equals("p", true) -> {
                        // 页码大于 1 才能上翻
                        if (pagingStoragePage > 1) pagingStoragePage--
                        true
                    }
                    // 判断是否选择了序号
                    nextMessage.toIntOrNull() != null -> {
                        // 撤回发出的列表消息
                        listMessage.recall()
                        if (nextMessage.toInt() > list.size) return PlainText("输入的序号过大")
                        if (nextMessage.toInt() < 0) return PlainText("输入的序号过小")
                        return parseSearchResult(filter, list[nextMessage.toInt()], this)
                    }
                    else -> false
                }
                // 撤回发出的列表消息
                listMessage.recall()
            } while (isContinue)
        }.onFailure {
            println(it)
            return PlainText("请求失败：${it.message}")
        }
        return PlainText("请不要输入一些奇奇怪怪的东西")
    }


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
                ITEM -> mcmodService.getItem(searchResult.url).toMessage(event)
                MODULE -> mcmodService.getModule(searchResult.url).toMessage(event)
                MODULE_PACKAGE -> mcmodService.getModulePackage(searchResult.url).toMessage(event)
                COURSE -> mcmodService.getCourse(searchResult.url).toMessage(event)
                SERVER -> mcmodService.getServer(searchResult.url).toMessage(event)
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

            val file = McmodPlugin.resolveDataFile("img/${imgUrl.toHttpUrl().encodedPath}")
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

                if (type?.subtype == "jpeg") {
                    if (bytes[bytes.lastIndex].toUByte() != 0xD9.toUByte()) { //意外的JPG结尾
                        withContext(Dispatchers.IO) {
                            val bufferedImage = ImageIO.read(bytes.inputStream()) ?: throw IOException("不支持的格式")
                            ImageIO.write(bufferedImage, "png", file) // 都转成 png 格式
                        }
                    } else {
                        file.writeBytes(bytes)
                    }
                } else {
                    file.writeBytes(bytes)
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