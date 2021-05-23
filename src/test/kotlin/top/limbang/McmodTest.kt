package top.limbang


fun main() {
    //println(MinecraftWiki.searchList("植物魔法",Filter.MODULE))
    //println(MinecraftWiki.searchList("植物魔法",Filter.DATA))
    //println(MinecraftWiki.searchList("植物魔法",Filter.COURSE_OF_STUDY))

    val searchList1 = MinecraftWiki.searchList("植物魔法", Filter.MODULE)
    println(searchList1)
    println(MinecraftWiki.parseModule(searchList1[1].url))

    val searchList2 = MinecraftWiki.searchList("火红莲", Filter.DATA)
    println(searchList2)
    println(MinecraftWiki.parseItem(searchList2[2].url))

    val searchList3 = MinecraftWiki.searchList("火红莲", Filter.COURSE_OF_STUDY)
    println(searchList3)
    println(MinecraftWiki.parseCourseOfStudy(searchList3[0].url))
}
