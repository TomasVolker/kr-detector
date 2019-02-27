package tomasvolker.kr

import tomasvolker.numeriko.core.dsl.I
import tomasvolker.numeriko.core.interfaces.array1d.double.DoubleArray1D
import tomasvolker.numeriko.core.interfaces.array1d.integer.IntArray1D


data class QRPattern(val x: Int, val y: Int, val unit: Int)

fun QRPattern.invertAxis() = QRPattern(y, x, unit)

fun QRPattern.isSame(other: QRPattern) =
    (unit == other.unit &&
            (other.x - 1 == x || other.x + 1 == x && other.y == y) ||
            (other.y - 1 == y || other.y + 1 == y && other.x == x))

fun QRPattern.center(axis: Int = 0) =
    if (axis == 0) QRPattern(x - 1 * unit, y, unit) else QRPattern(x, y - 1 * unit, unit)

data class LineSection(val value: Boolean, val rangeX: IntArray1D, val rangeY: IntArray1D)

val LineSection.unit get() = if (rangeX.size != 1) rangeX[1] - rangeX[0] else rangeY[1] - rangeY[0]

val LineSection.centerX get() = if (rangeX.size > 1) (rangeX[0] + rangeX[1]) / 2 else rangeX[0]

val LineSection.centerY get() = if (rangeY.size > 1) (rangeY[0] + rangeY[1]) / 2 else rangeY[0]

fun LineSection.invertAxis() = LineSection(value, rangeY, rangeX)

fun LineSection.isNeighbor(other: LineSection) =
    (rangeX[0] == other.rangeX[1]) || (rangeX[1] == other.rangeX[0]) ||
            (rangeY[0] == other.rangeY[1]) || (rangeY[1] == other.rangeY[0])

fun List<LineSection>.isPattern(tolerance: Double) =
    !this[0].value && this[1].unit.inTolerance(this[0].unit, tolerance)
            && this[3].unit.inTolerance(this[0].unit, tolerance)
            && this[4].unit.inTolerance(this[0].unit, tolerance)
            && this[2].unit.inTolerance(this[0].unit * 3, tolerance)


fun DoubleArray1D.toLineSections(from: Int = 0, axisIndex: Int = 0): List<LineSection> {
    return if (size < 2)
        emptyList()
    else {
        indexOfFirst { it != this[0] }.let {
            if (it == -1) emptyList()
            else {
                listOf(LineSection(this[0] == 1.0,I[from, it], I[axisIndex])) +
                        this[it until size - 1].toLineSections(it, axisIndex)
            }
        }
    }
}

fun <T> List<T>.interleaved(overlap: Int): Sequence<List<T>> = sequence {
    for (i in 0 until size - overlap) {
        yield(subList(i, i + overlap))
    }
}

fun Int.inTolerance(center: Int, tolerance: Double) =
    (this > center - center * tolerance) && (this < center + center * tolerance)
