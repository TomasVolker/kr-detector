package tomasvolker.kr.demo

import boofcv.io.webcamcapture.UtilWebcamCapture
import boofcv.struct.image.GrayU8
import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.colorBuffer
import org.openrndr.math.Vector2
import tomasvolker.kr.algorithm.QrDetector
import tomasvolker.kr.algorithm.position
import tomasvolker.kr.boofcv.toBufferedImage
import tomasvolker.kr.boofcv.toBufferedImageBinary
import tomasvolker.kr.geometry.Homography
import tomasvolker.kr.openrndr.average
import tomasvolker.kr.openrndr.write
import tomasvolker.numeriko.core.primitives.modulo
import tomasvolker.openrndr.math.extensions.CursorPosition
import tomasvolker.openrndr.math.extensions.FPSDisplay
import tomasvolker.openrndr.math.extensions.PanZoom
import tomasvolker.openrndr.math.primitives.d
import java.awt.image.BufferedImage

class DemoProgram: Program() {

    val webcam = UtilWebcamCapture.openDefault(640, 480)
    val imageWidth = webcam.viewSize.width
    val imageHeight = webcam.viewSize.height

    val detector = QrDetector(imageWidth, imageHeight)

    val imageBuffer: BufferedImage = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)
    val colorBuffer by lazy { colorBuffer(imageWidth, imageHeight) }


    var homography: Homography? = null
    var image: BufferedImage = webcam.image


    val localCorners = listOf(
        Vector2(0.0, 0.0),
        Vector2(25.0, 0.0),
        Vector2(25.0, 25.0),
        Vector2(0.0, 25.0)
    )

    enum class State {
        FINAL_RESULT,
        GRAY,
        THRESHOLDED,
        RECOGNITIONS,
        CLUSTERS,
        FILTERED_CLUSTERS,
        CORNERS,
        SORTED_MARKERS,
        SORTED_CORNERS,
        HOMOGRAPHY
    }

    var state = State.FINAL_RESULT

    override fun setup() {

        backgroundColor = ColorRGBa.WHITE

        extend(FPSDisplay())

        extend(PanZoom())
        extend(CursorPosition())

        extend {
            image = webcam.image
            homography = detector.detectQr(image)
        }

        keyboard.keyDown.listen { onKeyDown(it) }

    }

    fun onKeyDown(event: KeyEvent) {

        val stateList = State.values().toList()

        state = when(event.key) {
            KEY_ARROW_RIGHT -> stateList[(state.ordinal+1) modulo stateList.size]
            KEY_ARROW_LEFT -> stateList[(state.ordinal-1) modulo stateList.size]
            else -> state
        }

    }

    override fun draw() {

        with(drawer) {
            when(state) {
                State.FINAL_RESULT -> drawFinalResult()
                State.GRAY -> drawGray()
                State.THRESHOLDED -> drawThresholded()
                State.RECOGNITIONS -> drawDetections()
                State.CLUSTERS -> drawClusters()
                State.FILTERED_CLUSTERS -> drawFilteredClusters()
                State.CORNERS -> drawCorners()
                State.SORTED_MARKERS -> drawSortedMarkers()
                State.SORTED_CORNERS -> drawSortedCorners()
                State.HOMOGRAPHY -> drawComputedHomography()
            }
        }

    }

    fun Drawer.image(image: BufferedImage) {
        colorBuffer.write(image)
        image(colorBuffer)
    }

    fun Drawer.image(image: GrayU8) {
        image.toBufferedImage(imageBuffer)
        colorBuffer.write(imageBuffer)
        image(colorBuffer)
    }

    fun Drawer.imageBinary(image: GrayU8) {
        image.toBufferedImageBinary(destination = imageBuffer)
        colorBuffer.write(imageBuffer)
        image(colorBuffer)
    }

    fun Drawer.drawFinalResult() {

        image(image)

        homography?.let { drawHomography(it) }

    }

    fun Drawer.drawHomography(homography: Homography) {

        stroke = ColorRGBa.BLUE
        strokeWeight = 2.0

        val corners = localCorners.map(homography)

        if (corners.all { !it.x.isNaN() && !it.y.isNaN() })
            lineLoop(corners)

    }

    fun Drawer.drawGray() {
        image(detector.grayScale)
    }

    fun Drawer.drawThresholded() {
        imageBinary(detector.thresholded)
    }

    fun Drawer.drawDetections() {
        imageBinary(detector.thresholded)

        fill = ColorRGBa.RED

        detector.recognitions.forEach {
            circle(
                it.position,
                it.unitX
            )
        }

    }

    fun Drawer.drawClusters() {
        imageBinary(detector.thresholded)

        fontMap = Resources.defaultFont
        fill = ColorRGBa.RED

        detector.clusters.forEach {
            text(
                it.size.toString(),
                it.map { it.position }.average()
            )
        }

    }

    fun Drawer.drawFilteredClusters() {
        imageBinary(detector.thresholded)

        fontMap = Resources.defaultFont
        fill = ColorRGBa.RED

        detector.filteredClusters.forEach {
            text(
                it.size.toString(),
                it.map { it.position }.average()
            )
        }

    }

    fun Drawer.drawCorners() {
        imageBinary(detector.thresholded)

        fill = ColorRGBa.RED

        detector.rawMarkers.forEach {
            it.corners.forEach {
                circle(
                    it,
                    5.0
                )
            }
        }

    }

    fun Drawer.drawSortedMarkers() {
        imageBinary(detector.thresholded)

        fontMap = Resources.defaultFont
        fill = ColorRGBa.RED

        detector.sortedMarkers.forEachIndexed { i, marker ->

            text(
                i.toString(),
                marker.position
            )

            marker.corners.forEach {
                circle(
                    it,
                    5.0
                )
            }
        }

    }

    fun Drawer.drawSortedCorners() {
        imageBinary(detector.thresholded)

        fontMap = Resources.defaultFont
        fill = ColorRGBa.RED

        detector.sortedCorners.forEachIndexed { i, marker ->

            text(
                i.toString(),
                marker.position
            )

            marker.corners.forEachIndexed { j, corner ->
                text(
                    j.toString(),
                    corner
                )
            }
        }

    }

    fun Drawer.drawComputedHomography() {

        imageBinary(detector.thresholded)

        homography?.let { drawHomography(it) }

    }

}

fun main() = application(
    configuration = configuration {
        title = "KR Detector"
        width = 640
        height = 480
        windowResizable = true
    },
    program = DemoProgram()
)
