package tomasvolker.kr.openrndr

import org.openrndr.math.Vector2
import java.lang.IndexOutOfBoundsException

infix fun Vector2.cross(other: Vector2): Double =
        this.x * other.y - this.y * other.x

fun List<Vector2>.average(): Vector2 {
    var x = 0.0
    var y = 0.0
    forEach {
        x += it.x
        y += it.y
    }
    return Vector2(
        x = x / size,
        y = y / size
    )
}

operator fun Vector2.get(i: Int): Double = when(i) {
    0 -> x
    1 -> y
    else -> throw IndexOutOfBoundsException("$i")
}