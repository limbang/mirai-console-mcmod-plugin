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
        return Server(
            iconUrl = document.select(".favicon > img").attr("src"),
            name = document.select(".link > h6").text(),
            publisher = elements[0].text().replace("(?)", "").trim(),
            version = elements[1].text(),
            onlineUsers = elements[2].text(),
            type = elements[3].text(),
            qqGroup = elements[7].text(),
            officialWebsite = elements[8].text(),
            score = document.select(".score-avg > .score-number").text(),
            introduction = document.select(".common-text > .col-lg-12").labelReplacement()
        )
    }
}