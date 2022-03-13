package top.limbang.mcmod.network

import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class ServiceTest{

    @Test
    fun search(){
        val service = Service.getMcmodService
        runBlocking {
            runCatching { service.search("火红莲") }.onSuccess {
                println("成功:$it 大小：${it.size}")
            }.onFailure {
                println("失败:$it")
            }
        }
    }
}