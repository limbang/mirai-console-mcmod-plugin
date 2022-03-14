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