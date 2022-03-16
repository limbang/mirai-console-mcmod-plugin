package top.limbang.mcmod.mirai.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.nextEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import okhttp3.HttpUrl.Companion.toHttpUrl
import top.limbang.mcmod.mirai.McmodPlugin
import top.limbang.mcmod.mirai.McmodPluginConfig
import top.limbang.mcmod.mirai.utils.PagingStorage
import top.limbang.mcmod.mirai.utils.toMessage
import top.limbang.mcmod.network.Service
import top.limbang.mcmod.network.model.SearchFilter
import top.limbang.mcmod.network.model.SearchFilter.ITEM
import top.limbang.mcmod.network.model.SearchResult
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
        runCatching { mcmodService.search(key, filter.ordinal, pagingStoragePage) }.onSuccess {
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
                            runCatching { mcmodService.search(key, filter.ordinal, page) }.onSuccess { nextList ->
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
            return PlainText("请求失败：${it.message}")
        }
        return PlainText("请不要输入一些奇奇怪怪的东西")
    }

    /**
     * ### 把搜索的结果转换成 [ForwardMessage] 消息
     * @param event 消息事件
     * @param isFirst 是否是第一页
     */
    private fun List<SearchResult>.toMessage(event: MessageEvent, isFirst: Boolean): ForwardMessage {
        return with(event) {
            buildForwardMessage {
                bot says "30秒内回复编号查看"
                for (i in this@toMessage.indices) {
                    val title = this@toMessage[i].title
                        .replace("\\([^()]*\\)".toRegex(), "")
                        .replace("\\[[^\\[\\]]*\\]".toRegex(), "")
                        .replace("\\s*-\\s*".toRegex(), "-")
                    bot.id named i.toString() says title
                }
                when {
                    this@toMessage.size < McmodPluginConfig.pageSize && !isFirst -> bot says "回复:[P]上一页"
                    this@toMessage.size == McmodPluginConfig.pageSize && !isFirst -> bot says "回复:[P]上一页 [N]下一页"
                    this@toMessage.size == McmodPluginConfig.pageSize && isFirst -> bot says "回复:[N]下一页"
                }
            }
        }
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
        when (filter) {
            ITEM -> {
                runCatching { mcmodService.getItem(searchResult.url) }.onSuccess {
                    return it.toMessage(event)
                }.onFailure { return PlainText("请求失败：${it.message}") }
            }
        }
        return PlainText("未实现")
    }

    /**
     * ### 读取图片
     */
    suspend fun MessageEvent.readImage(url: String): Image {
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
            println(imgUrl.toHttpUrl().encodedPath)
            println(imgUrl.toHttpUrl())

            val file = McmodPlugin.resolveDataFile("img/${imgUrl.toHttpUrl().encodedPath}")
            if (file.exists()) { // 判断本地是否已经存储
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
                            ImageIO.write(ImageIO.read(bytes.inputStream()), "jpeg", file)
                        }
                    } else {
                        file.writeBytes(bytes)
                    }
                }else{
                    file.writeBytes(bytes)
                }
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