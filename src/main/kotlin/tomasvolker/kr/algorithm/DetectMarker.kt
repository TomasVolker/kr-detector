package tomasvolker.kr.algorithm

import boofcv.struct.image.GrayU8
import org.openrndr.math.Vector2
import tomasvolker.numeriko.core.primitives.squared
import tomasvolker.openrndr.math.primitives.d
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

val QrPattern.position get() = Vector2(x.d, y.d)

fun GrayU8.detectVerticalQrPatterns(): List<QrPattern> {

    val scanner = LinePatternScanner(
        pattern = doubleArrayOf(1.0, 1.0, 3.0, 1.0, 1.0),
        startsWith = false,
        tolerance = 0.5,
        minSize = 2
    )

    val result = mutableListOf<QrPattern>()

    for (x in 0 until width) {

        scanner.reset()

        for (y in 0 until height) {
            scanner.nextValue(this[x, y]).let { unit ->
                if (unit != 0.0) {
                    result.add(
                        QrPattern(x, (y - (7 * unit) / 2).roundToInt(), unit, QrPattern.Direction.VERTICAL)
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
        minSize = 2
    )

    val result = mutableListOf<QrPattern>()

    for (y in 0 until height) {

        scanner.reset()

        for (x in 0 until width) {
            scanner.nextValue(this[x, y]).let { unit ->
                if (unit != 0.0) {
                    result.add(
                        QrPattern((x - (7 * unit) / 2).roundToInt(), y, unit, QrPattern.Direction.HORIZONTAL)
                    )
                }
            }
        }

    }

    return result
}

fun QrPattern.distanceSquaredTo(other: QrPattern): Int =
    (other.x - this.x).squared() + (other.y - this.y).squared()

fun GrayU8.detectQrPatterns(): List<QrPattern> {

    val horizontal = detectHorizontalQrPatterns()
    if (horizontal.isEmpty()) return emptyList()

    val vertical = detectVerticalQrPatterns()
    if (vertical.isEmpty()) return emptyList()

    // Kd tree

    return horizontal
        .asSequence()
        .map { hPattern ->
            hPattern to (vertical.minBy { it.distanceSquaredTo(hPattern) } ?: error(""))
        }
        .filter { it.first.distanceSquaredTo(it.second) < 7.0.squared() }
        .map {
            QrPattern(
                x = (it.first.x + it.second.x) / 2,
                y = (it.first.y + it.second.y) / 2,
                unit = it.first.unit,
                direction = QrPattern.Direction.HORIZONTAL
            )
        }.toList()
}