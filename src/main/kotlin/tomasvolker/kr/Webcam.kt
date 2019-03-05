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
import tomasvolker.numeriko.core.dsl.I
import tomasvolker.numeriko.core.interfaces.array1d.double.DoubleArray1D
import tomasvolker.numeriko.core.interfaces.factory.doubleArray1D
import tomasvolker.numeriko.core.interfaces.factory.doubleIdentity
import tomasvolker.numeriko.core.interfaces.factory.nextGaussian
import tomasvolker.numeriko.core.operations.concatenate
import tomasvolker.numeriko.core.operations.stack
import tomasvolker.numeriko.core.operations.unstack
import tomasvolker.openrndr.math.extensions.CursorPosition
import tomasvolker.openrndr.math.extensions.FPSDisplay
import tomasvolker.openrndr.math.extensions.Grid2D
import tomasvolker.openrndr.math.extensions.PanZoom
import tomasvolker.openrndr.math.primitives.d
import java.awt.image.BufferedImage
import kotlin.math.absoluteValue
import kotlin.random.Random


fun Double.inRange(center: Double, deviation: Double): Boolean =
    (this - center).absoluteValue < deviation

fun <T> List<T>.mode(): T {
    val list = mutableListOf<T>().apply { this.addAll(this@mode) }
    var mode: T = this[0]
    var modeCount = 0

    for (i in 0 until size) {
        val currCount = list.count { it == this[i] }

        if (currCount > modeCount) {
            modeCount = currCount
            mode = this[i]
        }

        list.replace(list.filter { it != this[i] })
    }

    return mode
}

fun QrMarker.isNeighbor(other: QrMarker, deviation: Int = 3) =
        x.inRange(other.x, deviation) && y.inRange(other.y, deviation)

fun List<QrMarker>.hasNeighbors(nNeighbors: Int = 10,deviation: Int = 4): List<QrMarker> {
    val list = mutableListOf<QrMarker>()

    for (i in 0 until size) {
        if (this.mapIndexed { index, qrMarker ->
                if (index != i) qrMarker.isNeighbor(this[i], deviation) else false }.count() > nNeighbors)
            list.add(this[i])
    }

    return list.toList()
}

fun List<QrMarker>.centered() =
    map { QrMarker(it.x - 2 * it.unit.toInt(), it.y - 2 * it.unit.toInt(), it.unit) }

fun List<DoubleArray1D>.flatten() =
        reduce { acc, array -> acc.concatenate(array) }

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

//            val result = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)

            var centroids = emptyList<DoubleArray1D>()

            val nClusters = 3
            val featureExtractor = { marker: QrMarker -> D[marker.x, marker.y] }
            val kmeans = KMeans<QrMarker>(
                nClusters = nClusters,
                featureExtractor = featureExtractor
            )
            var clusters: List<ClusterSet<QrMarker>> = emptyList()

            val transitionMatrix = listOf<DoubleArray1D>(
                doubleArray1D(6) { 1.0 },
                doubleArray1D(6) { 1.0 },
                doubleArray1D(6) { 1.0 },
                doubleArray1D(6) { i -> if (i < 4) 0.0 else 1.0 },
                doubleArray1D(6) { i -> if (i < 4) 0.0 else 1.0 },
                doubleArray1D(6) { i -> if (i < 4) 0.0 else 1.0 }
            ).stack()
            val processNoiseMatrix = listOf<DoubleArray1D>(
                doubleArray1D(6) { i -> if (i < 4) 0.5 else 1.0 }
            ).stack()
            val measurementNoiseMatrix = listOf<DoubleArray1D>(
                doubleArray1D(6) { 1.0 }
            ).stack()

            val kalmanFilter = KalmanFilter(
                transitionMatrix = transitionMatrix,
                processNoiseMatrix = processNoiseMatrix,
                processNoise = 20.0,
                measurementMatrix = doubleIdentity(6),
                measurementNoiseMatrix = measurementNoiseMatrix,
                measurementNoise = 0.1
            )

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


                val markers = input.detectQrMarkers()
                    .hasNeighbors()
                    .centered()

                if (markers.size > 3) {
                    kmeans.reset(
                        initCentroids = clusters.map { QrMarker(it.centroid[0].toInt(), it.centroid[1].toInt(), 10.0) }
                    )
                    clusters = kmeans.cluster(markers)
                    kmeans.reset(
                        initCentroids = clusters.map { QrMarker(it.centroid[0].toInt(), it.centroid[1].toInt(), 10.0) }
                    )

                    val filteredData = mutableListOf<QrMarker>()
                    for (i in 0 until clusters.size) {
                        if (clusters[i].data.toList().isNotEmpty()) {
//                            val clusterSize = clusters[i].data.toList().size
//                            val medianUnit = clusters[i].data.map { it.unit }.sorted()[clusterSize / 2]
//                            val meanUnit = clusters[i].data.map { it.unit }.average()
                            val modeUnit = clusters[i].data.map { it.unit }.mode()

                            filteredData.addAll(clusters[i].data.filter { it.unit.inRange(modeUnit, 2.0) })
                        }
                    }

                    clusters = kmeans.cluster(filteredData)
                    /*centroids = kalmanFilter.step(clusters.map { it.centroid }.flatten())
                        .withShape(I[3, 2]).as2D().unstack()*/
                    centroids = kmeans.centroids.toList()
                    println(centroids)
                }

                val gray = input.toBufferedImage()

                buffer.write(image)
                drawer.image(buffer)

                drawer.fill = ColorRGBa.RED

                centroids.forEach {
                    drawer.circle(it[0], it[1], 10.0)
                }

                /*markers.forEach {
                    drawer.circle(it.x.d, it.y.d, it.unit)
                }*/

            }

        }


    }

    webcam.close()

}
