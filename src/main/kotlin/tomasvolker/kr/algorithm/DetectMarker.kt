package tomasvolker.kr.algorithm

import boofcv.struct.image.GrayU8
import org.ddogleg.nn.FactoryNearestNeighbor
import org.ddogleg.nn.NnData
import org.ddogleg.nn.alg.KdTreeDistance
import tomasvolker.kr.geometry.Point
import tomasvolker.numeriko.core.primitives.squared
import tomasvolker.openrndr.math.primitives.d
import java.lang.IndexOutOfBoundsException
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class LinePatternScanner(
    val pattern: DoubleArray,
    val startsWith: Boolean,
    val tolerance: Double = 0.3,
    val minSize: Int = 2
) {

    // If zero, match any
    var patternUnit = 0.0
    var patternIndex = 0

    var currentValue = false
    var currentCount = 0

    fun reset() {
        patternUnit = 0.0
        patternIndex = 0

        currentValue = false
        currentCount = 0
    }

    fun nextValue(value: Int) = nextValue(value != 0)

    fun nextValue(value: Boolean): Double {

        if (value == currentValue) {
            currentCount++
        } else {
            if (patternIndex == 0) {
                if (currentValue == startsWith) {
                    patternUnit = currentCount / pattern[0]
                    patternIndex = 1
                } else {
                    patternUnit = 0.0
                    patternIndex = 0
                }
            } else {

                if (currentCount < minSize) {

                    patternUnit = 0.0
                    patternIndex = 0

                } else {

                    val target = (pattern[patternIndex] * patternUnit)
                    if (((currentCount - target).absoluteValue) / target < tolerance) {
                        patternIndex++
                        if (patternIndex >= pattern.size) {
                            val result = patternUnit
                            patternUnit = 0.0
                            patternIndex = 0
                            currentValue = value
                            currentCount = 1
                            return result
                        }
                    } else {
                        patternUnit = currentCount / pattern[0]
                        patternIndex = 1
                    }

                }


            }
            currentValue = value
            currentCount = 1
        }

        return 0.0
    }

}

data class QrPattern(
    val x: Int,
    val y: Int,
    val unitX: Double,
    val unitY: Double
)

fun GrayU8.detectVerticalQrPatterns(): List<QrPattern> {

    val scanner = LinePatternScanner(
        pattern = doubleArrayOf(1.0, 1.0, 3.0, 1.0, 1.0),
        startsWith = false,
        tolerance = 0.1,
        minSize = 10
    )

    val result = mutableListOf<QrPattern>()

    for (x in 0 until width) {

        scanner.reset()

        for (y in 0 until height) {
            scanner.nextValue(this[x, y]).let { unit ->
                if (unit != 0.0) {
                    result.add(
                        QrPattern(x, (y - (7 * unit) / 2).roundToInt(), unit, unit)
                    )
                }
            }
        }

    }

    return result
}

fun GrayU8.detectHorizontalQrPatterns(): List<QrPattern> {

    val scanner = LinePatternScanner(
        pattern = doubleArrayOf(1.0, 1.0, 3.0, 1.0, 1.0),
        startsWith = false,
        tolerance = 0.5,
        minSize = 1
    )

    val result = mutableListOf<QrPattern>()

    for (y in 0 until height) {

        scanner.reset()

        for (x in 0 until width) {
            scanner.nextValue(this[x, y]).let { unit ->
                if (unit != 0.0) {
                    result.add(
                        QrPattern((x - (7 * unit) / 2).roundToInt(), y, unit, unit)
                    )
                }
            }
        }

    }

    return result
}

fun GrayU8.detectQrPatterns(): List<QrPattern> {

    val horizontal = detectHorizontalQrPatterns()
    val vertical = detectVerticalQrPatterns()

    val distance = object: KdTreeDistance<Point> {
        override fun length(): Int = 2

        override fun distance(a: Point, b: Point): Double =
            ((a.x - b.x).squared() + (a.y - b.y).squared()).d

        override fun valueAt(point: Point, index: Int): Double =
            when(index) {
                0 -> point.x.d
                1 -> point.y.d
                else -> throw IndexOutOfBoundsException(index)
            }

    }

    val nn = FactoryNearestNeighbor.kdtree<Point>(distance)

    nn.setPoints(vertical.map { Point(it.x, it.y) }, true)
    val result = NnData<Point>()

    return horizontal.filter {
        nn.findNearest(Point(it.x, it.y), (it.unitX).squared(), result)
    }
}