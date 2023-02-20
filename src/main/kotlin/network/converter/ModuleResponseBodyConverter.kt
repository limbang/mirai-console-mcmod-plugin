/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.network.converter

import okhttp3.ResponseBody
import retrofit2.Converter
import top.limbang.mcmod.network.model.Module
import top.limbang.mcmod.network.utils.labelReplacement
import top.limbang.mcmod.network.utils.parse
import top.limbang.mcmod.network.utils.substringBetween
import java.util.*

/**
 * ### 把 html 响应解析成 [Module]
 */
class ModuleResponseBodyConverter : Converter<ResponseBody, Module> {
    override fun convert(value: ResponseBody): Module {
        val document = value.parse()
        // 解析作者或团队
        val avatarUrls = document.select(".frame > ul > li > .avatar > a > img")
        val names = document.select(".frame > ul > li > .member > .name > a")
        val relations = document.select(".frame > ul > li > .member > .position")
        val author = mutableListOf<Module.Author>()
        for (i in 0 until avatarUrls.size) {
            author.add(
                Module.Author(
                    avatarUrl = avatarUrls[i].attr("src"),
                    name = names[i].text(),
                    relation = relations[i].text(),
                )
            )
        }
        // 解析相关链接
        val relatedLinks = mutableListOf<String>()
        document.select(".common-link-frame > .list > ul > li")
            .forEach { li ->
                // 从脚本中截取 url
                val url = li.select("a").attr("href")
                if (url.isNotEmpty()) {
                    // 提取 base64 加密字符串并解码出原 url
                    val base64Url = url.substring(url.indexOf("/target/") + "/target/".length)
                    val decodeUrl = Base64.getDecoder().decode(base64Url).decodeToString()
                    // 从 a 标签中读取标题
                    val title = li.select("a").attr("data-original-title")
                    relatedLinks.add("$title: $decodeUrl")
                }
            }
        // 解析支持的MC版本
        val versions = mutableListOf<Module.Version>()
        document.select(".mcver > ul > ul")
            .forEach { ul ->
                val eachText = ul.select("li").eachText()
                versions.add(Module.Version(eachText[0].replace(":", ""), eachText.subList(1, eachText.size)))
            }
        return Module(
            iconUrl = document.select(".class-cover-image > img").attr("src"),
            shortName = document.select(".short-name").text().substringBetween("[", "]"),
            mainName = document.select(".class-title > h3").text(),
            secondaryName = document.select(".class-title > h4").text(),
            authors = author,
            introduction = document.select("[class=text-area common-text font14]").labelReplacement(),
            relatedLinks = relatedLinks,
            versions = versions
        )
    }
}

