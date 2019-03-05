package tomasvolker.kr

import tomasvolker.numeriko.core.dsl.I
import kotlin.math.absoluteValue


data class QRPattern(val x: Int, val y: Int, val unit: Int)

fun QRPattern.invertAxis() = QRPattern(y, x, unit)

fun QRPattern.isSame(other: QRPattern) =
    (unit == other.unit &&
            (other.x - 1 == x || other.x + 1 == x && other.y == y) ||
            (other.y - 1 == y || other.y + 1 == y && other.x == x))

fun QRPattern.center(axis: Int = 0) =
    if (axis == 0) QRPattern(x - 1 * unit, y, unit) else QRPattern(x, y - 1 * unit, unit)

data class LineSection(val value: Boolean, val range: IntRange)

val LineSection.unit get() = range.last - range.first

val LineSection.center get() = (range.first + range.last) / 2

fun LineSection.isNeighbor(other: LineSection) =
    (range.first == other.range.last) || (range.last == other.range.first)

fun List<LineSection>.isPattern(tolerance: Double) =
    !this[0].value && this[1].unit.inTolerance(this[0].unit, tolerance)
            && this[3].unit.inTolerance(this[0].unit, tolerance)
            && this[4].unit.inTolerance(this[0].unit, tolerance)
            && this[2].unit.inTolerance(this[0].unit * 3, tolerance)


fun List<Boolean>.toLineSections(axisIndex: Int = 0): List<LineSection> =
    this[0].let {
        this.toList()
            .sections()
            .toList()
    }

fun <T> List<T>.interleaved(overlap: Int): Sequence<List<T>> = sequence {
    for (i in 0 until size - overlap) {
        yield(subList(i, i + overlap))
    }
}

fun List<Boolean>.sections(): Sequence<LineSection> = sequence {
    var current = first()
    var lastIndex = 0

    forEachIndexed { index, b ->
        if (b != current) {
            yield(LineSection(current,lastIndex until index))
            lastIndex = index
            current = b
        }
    }
}

fun Int.inTolerance(center: Int, tolerance: Double) =
    (this > center - center * tolerance) && (this < center + center * tolerance)

fun Int.inRange(center: Int, deviation: Int): Boolean =
    (this - center).absoluteValue < deviation
