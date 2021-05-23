package top.limbang

fun main() {
//    RequestSupport.downloadImage(
//        "https://i.mcmod.cn/editor/upload/20180421/1524303371_7926_D5Bw.png",
//        File("data/top.limbang.mirai-console-mcmod-plugin/img/1524303371_7926_D5Bw.png")
//    )

   val img =  "<img data-src=\"https://i.mcmod.cn/editor/upload/20180421/1524304430_7926_v4GC.png\">"
    print(img.substringBetween("<img data-src=\"","\">"))
}