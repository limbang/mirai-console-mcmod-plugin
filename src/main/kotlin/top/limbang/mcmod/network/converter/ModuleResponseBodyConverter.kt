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

