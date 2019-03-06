package tomasvolker.kr.algorithm

import java.util.*

data class LineSection(val value: Boolean, val range: IntRange)
val LineSection.unit get() = range.last - range.first
val LineSection.center get() = (range.first + range.last) / 2

fun List<LineSection>.isPattern(tolerance: Double): Boolean {
    if (size != 5) return false

    val unit = this[0].unit

    return !this[0].value &&
            this[1].unit.inTolerance(unit, tolerance) &&
            this[2].unit.inTolerance(3 * unit, tolerance) &&
            this[3].unit.inTolerance(unit, tolerance) &&
            this[4].unit.inTolerance(unit, tolerance)
}

fun <T> List<T>.interleaved(length: Int): Sequence<List<T>> = sequence {
    for (i in 0 until size - length) {
        yield(subList(i, i + length))
    }
}

fun <T> Sequence<T>.interleaved(length: Int): Sequence<List<T>> = sequence {
    val input = this@interleaved.iterator()

    val state = ArrayDeque<T>()

    repeat(length) {

        if (input.hasNext())
            state.addLast(input.next())
        else
            return@sequence

    }

    yield(state.toList())

    while (input.hasNext()) {
        state.pop()
        state.addLast(input.next())
        yield(state.toList())
    }
}

fun List<Boolean>.sections(): Sequence<LineSection> = sequence {
    var current = first()
    var lastIndex = 0

    forEachIndexed { index, b ->
        if (b != current) {
            yield(LineSection(current, lastIndex until index))
            lastIndex = index
            current = b
        }
    }
}

fun Sequence<Boolean>.sections(): Sequence<LineSection> = sequence {
    val input = this@sections.iterator()

    if (!input.hasNext()) return@sequence

    var current = input.next()
    var lastIndex = 0
    var currentIndex = 0

    while (input.hasNext()) {
        val next = input.next()
        currentIndex++

        if (next != current) {
            yield(LineSection(current, lastIndex until currentIndex))
            lastIndex = currentIndex
            current = next
        }

    }

}
