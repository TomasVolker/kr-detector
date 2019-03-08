package tomasvolker.kr.geometry

import boofcv.struct.image.GrayU8
import boofcv.struct.image.ImageBase
import org.openrndr.math.Vector2
import tomasvolker.openrndr.math.primitives.d

data class Point(
    val x: Int,
    val y: Int
)

fun Point.toVector2() = Vector2(x.d, y.d)

operator fun Point.plus(other: Point) =
    Point(this.x + other.x, this.y + other.y)
operator fun Point.minus(other: Point) =
    Point(this.x - other.x, this.y - other.y)

fun Point.neighboors4() = listOf(
    Point(x - 1, y),
    Point(x, y - 1),
    Point(x + 1, y),
    Point(x, y + 1)
)

fun Point.neighboors8() = listOf(
    Point(x - 1, y - 1),
    Point(x, y - 1),
    Point(x + 1, y - 1),
    Point(x - 1, y),
    Point(x + 1, y),
    Point(x - 1, y + 1),
    Point(x, y + 1),
    Point(x + 1, y + 1)
)

fun ImageBase<*>.isInBounds(point: Point) = isInBounds(point.x, point.y)

operator fun GrayU8.get(point: Point) = this[point.x, point.y]
operator fun GrayU8.set(point: Point, value: Int) { this[point.x, point.y] = value }

fun Iterable<Point>.average(): Vector2 {
    var x = 0
    var y = 0
    var count = 0
    forEach {
        x += it.x
        y += it.y
        count++
    }
    return Vector2(x / count.d, y / count.d)
}