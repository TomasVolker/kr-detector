package tomasvolker.kr.algorithm

import boofcv.gui.binary.VisualizeBinaryData
import boofcv.io.webcamcapture.UtilWebcamCapture
import boofcv.struct.image.GrayF32
import boofcv.struct.image.GrayU8
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import org.openrndr.math.Vector2
import tomasvolker.kr.boofcv.convertToSingle
import tomasvolker.kr.boofcv.createSameShapeOf
import tomasvolker.kr.boofcv.localMeanThreshold
import tomasvolker.kr.geometry.Point
import tomasvolker.kr.openrndr.write
import tomasvolker.numeriko.core.dsl.D
import tomasvolker.numeriko.core.interfaces.array1d.double.DoubleArray1D
import tomasvolker.openrndr.math.extensions.CursorPosition
import tomasvolker.openrndr.math.extensions.FPSDisplay
import tomasvolker.openrndr.math.extensions.Grid2D
import tomasvolker.openrndr.math.extensions.PanZoom
import tomasvolker.openrndr.math.primitives.d
import kotlin.math.roundToInt

typealias GrayscaleImage = List<List<Boolean>>

fun GrayscaleImage.transpose() =
    List(this[0].size) { i ->
        List(size) { j ->
            this[j][i]
        }
    }

object QRPatternFinder {

    /*fun findPatterns(lines: GrayscaleImage, tolerance: Double = 0.2): List<QRPattern> =
        scanImageHorizontal(lines, tolerance).let { horizontal ->
            horizontal + scanImageVertical(lines, tolerance).filter { !horizontal.contains(it) }
        }.map { it.invertAxis() }*/

    fun findPatterns(lines: GrayscaleImage, tolerance: Double = 0.2): List<QrPattern> =
        scanImageHorizontal(lines, tolerance).let { horizontal ->
            horizontal + scanImageVertical(lines, tolerance)
        }.map { QrPattern(it.y, it.x, it.unitX, it.unitY) }

    private fun scanImageHorizontal(lines: GrayscaleImage, tolerance: Double = 0.2): List<QrPattern> {
        val qrPatternList = mutableListOf<QrPattern>()

        lines.mapIndexed { index, line ->
            line.toLineSections(axisIndex = index)
                .interleaved(overlap = 5)
                .map {
                    if (it.isPattern(tolerance))
                        qrPatternList.add(QrPattern(it[2].center, index, it[0].unit.d, it[0].unit.d))
                }
                .toList()
        }

        return qrPatternList
    }

    private fun scanImageVertical(lines: GrayscaleImage, tolerance: Double = 0.2): List<QrPattern> =
        scanImageHorizontal(lines.transpose(), tolerance).map { QrPattern(it.y, it.x, it.unitX, it.unitY) }

}

fun GrayU8.toGrayscaleImage(): List<List<Boolean>> =
    List(width) { i -> List(height) { j -> get(i, j) == 1 } }

/*
fun main() {
    val webcam = UtilWebcamCapture.openDefault(640, 480)
    val imageWidth = webcam.viewSize.width
    val imageHeight = webcam.viewSize.height

    application {

        configure {
            windowResizable = true
        }

        program {

            val buffer = colorBuffer(imageWidth, imageHeight)

            val binary = GrayU8(imageWidth, imageHeight)

            val work0 = binary.createSameShapeOf<GrayF32>()
            val work1 = binary.createSameShapeOf<GrayF32>()
            val work2 = binary.createSameShapeOf<GrayF32>()

            var clusters = emptyList<ClusterSet<QRPattern>>()
            val featureExtractor = { marker: QRPattern -> D[marker.x, marker.y] }
            val kMeans = KMeans(
                nClusters = 3,
                featureExtractor = featureExtractor
            )
            var centroids: List<DoubleArray1D> = emptyList()

            backgroundColor = ColorRGBa.WHITE

            extend(FPSDisplay())

            extend(PanZoom())
            extend(Grid2D())
            extend(CursorPosition())

            extend {

                val mousePosition = mouse.position.let {
                    Point(
                        it.x.roundToInt(),
                        it.y.roundToInt()
                    )
                }

                val image = webcam.image

                val input = image
                    .convertToSingle(work0)
                    .localMeanThreshold(
                        size = 50.0,
                        scale = 0.95,
                        down = false,
                        destination = binary,
                        work1 = work1,
                        work2 = work2
                    )
                val markers = QRPatternFinder.findPatterns(
                    binary.toGrayscaleImage(),
                    tolerance = 0.2
                )
                    .hasNeighbors(nNeighbors = 40, deviation = 15)

                if (markers.size > 3) {
                    kMeans.reset(
                        initCentroids = clusters
                            .map {
                                QRPattern(
                                    it.centroid[0].toInt(),
                                    it.centroid[1].toInt(),
                                    10
                                )
                            }
                    )
                    clusters = kMeans.cluster(markers)
                    centroids = clusters.map { it.centroid }
                }

                val gray = VisualizeBinaryData.renderBinary(binary, false, null)

                buffer.write(gray)
                drawer.image(buffer)
                drawer.stroke = ColorRGBa.GREEN
                drawer.strokeWeight = 2.0

                drawer.run {

                    fill = ColorRGBa.RED

                    centroids.forEach {
                        circle(it[0], it[1], 10.0)
                    }

                    markers.forEach {
                        println(it)
                        circle(x = it.x.toDouble(), y = it.y.toDouble(), radius = it.unit / 2.0)
                    }

                }

            }

        }


    }

    webcam.close()

}*/