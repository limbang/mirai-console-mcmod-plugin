/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.network.converter

import okhttp3.FormBody
import okhttp3.RequestBody
import retrofit2.Converter
import top.limbang.mcmod.network.model.SearchServer

class ServerRequestBodyConverter : Converter<SearchServer, RequestBody> {
    override fun convert(value: SearchServer): RequestBody {
        return FormBody.Builder()
            .add(
                "data",
                "{\"0\":{\"type\":\"search\",\"id\":\"${value.key}\",\"see\":0},\"page\":${value.page},\"showOffline\":1,\"showModonly\":0}"
            )
            .build()
    }
}