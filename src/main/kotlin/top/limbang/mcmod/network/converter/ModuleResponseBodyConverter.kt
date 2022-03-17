package top.limbang.mcmod.network.converter

import okhttp3.ResponseBody
import retrofit2.Converter
import top.limbang.mcmod.network.model.Module
import top.limbang.mcmod.network.utils.labelReplacement
import top.limbang.mcmod.network.utils.parse

/**
 * ### 把 html 响应解析成 [Module]
 */
class ModuleResponseBodyConverter : Converter<ResponseBody, Module> {
    override fun convert(value: ResponseBody): Module {
        val document = value.parse()
        val iconUrl = document.select(".class-cover-image > img").attr("src")
        val shortName = document.select(".short-name").text()
        val mainName = document.select(".class-title > h3").text()
        val secondaryName = document.select(".class-title > h4").text()
        val introduction = document.select("[class=text-area common-text font14]").labelReplacement()
        return Module(iconUrl, shortName, mainName, secondaryName, introduction)
    }
}

