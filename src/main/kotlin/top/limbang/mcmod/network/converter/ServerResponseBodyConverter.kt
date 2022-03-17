package top.limbang.mcmod.network.converter

import okhttp3.ResponseBody
import retrofit2.Converter
import top.limbang.mcmod.network.model.Server
import top.limbang.mcmod.network.utils.labelReplacement
import top.limbang.mcmod.network.utils.parse

/**
 * ### 把 html 响应解析成 [Server]
 */
class ServerResponseBodyConverter : Converter<ResponseBody, Server> {
    override fun convert(value: ResponseBody): Server? {
        val document = value.parse()
        val elements = document.select(".server-info-ext > li > span")
        val iconUrl = document.select(".favicon > img").attr("src")
        val name = document.select(".link > h6").text()
        val publisher = elements[0].text()
        val type = elements[3].text()
        val qqGroup = elements[7].text()
        val score = document.select(".score-avg > .score-number").text()
        val introduction = document.select(".common-text > .col-lg-12").labelReplacement()
        return Server(iconUrl, name, publisher, type, qqGroup, score, introduction)
    }
}