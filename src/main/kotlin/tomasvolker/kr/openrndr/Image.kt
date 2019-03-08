package tomasvolker.kr.openrndr

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import java.awt.image.BufferedImage

fun ColorRGBa.Companion.gray(value: Double) = ColorRGBa.WHITE.shade(value)

fun ColorBuffer.write(image: BufferedImage) {

    require(
        this.width == image.width &&
        this.height == image.height
    )

    shadow.buffer.rewind()

    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = image.getRGB(x, y)

            val alpha = (color and 0x00FFFFFF.inv()) shr 24
            val red = (color and 0x00FF0000) shr 16
            val green = (color and 0x0000FF00) shr 8
            val blue = (color and 0x000000FF)

            shadow.write(
                x,
                y,
                red / 255.0,
                green / 255.0,
                blue / 255.0,
                1 - alpha / 255.0
            )
        }
    }
    shadow.upload()

}

fun ColorBuffer.write(image: DoubleArray2D) {

    require(
        this.width == image.shape0 &&
        this.height == image.shape1
    )

    shadow.buffer.rewind()

    for (y in 0 until height) {
        for (x in 0 until width) {
            val value = image[x, y]
            shadow.write(
                x = x,
                y = y,
                r = value,
                g = value,
                b = value,
                a = 1.0
            )
        }
    }
    shadow.upload()

}