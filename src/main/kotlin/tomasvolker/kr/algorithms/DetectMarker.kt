package tomasvolker.kr.algorithms

import boofcv.struct.image.GrayU8
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class LinePatternScanner(
    val pattern: DoubleArray,
    val startsWith: Boolean,
    val tolerance: Double = 0.2,
    val minSize: Int = 5
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

data class QrMarker(
    val x: Int,
    val y: Int,
    val unit: Double
)

fun GrayU8.detectQrMarkers(): List<QrMarker> {

    val scanner = LinePatternScanner(
        pattern = doubleArrayOf(1.0, 1.0, 3.0, 1.0, 1.0),
        startsWith = false,
        tolerance = 0.2,
        minSize = 5
    )

    val result = mutableListOf<QrMarker>()

    for (y in 0 until height) {

        scanner.reset()

        for (x in 0 until width) {
            scanner.nextValue(this[x, y]).let { unit ->
                if (unit != 0.0) {
                    result.add(
                        QrMarker((x - (7 * unit) / 2).roundToInt(), y, unit)
                    )
                }
            }
        }

    }

    for (x in 0 until width) {

        scanner.reset()

        for (y in 0 until height) {
            scanner.nextValue(this[x, y]).let { unit ->
                if (unit != 0.0) {
                    result.add(
                        QrMarker(x, (y - (7 * unit) / 2).roundToInt(), unit)
                    )
                }
            }
        }

    }

    return result
}