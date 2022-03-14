package top.limbang.mcmod.network

import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class ServiceTest{
    private val service = Service.getMcmodService

    @Test
    fun search(){
        runBlocking {
            runCatching { service.search("火红莲") }.onSuccess {
                println("成功:$it 大小：${it.size}")
            }.onFailure {
                println("失败:$it")
            }
        }
    }

    @Test
    fun getItem(){
        runBlocking {
            runCatching { service.getItem("https://www.mcmod.cn/item/7386.html") }.onSuccess {
                println("成功:$it")
            }.onFailure {
                println("失败:$it")
            }
        }
    }

    @Test
    fun downloadFile(){
        runBlocking {

            runCatching { service.downloadFile("https://www.mcmod.cn/static/public/images/identicons/29.jpg")}.onSuccess {
                with(it){
                    println("contentType:${contentType()}")
                    println("contentLength:${contentLength()}")
                    println("bytes:${bytes().contentToString()}")
                }
            }.onFailure {
                println("失败:$it")
            }

            runCatching { service.downloadFile("https://www.mcmod.cn/pages/center/0/album/20150618/14345582456925.jpg") }.onSuccess {
               with(it){
                   println("contentType:${contentType()}")
                   println("contentLength:${contentLength()}")
                   println("bytes:${bytes().contentToString()}")
               }
            }.onFailure {
                println("失败:$it")
            }
        }
    }
}