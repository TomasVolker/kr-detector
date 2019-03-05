package tomasvolker.kr.algorithm.kdtree

import org.openrndr.shape.Rectangle
import tomasvolker.openrndr.math.*
import java.lang.IndexOutOfBoundsException
import kotlin.math.max
import kotlin.math.min

typealias DoubleRange = ClosedFloatingPointRange<Double>

val DoubleRange.min get() = if(isEmpty()) Double.NaN else start
val DoubleRange.max get() = if(isEmpty()) Double.NaN else endInclusive
val DoubleRange.length get() = if (isEmpty()) 0.0 else max - min
val DoubleRange.centroid get() = if(isEmpty()) Double.NaN else (min + max) / 2.0

val Rectangle.rangeList get() = listOf(left..right, top..bottom)
fun Rectangle.range(i: Int): DoubleRange  = when(i) {
    0 -> left..right
    1 -> top..bottom
    else -> throw IndexOutOfBoundsException(i)
}

fun Rectangle.Companion.fromRangeList(
    rangeList: List<DoubleRange>
): Rectangle {
    require(rangeList.size == 2)
    return Rectangle.fromSides(
        left = rangeList[0].min,
        right = rangeList[0].max,
        top = rangeList[1].min,
        bottom = rangeList[1].max
    )
}

infix fun DoubleRange.intersect(other: DoubleRange): DoubleRange =
    max(this.min, other.min) .. min(this.max, other.max)
