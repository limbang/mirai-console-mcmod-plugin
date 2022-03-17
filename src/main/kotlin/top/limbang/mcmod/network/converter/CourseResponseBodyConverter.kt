package top.limbang.mcmod.network.converter

import okhttp3.ResponseBody
import retrofit2.Converter
import top.limbang.mcmod.network.model.Course
import top.limbang.mcmod.network.utils.labelReplacement
import top.limbang.mcmod.network.utils.parse

/**
 * ### 把 html 响应解析成 [Course]
 */
class CourseResponseBodyConverter : Converter<ResponseBody, Course> {
    override fun convert(value: ResponseBody): Course {
        val document = value.parse()
        val name = document.select(".name").text()
        val introduction = document.select("[class=post-content common-text font14]").labelReplacement()
        return Course(name, introduction)
    }
}