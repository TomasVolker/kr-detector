package tomasvolker.kr

import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.operations.unstack
import kotlin.math.absoluteValue


fun <T> List<T>.split(times: Int = 2) = this.chunked(times)

class AdaptiveLightBalancer(val sectionCount: Int,
                            val maxCount: Int,
                            val diffThreshold: Double,
                            val brightnessThreshold: Double) {

    fun filter(input: DoubleArray2D): DoubleArray2D {
        val sectionLevels = input.unstack().map {
            it.chunked(sectionCount)
                .map {
                    it.sorted()
                        .take(maxCount)
                        .average() to it
                }
        }.toMutableList()

        val diffLevels = List(sectionCount) { i ->
            List(input.shape1) { j ->
                (if (j == 0)
                    0.0
                else
                    (sectionLevels[i][j].first - sectionLevels[i][j - 1].first).absoluteValue) to sectionLevels[i][j]
            }
        }

        TODO("split levels if diff > threshold")
        TODO("linear interpolation")
        TODO("automatic gain if below a threshold")
        TODO("threshold in the middle of gray level")
    }
}