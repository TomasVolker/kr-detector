package tomasvolker.kr.algorithm

import boofcv.struct.image.GrayU8
import boofcv.struct.image.ImageBase
import tomasvolker.kr.geometry.Point

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

