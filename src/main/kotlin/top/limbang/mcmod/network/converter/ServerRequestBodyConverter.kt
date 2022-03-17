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