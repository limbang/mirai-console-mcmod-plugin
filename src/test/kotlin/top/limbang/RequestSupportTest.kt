package top.limbang

import java.io.File

fun main() {
    RequestSupport.downloadImage(
        "https://i.mcmod.cn/editor/upload/20180421/1524303371_7926_D5Bw.png",
        File("data/top.limbang.mirai-console-mcmod-plugin/img/1524303371_7926_D5Bw.png")
    )
}