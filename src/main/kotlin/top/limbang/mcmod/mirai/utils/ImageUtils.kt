package top.limbang.mcmod.mirai.utils

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * ### 按照指定的尺寸缩放图片
 * @param width 宽
 * @param height 高
 */
fun File.zoomBySize(width: Int, height: Int = width): Boolean {
    val bufferedImage = ImageIO.read(this)
    if (width == bufferedImage.width && height == bufferedImage.height) return true
    val formatName = if (name.indexOf(".") != -1) name.substringAfter(".") else "png"
    val zoomImage = bufferedImage.getScaledInstance(width, height, Image.SCALE_DEFAULT)
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val g2 = image.graphics
    g2.drawImage(zoomImage, 0, 0, null)
    g2.dispose()
    return ImageIO.write(image, formatName, this)
}