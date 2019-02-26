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
            shadow[x, y] = ColorRGBa.fromHex(image.getRGB(x, y))
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
            shadow.write(x, y, value, value, value, 1.0)
        }
    }
    shadow.upload()

}