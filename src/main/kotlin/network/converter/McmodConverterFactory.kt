/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.network.converter

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import top.limbang.mcmod.network.model.*
import top.limbang.mcmod.network.utils.TypeToken
import java.lang.reflect.Type

/**
 * ### mcmod.cn 的自定义转换工厂
 */
class McmodConverterFactory private constructor(): Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        when(type){
            TypeToken.getType(List::class.java, SearchResult::class.java) -> return SearchResultResponseBodyConverter(annotations[0])
            Item::class.java -> return ItemResponseBodyConverter()
            Module::class.java -> return ModuleResponseBodyConverter()
            ModulePackage::class.java -> return ModulePackageResponseBodyConverter()
            Course::class.java -> return CourseResponseBodyConverter()
            Server::class.java -> return ServerResponseBodyConverter()
        }
        return super.responseBodyConverter(type, annotations, retrofit)
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        when(type){
            SearchServer::class.java -> return ServerRequestBodyConverter()
        }
        return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
    }

    companion object {
        fun create(): McmodConverterFactory {
            return McmodConverterFactory()
        }
    }
}

