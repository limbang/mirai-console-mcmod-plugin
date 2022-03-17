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
        return Module(
            iconUrl = document.select(".class-cover-image > img").attr("src"),
            shortName = document.select(".short-name").text(),
            mainName = document.select(".class-title > h3").text(),
            secondaryName = document.select(".class-title > h4").text(),
            introduction = document.select("[class=text-area common-text font14]").labelReplacement()
        )
    }
}

