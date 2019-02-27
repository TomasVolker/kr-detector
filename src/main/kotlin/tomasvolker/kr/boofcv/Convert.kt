package tomasvolker.kr.boofcv

import boofcv.gui.binary.VisualizeBinaryData
import boofcv.io.image.ConvertBufferedImage
import boofcv.struct.image.*
import java.awt.image.BufferedImage

inline fun <reified T: ImageInterleaved<T>> BufferedImage.convertToInterleaved(
    destination: T,
    orderRgb: Boolean = true
): T {
    ConvertBufferedImage.convertFromInterleaved(this, destination, orderRgb)
    return destination
}

inline fun <reified T: ImageGray<T>> BufferedImage.convertToPlanar(
    destination: Planar<T>? = null,
    orderRgb: Boolean = true
): Planar<T> = ConvertBufferedImage.convertFromPlanar(this, destination, orderRgb, T::class.java)

inline fun <reified T: ImageGray<T>> BufferedImage.convertToSingle(destination: T? = null): T =
    ConvertBufferedImage.convertFromSingle(this, destination, T::class.java)

fun BufferedImage.toGrayF32(destination: GrayF32? = null): GrayF32 =
    ConvertBufferedImage.convertFrom(this, destination)

fun GrayU8.toBufferedImage(
    invert: Boolean = false,
    destination: BufferedImage? = null): BufferedImage =
    VisualizeBinaryData.renderBinary(this, invert, destination)

operator fun <T: ImageBase<T>> T.get(x: IntRange, y: IntRange): T =
    subimage(
        x.start,
        y.start,
        x.endInclusive+1,
        y.endInclusive+1
    )

operator fun <B: ImageGray<B>, T: Planar<B>> T.get(band: Int): B = getBand(band)

inline fun <reified T: ImageGray<T>> ImageGray<*>.createSameShapeOf(): T =
        createSameShape(T::class.java)