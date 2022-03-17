package top.limbang.mcmod.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test
import top.limbang.mcmod.network.model.DownloadStatus
import top.limbang.mcmod.network.model.SearchServer
import top.limbang.mcmod.network.utils.startDownload
import java.io.File
import javax.imageio.ImageIO

internal class ServiceTest {
    private val service = Service.getMcmodService

    @Test
    fun search() {
        runBlocking {
            runCatching { service.search("火红莲") }.onSuccess {
                println("成功:$it 大小：${it.size}")
            }.onFailure {
                println("失败:$it")
            }
        }
    }

    @Test
    fun searchServer(){
        runBlocking {
            runCatching { service.searchServer(body = SearchServer("遗落之地")) }.onSuccess {
                println("成功:$it 大小：${it.size}")
            }.onFailure {
                println("失败:$it")
            }
        }
    }

    @Test
    fun getItem() {
        runBlocking {
            runCatching { service.getItem("https://www.mcmod.cn/item/7386.html") }.onSuccess {
                println("成功:$it")
            }.onFailure {
                println("失败:$it")
            }
        }
    }

    @Test
    fun getModule() {
        runBlocking {
            runCatching { service.getModule("https://www.mcmod.cn/class/353.html") }.onSuccess {
                println("成功:$it")
            }.onFailure {
                println("失败:$it")
            }
        }
    }

    @Test
    fun getCourse() {
        runBlocking {
            runCatching { service.getCourse("https://www.mcmod.cn/post/273.html") }.onSuccess {
                println("成功:$it")
            }.onFailure {
                println("失败:$it")
            }
        }
    }

    @Test
    fun getModulePackage() {
        runBlocking {
            runCatching { service.getModulePackage("https://www.mcmod.cn/modpack/2.html") }.onSuccess {
                println("成功:$it")
            }.onFailure {
                println("失败:$it")
            }
        }
    }

    @Test
    fun getServer() {
        runBlocking {
            runCatching { service.getServer("https://play.mcmod.cn/sv20184730.html")}.onSuccess {
                println("成功:$it")
            }.onFailure {
                println("失败:$it")
            }
        }
    }

    /**
     * ### 测试意外结尾的图片修复
     */
    @Test
    fun image() {
        // 有问题的 jpg文件
        val url1 = "https://www.mcmod.cn/pages/center/0/album/20150618/14345582456925.jpg"
        // 无问题的jpg文件
        val url2 = "https://www.mcmod.cn/static/public/images/identicons/29.jpg"
        runBlocking {
            val body = service.downloadFile(url1)
            val type = body.contentType()
            val bytes = body.bytes()

            if (type?.subtype == "jpeg") {
                if (bytes[bytes.lastIndex].toUByte() != 0xD9.toUByte()) {
                    println("意外的JPG结尾")
                    withContext(Dispatchers.IO) {
                        val bufferedImage = ImageIO.read(bytes.inputStream())
                        ImageIO.write(bufferedImage, "jpeg", File("123.jpg"))
                    }
                }
            }
        }
    }

    /**
     * ### 测试大文件下载
     */
    @Test
    fun downloadFile() {
        val urlService = Service.getUrlService
        runBlocking {
            urlService.downloadFile("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
                .startDownload(File("big_buck_bunny.mp4")).collect {
                    when (it) {
                        is DownloadStatus.Error -> {
                            println(it.error)
                        }
                        is DownloadStatus.Success -> {
                            println(it.file)
                        }
                        is DownloadStatus.Process -> {
                            println("当前进度:${it.progress}% ${it.currentLength}/${it.contentLength}")
                        }
                    }
                }
        }
    }
}
