package tomasvolker.kr.boofcv

import boofcv.alg.misc.ImageStatistics.*
import boofcv.struct.image.*

fun GrayI<*>.min(): Int = when(this) {
    is GrayU8 -> min(this)
    is GrayU16 -> min(this)
    is GrayS8 -> min(this)
    is GrayS16 -> min(this)
    is GrayS32 -> min(this)
    else -> error("Invalid image type: ${this::class.java}")
}

fun GrayS64.min(): Long = min(this)
fun GrayF32.min(): Float = min(this)
fun GrayF64.min(): Double = min(this)

fun GrayI<*>.max(): Int = when(this) {
    is GrayU8 -> max(this)
    is GrayU16 -> max(this)
    is GrayS8 -> max(this)
    is GrayS16 -> max(this)
    is GrayS32 -> max(this)
    else -> error("Invalid image type: ${this::class.java}")
}

fun GrayS64.max(): Long = min(this)
fun GrayF32.max(): Float = min(this)
fun GrayF64.max(): Double = min(this)

fun GrayI<*>.mean(): Double = when(this) {
    is GrayU8 -> mean(this)
    is GrayU16 -> mean(this)
    is GrayS8 -> mean(this)
    is GrayS16 -> mean(this)
    is GrayS32 -> mean(this)
    else -> error("Invalid image type: ${this::class.java}")
}

fun GrayS64.mean(): Double = mean(this)
fun GrayF32.mean(): Float = mean(this)
fun GrayF64.mean(): Double = mean(this)

fun GrayF64.histogram() {



    histogram(this, 0.0, IntArray(10))

}
