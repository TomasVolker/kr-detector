package tomasvolker.kr.algorithms

import boofcv.struct.image.GrayU8
import boofcv.struct.image.ImageBase
import java.util.*

data class Point(
    val x: Int,
    val y: Int
)

fun Point.neighboors4() = listOf(
    Point(x-1, y),
    Point(x, y-1),
    Point(x+1, y),
    Point(x, y+1)
)

fun Point.neighboors8() = listOf(
    Point(x-1, y-1),
    Point(x  , y-1),
    Point(x+1, y-1),
    Point(x-1, y  ),
    Point(x+1, y  ),
    Point(x-1, y+1),
    Point(x  , y+1),
    Point(x+1, y+1)
)

fun ImageBase<*>.isInBounds(point: Point) = isInBounds(point.x, point.y)

operator fun GrayU8.get(point: Point) = this[point.x, point.y]
operator fun GrayU8.set(point: Point, value: Int) { this[point.x, point.y] = value }

fun GrayU8.reconstruct(
    seed: Point,
    neighborhood: (Point)->List<Point> = Point::neighboors8,
    destination: GrayU8? = null
) = reconstruct(
    seeds = listOf(seed),
    neighborhood = neighborhood,
    destination = destination
)

fun GrayU8.reconstruct(
    seeds: List<Point>,
    neighborhood: (Point)->List<Point> = Point::neighboors8,
    destination: GrayU8? = null
): GrayU8 {

    val result = destination ?: createSameShape()

    val frontier = ArrayDeque<Point>()

    seeds.forEach {
        result[it] = 1
        frontier.add(it)
    }

    while(frontier.isNotEmpty()) {

        val value = frontier.pop()

        for(neighbor in neighborhood(value)) {
            if (isInBounds(neighbor) && result[neighbor] == 0 && this[neighbor] != 0) {
                result[neighbor] = 1
                frontier.add(neighbor)
            }
        }

    }

    return result
}