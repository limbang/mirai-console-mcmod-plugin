package top.limbang.mcmod.network.converter

import okhttp3.ResponseBody
import retrofit2.Converter
import top.limbang.mcmod.network.model.ModulePackage
import top.limbang.mcmod.network.utils.labelReplacement
import top.limbang.mcmod.network.utils.parse
import top.limbang.mcmod.network.utils.substringBetween

/**
 * ### 把 html 响应解析成 [ModulePackage]
 */
class ModulePackageResponseBodyConverter : Converter<ResponseBody, ModulePackage> {
    override fun convert(value: ResponseBody): ModulePackage {
        val document = value.parse()
        val iconUrl = document.select(".class-cover-image > img").attr("src")
        val shortName = document.select(".short-name").text().substringBetween("[", "]")
        val name = document.select(".class-title > h3").text()
        val introduction = document.select("[class=text-area common-text font14]").labelReplacement()
        return ModulePackage(iconUrl, shortName, name, introduction)
    }
}