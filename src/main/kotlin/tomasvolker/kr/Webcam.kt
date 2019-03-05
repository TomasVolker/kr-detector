package tomasvolker.kr

import boofcv.io.webcamcapture.UtilWebcamCapture
import boofcv.struct.image.GrayF32
import boofcv.struct.image.GrayU8
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import tomasvolker.kr.algorithms.Point
import tomasvolker.kr.algorithms.QrMarker
import tomasvolker.kr.algorithms.detectQrMarkers
import tomasvolker.kr.algorithms.reconstruct
import tomasvolker.kr.boofcv.*
import tomasvolker.kr.openrndr.write
import tomasvolker.numeriko.core.dsl.D
import tomasvolker.numeriko.core.interfaces.factory.doubleArray1D
import tomasvolker.numeriko.core.interfaces.factory.nextGaussian
import tomasvolker.openrndr.math.extensions.CursorPosition
import tomasvolker.openrndr.math.extensions.FPSDisplay
import tomasvolker.openrndr.math.extensions.Grid2D
import tomasvolker.openrndr.math.extensions.PanZoom
import tomasvolker.openrndr.math.primitives.d
import java.awt.image.BufferedImage
import kotlin.random.Random

fun QrMarker.isNeighbor(other: QrMarker, tolerance: Double = 0.2) =
        x.inTolerance(other.x, tolerance) && y.inTolerance(other.y, tolerance)

fun List<QrMarker>.hasNeighbors(tolerance: Double = 0.2): List<QrMarker> {
    val list = mutableListOf<QrMarker>()

    for (i in 0 until size) {
        if (this.mapIndexed { index, qrMarker ->
                if (index != i) qrMarker.isNeighbor(this[i], tolerance) else false }.count() > 5)
            list.add(this[i])
    }

    return list.toList()
}

fun main() {

    val webcam = UtilWebcamCapture.openDefault(640, 480)
    val imageWidth = webcam.viewSize.width
    val imageHeight = webcam.viewSize.height

    application {

        configure {
            windowResizable = true
        }

        program {

            val buffer = colorBuffer(640, 480)

            val binary = GrayU8(imageWidth, imageHeight)

            val work0 = binary.createSameShapeOf<GrayF32>()
            val work1 = binary.createSameShapeOf<GrayF32>()
            val work2 = binary.createSameShapeOf<GrayF32>()

            val seed = binary.createSameShape()
            seed[640/2, 480/2] = 1

            val result = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)

            val nClusters = 3
            val featureExtractor = { marker: QrMarker -> D[marker.x, marker.y] }
            val kmeans = KMeans<QrMarker>(
                nClusters = nClusters,
                featureExtractor = featureExtractor
            )
            var clusters = emptyList<ClusterSet<QrMarker>>()

            backgroundColor = ColorRGBa.WHITE

            extend(FPSDisplay())

            extend(PanZoom())
            extend(Grid2D())
            extend(CursorPosition())


            extend {

                val image = webcam.image

                val input = image
                    .convertToSingle(work0)
                    .localMeanThreshold(
                        60.0,
                        down = false,
                        destination = binary,
                        work1 = work1,
                        work2 = work2
                    )


                val markers = input.detectQrMarkers().hasNeighbors()

                if (markers.size > 3) {
                    kmeans.reset(data = markers)
                    clusters = kmeans.cluster(markers)
                }

                val gray = input.toBufferedImage()

                buffer.write(image)
                drawer.image(buffer)

                drawer.fill = ColorRGBa.RED

                clusters.forEach {
                    drawer.circle(it.centroid[0], it.centroid[1], 10.0)
                }

                /*markers.forEach {
                    drawer.circle(it.x.d, it.y.d, it.unit)
                }*/

            }

        }


    }

    webcam.close()

}
