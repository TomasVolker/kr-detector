package tomasvolker.kr.algorithm

import boofcv.struct.image.GrayU8
import org.openrndr.math.Vector2
import tomasvolker.kr.boofcv.convertToSingle
import tomasvolker.kr.boofcv.localMeanThreshold
import tomasvolker.kr.boofcv.nonZeroPoints
import tomasvolker.kr.geometry.*
import tomasvolker.kr.openrndr.average
import tomasvolker.kr.openrndr.cross
import tomasvolker.numeriko.core.dsl.D
import tomasvolker.numeriko.core.functions.matMul
import tomasvolker.numeriko.core.functions.solve
import tomasvolker.numeriko.core.functions.transpose
import tomasvolker.numeriko.core.operations.concat
import tomasvolker.numeriko.core.operations.stack
import tomasvolker.numeriko.core.primitives.squared
import tomasvolker.openrndr.math.primitives.d
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.roundToInt

class QrDetector(
    val imageWidth: Int,
    val imageHeight: Int
) {

    val grayScale = GrayU8(imageWidth, imageHeight)
    val thresholded = GrayU8(imageWidth, imageHeight)

    var recognitions: List<QrPattern> = emptyList()

    var clusters: List<Set<QrPattern>> = emptyList()

    var filteredClusters: List<Set<QrPattern>> = emptyList()

    var recognizedMarkers: List<QrPattern> = emptyList()

    var rawMarkers: List<MarkerCorners> = emptyList()
        private set

    var sortedMarkers: List<MarkerCorners> = emptyList()
        private set

    var sortedCorners: List<MarkerCorners> = emptyList()
        private set

    var homography: Homography? = null
        private set

    val VERTICAL = QrPattern.Direction.VERTICAL
    val HORIZONTAL = QrPattern.Direction.HORIZONTAL

    fun detectQr(image: BufferedImage): Homography? {

        val thresholded = image
            .convertToSingle<GrayU8>(destination = grayScale)
            .localMeanThreshold(
                size = 50.0,
                scale = 0.95,
                down = false,
                destination = thresholded
            )

        recognitions = thresholded.scanForQrPattern(tolerance = 0.4)

        clusters = recognitions.cluster(distance = 1.0)
        filteredClusters = clusters
            .filter {
                it.size >= 15 &&
                it.count { it.direction == VERTICAL }.inTolerance(it.count { it.direction == HORIZONTAL }, 0.5)
            }

        recognizedMarkers = filteredClusters.map {
                val centroid = it.map { Vector2(it.x.d, it.y.d) }.average()

                val horizontalUnit = it
                    .asSequence()
                    .filter { it.direction == HORIZONTAL }
                    .map { it.unit }
                    .average()

                val verticalUnit = it
                    .asSequence()
                    .filter { it.direction == VERTICAL }
                    .map { it.unit }
                    .average()

                QrPattern(
                    x = centroid.x.roundToInt(),
                    y = centroid.y.roundToInt(),
                    unit = max(horizontalUnit, verticalUnit),
                    direction = QrPattern.Direction.HORIZONTAL
                )
            }

        if (recognizedMarkers.size != 3) {
            rawMarkers = emptyList()
            sortedMarkers = emptyList()
            sortedCorners = emptyList()
            homography = null
            return null
        }

        rawMarkers = recognizedMarkers.map {
            thresholded.reconstructMarker(it)
        }

        sortedMarkers = rawMarkers.sortedMarkers()
        sortedCorners = sortedMarkers.sortedCorners()
        homography = sortedCorners.computeHomography()

        return homography
    }

    fun GrayU8.reconstructMarker(
        pattern: QrPattern,
        neighborhood: (Point)->List<Point> = Point::neighboors4,
        destination: GrayU8? = null
    ): MarkerCorners {

        val rangeX = rangeAround(pattern.x, (10 * pattern.unit).roundToInt())
        val rangeY = rangeAround(pattern.y, (10 * pattern.unit).roundToInt())

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

    fun List<MarkerCorners>.sortedMarkers(): List<MarkerCorners> {

        val (d1, d2) = estimateDiagonal()

        val offDiagonal = find { it != d1 && it != d2 } ?: error("list doesn't contain 3 elements")

        return if ((offDiagonal.position - d1.position) cross (d2.position - d1.position) > 0.0)
            listOf(offDiagonal, d2, d1)
        else
            listOf(offDiagonal, d1, d2)

    }

    private fun MarkerCorners.sortCorners(
        verticalSplit: (Vector2)->Boolean,
        horizontalSplit: (Vector2)->Boolean
    ) = corners
        .map { it - position } // to local coordinates
        .partition(verticalSplit) // Vertical split
        .toList()
        .flatMap {
            it.partition(horizontalSplit) // Horizontal split
                .toList()
                .map { it.average() }
        }.let {
            MarkerCorners(
                position = position,
                corners = it.map { it + position } // to global coordinates
            )
        }

    fun List<MarkerCorners>.sortedCorners(): List<MarkerCorners> {

        val marker0 = this[0]
        val marker1 = this[1]
        val marker2 = this[2]

        val line0to1 = marker1.position - marker0.position
        val line1to2 = marker2.position - marker1.position
        val line2to0 = marker0.position - marker2.position

        val sortedMarker0 = marker0.sortCorners(
            verticalSplit = { line0to1 cross it < 0 },
            horizontalSplit = { line2to0 cross it < 0 }
        )

        val sortedMarker1 = marker1.sortCorners(
            verticalSplit = { line0to1 cross it < 0 },
            horizontalSplit = { line0to1 dot it < 0 }
        )

        val sortedMarker2 = marker2.sortCorners(
            verticalSplit = { line2to0 dot it > 0 },
            horizontalSplit = { line2to0 cross it < 0 }
        )

        return listOf(
            sortedMarker0,
            sortedMarker1,
            sortedMarker2
        )
    }

    fun List<MarkerCorners>.projectCorner(): Vector2 {

        val corner0 = this[0].corners[0]
        val corner1 = this[1].corners[1]
        val corner2 = this[2].corners[2]

        val bottomLine = Line.fromPointAndDirection(
            point = corner2,
            direction = this[2].corners[3] - corner2
        )

        val rightLine = Line.fromPointAndDirection(
            point = corner1,
            direction = this[1].corners[3] - corner1
        )

        return bottomLine intersection rightLine
    }


    fun List<MarkerCorners>.computeHomography(): Homography {

        val mappings = this.mapIndexed { i, marker ->

            val offset = when(i) {
                0 -> Vector2(0.0, 0.0)
                1 -> Vector2(18.0, 0.0)
                2 -> Vector2(0.0, 18.0)
                else -> error("input is not of size 3")
            }

            listOf(
                offset to marker.corners[0],
                (offset + Vector2(7.0, 0.0)) to marker.corners[1],
                (offset + Vector2(0.0, 7.0)) to marker.corners[2],
                (offset + Vector2(7.0, 7.0)) to marker.corners[3]
            )
        }.flatten()

        val matrix = mappings.flatMap {
            val local = it.first
            val image = it.second
            listOf(
                D[local.x, local.y, 1.0, 0.0    , 0.0    , 0.0, - image.x * local.x, - image.x * local.y],
                D[0.0    , 0.0    , 0.0, local.x, local.y, 1.0, - image.y * local.x, - image.y * local.y]
            )
        }.stack()

        val result = mappings
            .map { D[it.second.x, it.second.y] }
            .reduce { acc, array1D -> acc concat array1D }

        val entries = (matrix.transpose() matMul matrix).solve(matrix.transpose() matMul result)

        return Homography(
            D[D[entries[0], entries[1], entries[2]],
                    D[entries[3], entries[4], entries[5]],
                    D[entries[6], entries[7], 1.0]]
        )
    }

    data class MarkerCorners(
        val position: Vector2,
        val corners: List<Vector2>
    )

}
