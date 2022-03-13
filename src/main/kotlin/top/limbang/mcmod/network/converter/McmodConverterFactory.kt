package top.limbang.mcmod.network.converter

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import top.limbang.mcmod.network.model.SearchResult
import top.limbang.mcmod.network.utils.TypeToken
import java.lang.reflect.Type

class McmodConverterFactory private constructor(): Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        if (type == TypeToken.getType(List::class.java, SearchResult::class.java))
            return SearchResultResponseBodyConverter()
        return super.responseBodyConverter(type, annotations, retrofit)
    }

    companion object {
        fun create(): McmodConverterFactory {
            return McmodConverterFactory()
        }
    }
}

