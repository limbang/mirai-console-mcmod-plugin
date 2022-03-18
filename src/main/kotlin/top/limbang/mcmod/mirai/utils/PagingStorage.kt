/*
 * Copyright 2020-2022 limbang and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/limbang/mirai-console-mcmod-plugin/blob/master/LICENSE
 */

package top.limbang.mcmod.mirai.utils

/**
 * ### 分页存储
 * @param pageMax 每页显示的最大数
 */
class PagingStorage<E>(private val pageMax: Int) {
    private val list = mutableListOf<E>()

    /**
     * ### 向分页存储添加集合元素
     */
    fun addAll(elements: Collection<E>): Boolean {
        return list.addAll(elements)
    }

    /**
     * ### 获取指定页数据
     * @param page 要获取的页码
     */
    fun getPageList(page: Int): MutableList<E> {
        return if (pageMax * page <= list.size) list.subList(pageMax * page - pageMax, pageMax * page)
        else if (pageMax * page - pageMax <= list.size) list.subList(pageMax * page - pageMax, list.size)
        else throw ArrayIndexOutOfBoundsException()
    }
}