package top.limbang.mcmod

import top.limbang.Filter
import top.limbang.MinecraftWiki


fun main() {
    println(MinecraftWiki.searchList("植物魔法",Filter.MODULE))
    println(MinecraftWiki.searchList("植物魔法",Filter.DATA))
    println(MinecraftWiki.searchList("植物魔法",Filter.COURSE_OF_STUDY))
}
