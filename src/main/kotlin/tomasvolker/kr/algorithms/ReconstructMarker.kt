package tomasvolker.kr.algorithms

import boofcv.struct.image.GrayU8
import boofcv.struct.image.ImageBase
import io.lacuna.artifex.Vec
import org.openrndr.math.Vector2
import tomasvolker.kr.boofcv.nonZeroPoints
import tomasvolker.numeriko.core.primitives.squared
import tomasvolker.openrndr.math.primitives.d
import java.util.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class MarkerCorners(
    val position: Vector2,
    val corners: List<Vector2>
)

val ImageBase<*>.center get() = Point(width / 2, height / 2)

fun rangeAround(value: Int, delta: Int) = (value-delta)..(value+delta)

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

fun GrayU8.paddedSubImage(
    xRange: IntRange,
    yRange: IntRange,
    padding: Int = 0,
    destination: GrayU8? = null
): GrayU8 {
    val result = destination ?: createNew(xRange.count(), yRange.count())

    val firstX = xRange.first
    val firstY = yRange.first

    for (y in yRange) {
        val localY = y - firstY
        for (x in xRange) {
            val localX = x - firstX
            result[localX, localY] = if (isInBounds(x, y)) this[x, y] else padding
        }
    }

    return result
}

fun GrayU8.reconstructMarker(
    pattern: QrPattern,
    neighborhood: (Point)->List<Point> = Point::neighboors4,
    destination: GrayU8? = null
): MarkerCorners {

    val rangeX = rangeAround(pattern.x, (10 * pattern.unitX).roundToInt())
    val rangeY = rangeAround(pattern.y, (10 * pattern.unitY).roundToInt())

    val topLeft = Vector2(rangeX.first.d, rangeY.first.d)

    val image = paddedSubImage(rangeX, rangeY)

    val smallSquare = image.reconstruct(image.center, neighborhood, invertImage = true).nonZeroPoints()
    val mediumSquare = image.reconstruct(smallSquare, neighborhood).nonZeroPoints()
    val outerSquare = image.reconstruct(mediumSquare, neighborhood, invertImage = true, destination = destination).nonZeroPoints()
    // Check if outer square reaches border

    return MarkerCorners(
        position = topLeft + outerSquare.average(),
        corners = outerSquare.estimateCorners().map { topLeft + it }
    )
}

fun List<Point>.estimateCorners(): List<Vector2> {

    val result = listOf(
        maxBy { it.x } ?: error("empty list"),
        minBy { it.x } ?: error("empty list"),
        maxBy { it.y } ?: error("empty list"),
        minBy { it.y } ?: error("empty list"),
        maxBy { it.x + it.y } ?: error("empty list"),
        minBy { it.x + it.y } ?: error("empty list"),
        maxBy { it.x - it.y } ?: error("empty list"),
        minBy { it.x - it.y } ?: error("empty list")
    )

    return result.map { it.toVector2() }
}

fun <T> List<T>.allPairsSequence(): Sequence<Pair<T, T>> = sequence {

    for (i in indices) {
        for (j in (i+1) until size) {
            yield(get(i) to get(j))
        }
    }

}

infix fun Vector2.cross(other: Vector2): Double =
        this.x * other.y - this.y * other.x

fun List<MarkerCorners>.estimateDiagonal(): Pair<MarkerCorners, MarkerCorners> =
    allPairsSequence().minBy { (marker1, marker2) ->

        val corners = marker1.corners + marker2.corners
        // Connection projection
        val projection = (marker2.position - marker1.position).normalized

        // Minimum corner distance to centroid connection connection
        corners
            .map { (projection cross (it - marker1.position)).squared() }
            .min() ?: Double.POSITIVE_INFINITY
    } ?: error("impossible state")

fun List<MarkerCorners>.sortMarkers(): List<MarkerCorners> {

    val (d1, d2) = estimateDiagonal()

    val offDiagonal = find { it != d1 && it != d2 } ?: error("list doesn't contain 3 elements")

    return if ((offDiagonal.position - d1.position) cross (d2.position - d1.position) > 0.0)
        listOf(d1, offDiagonal, d2)
    else
        listOf(d2, offDiagonal, d1)

}