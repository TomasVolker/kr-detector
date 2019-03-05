package tomasvolker.kr.algorithm

import boofcv.struct.image.GrayU8
import boofcv.struct.image.ImageBase
import tomasvolker.kr.geometry.Point
import tomasvolker.numeriko.core.interfaces.array1d.double.DoubleArray1D
import tomasvolker.numeriko.core.operations.concatenate
import kotlin.math.absoluteValue

val ImageBase<*>.center get() = Point(width / 2, height / 2)

fun rangeAround(value: Int, delta: Int) = (value-delta)..(value+delta)

fun GrayU8.paddedSubImage(
    xRange: IntRange,
    yRange: IntRange,
    padding: Int = 0,
    destination: GrayU8? = null
): GrayU8 {
    val result = destination ?: createNew(xRange.count(), yRange.count())

    val firstX = xRange.first
    val firstY = yRange.first

    for (y in yRange) {
        val localY = y - firstY
        for (x in xRange) {
            val localX = x - firstX
            result[localX, localY] = if (isInBounds(x, y)) this[x, y] else padding
        }
    }

    return result
}

fun <T> List<T>.allPairsSequence(): Sequence<Pair<T, T>> = sequence {

    for (i in indices) {
        for (j in (i+1) until size) {
            yield(get(i) to get(j))
        }
    }

}

fun Int.inTolerance(center: Int, tolerance: Double) =
    (this > center - center * tolerance) && (this < center + center * tolerance)

fun Int.inRange(center: Int, deviation: Int): Boolean =
    (this - center).absoluteValue < deviation


inline fun <T> List<T>.minDouble(selector: (T)->Double): Double {
    var result = Double.MAX_VALUE
    forEach {
        val value = selector(it)
        if (value < result)
            result = value
    }
    return result
}

inline fun <T> List<T>.maxDouble(selector: (T)->Double): Double {
    var result = Double.MIN_VALUE
    forEach {
        val value = selector(it)
        if (value > result)
            result = value
    }
    return result
}

fun Double.inRange(center: Double, deviation: Double): Boolean =
    (this - center).absoluteValue < deviation

fun <T> List<T>.mode(): T {
    val list = mutableListOf<T>().apply { this.addAll(this@mode) }
    var mode: T = this[0]
    var modeCount = 0

    for (i in 0 until size) {
        val currCount = list.count { it == this[i] }

        if (currCount > modeCount) {
            modeCount = currCount
            mode = this[i]
        }

        list.replace(list.filter { it != this[i] })
    }

    return mode
}

fun QrPattern.isNeighbor(other: QrPattern, deviation: Int = 3) =
    x.inRange(other.x, deviation) && y.inRange(other.y, deviation)

fun List<QrPattern>.hasNeighbors(nNeighbors: Int = 10,deviation: Int = 4): List<QrPattern> {
    val list = mutableListOf<QrPattern>()

    for (i in 0 until size) {
        if (this.mapIndexed { index, qrMarker ->
                if (index != i) qrMarker.isNeighbor(this[i], deviation) else false }.count() > nNeighbors)
            list.add(this[i])
    }

    return list.toList()
}

fun List<QrPattern>.centered() =
    map { QrPattern(it.x - 2 * it.unitX.toInt(), it.y - 2 * it.unitY.toInt(), it.unitX, it.unitY) }

fun List<DoubleArray1D>.flatten() =
    reduce { acc, array -> acc.concatenate(array) }