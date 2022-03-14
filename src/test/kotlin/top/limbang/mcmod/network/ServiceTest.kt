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
}