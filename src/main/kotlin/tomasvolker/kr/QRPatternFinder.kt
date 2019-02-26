package tomasvolker.kr

import com.github.tomasvolker.parallel.mapParallel
import tomasvolker.numeriko.core.dsl.I
import tomasvolker.numeriko.core.interfaces.array1d.double.DoubleArray1D
import tomasvolker.numeriko.core.interfaces.array1d.integer.IntArray1D
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D


fun Int.inTolerance(center: Int, tolerance: Double) =
    (this > center - center * tolerance) && (this < center + center * tolerance)

data class QRPattern(val x: Int, val y: Int, val unit: Int)

fun QRPattern.invertAxis() = QRPattern(y, x, unit)

data class LineSection(val value: Boolean, val rangeX: IntArray1D, val rangeY: IntArray1D)

val LineSection.unit get() = if (rangeX.size != 1) rangeX[1] - rangeX[0] else rangeY[1] - rangeY[0]

val LineSection.centerX get() = if (rangeX.size > 1) (rangeX[0] + rangeX[1]) / 2 else rangeX[0]

val LineSection.centerY get() = if (rangeY.size > 1) (rangeY[0] + rangeY[1]) / 2 else rangeY[0]

fun LineSection.invertAxis() = LineSection(value, rangeY, rangeX)

fun LineSection.isNeighbor(other: LineSection) =
    (rangeX[0] == other.rangeX[1]) || (rangeX[1] == other.rangeX[0]) ||
            (rangeY[0] == other.rangeY[1]) || (rangeY[1] == other.rangeY[0])

fun List<LineSection>.isPattern(tolerance: Double) =
    (this[0].value && this[1].unit.inTolerance(this[0].unit, tolerance)
            && this[3].unit.inTolerance(this[0].unit, tolerance)
            && this[4].unit.inTolerance(this[0].unit, tolerance)
            && this[2].unit.inTolerance(this[0].unit * 3, tolerance))

fun DoubleArray1D.toLineSections(from: Int = 0, axisIndex: Int = 0): List<LineSection> {
    return if (size == 0)
        listOf<LineSection>()
    else {
        indexOfFirst { it != this[0] }.let {
            listOf(LineSection(this[0] == 1.0,
                I[from,this.indexOfFirst { it != this[0] }],
                I[axisIndex])) + this[this.indexOfFirst { it != this[0] } until size].toLineSections(it, axisIndex)
        }
    }
}

class QRPatternFinder() {

    fun findPatterns(image: DoubleArray2D): List<QRPattern> {
        val width = image.shape0
        val height = image.shape1
        val qrPatternList = mutableListOf<QRPattern>()

        TODO("scan X and Y and see what happens")
    }

    fun scanImage(lines: List<DoubleArray1D>, tolerance: Double = 0.2): List<QRPattern> {
        val qrPatternList = mutableListOf<QRPattern>()

        lines.mapIndexed { index, line ->
            line.toLineSections(axisIndex = index)
                .chunked(5)
                .map {  // make it with a 4 overlap...
                    if (it.isPattern(tolerance))
                        qrPatternList.add(QRPattern(it[2].centerX, it[2].centerY, it[0].unit))
                }
        }

        TODO("filter the values that are repeated")
    }
}