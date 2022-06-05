/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.network.utils

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class TypeToken<T> {
    protected val type: Type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]

    companion object {
        /**
         * ### 使用反射方式获取 Type,性能没有拼接强
         * 例如：
         * ```
         * // 获取 Map<String,List<String>> 的实际类型
         * TypeToken.getType<Map<String,List<String>>>()
         * ```
         */
        inline fun <reified T> getType() = object : TypeToken<T>() {}.type

        /**
         * ### 使用拼装方式获取 Type
         * 例如:
         * ```
         * // List<String>
         * getType(List::class.java,String::class.java)
         * // List<List<String>>
         * getType(List::class.java,getType(List::class.java,String::class.java))
         * // Map<Int,String>
         * getType(List::class.java,Int::class.java,String::class.java)
         * // Map<String,List<String>>
         * getType(Map::class.java,String::class.java, getType(List::class.java,String::class.java))
         * ```
         */
        fun getType(raw: Class<*>, vararg args: Type) = object : ParameterizedType {
            /** 返回最外层的类型，比如Map<K,V>中的Map */
            override fun getRawType(): Type = raw

            /** 返回<>中的类型，比如Map<K,V>中的K和V，因为可能有多个，所以以数组返回 */
            override fun getActualTypeArguments(): Array<out Type> = args

            /** 返回这个类型的所有者类型，如果这个类是个顶层类，那么返回null，如果是个内部类，那么就返回这个类的外层，比如 View.OnClickListener的View */
            override fun getOwnerType(): Type? = null
        }
    }

}