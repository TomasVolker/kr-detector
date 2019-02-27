package tomasvolker.kr.boofcv

import boofcv.alg.filter.binary.GThresholdImageOps
import boofcv.struct.ConfigLength
import boofcv.struct.image.*

fun <T: ImageGray<T>> T.threshold(
    threshold: Double,
    destination: GrayU8? = null,
    down: Boolean = true
): GrayU8 =
    GThresholdImageOps.threshold(
        this,
        destination,
        threshold,
        down
    ) ?: error("null image")

fun <T: ImageGray<T>> T.localMeanThreshold(
    size: Double,
    destination: GrayU8? = null,
    scale: Double = 1.0,
    down: Boolean = true,
    work1: T? = null,
    work2: T? = null
): GrayU8 =
    GThresholdImageOps.localMean(
        this,
        destination,
        ConfigLength.fixed(size),
        scale,
        down,
        work1,
        work2
    )

fun <T: ImageGray<T>> T.localMeanThreshold(
    size: Size,
    destination: GrayU8? = null,
    scale: Double = 1.0,
    down: Boolean = true,
    work1: T? = null,
    work2: T? = null
): GrayU8 =
    GThresholdImageOps.localMean(
        this,
        destination,
        size.toConfigLength(),
        scale,
        down,
        work1,
        work2
    )

sealed class Size

data class Fixed(val pixels: Double): Size()
data class Relative(
    val ratio: Double,
    val minimum: Int
): Size()

fun Size.toConfigLength() = when(this) {
    is Fixed -> ConfigLength.fixed(pixels)
    is Relative -> ConfigLength.relative(ratio, minimum)
}
