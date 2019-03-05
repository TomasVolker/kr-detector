package tomasvolker.kr

import boofcv.io.webcamcapture.UtilWebcamCapture
import boofcv.struct.image.GrayF32
import boofcv.struct.image.GrayU8
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import org.openrndr.math.Vector2
import tomasvolker.kr.algorithm.*
import tomasvolker.kr.geometry.Point
import tomasvolker.kr.openrndr.write
import tomasvolker.openrndr.math.extensions.CursorPosition
import tomasvolker.openrndr.math.extensions.FPSDisplay
import tomasvolker.openrndr.math.extensions.PanZoom
import kotlin.math.roundToInt


fun main() {

    val webcam = UtilWebcamCapture.openDefault(640, 480)
    val imageWidth = webcam.viewSize.width
    val imageHeight = webcam.viewSize.height

    val detector = QrDetector(imageWidth, imageHeight)

    println("Webcam opened: $imageWidth x $imageHeight")

    application {

        configure {
            windowResizable = true
        }

        program {

            val buffer = colorBuffer(imageWidth, imageHeight)

            backgroundColor = ColorRGBa.WHITE

            extend(FPSDisplay())

            extend(PanZoom())
            extend(CursorPosition())

            val localCorners = listOf(
                Vector2(0.0, 0.0),
                Vector2(25.0, 0.0),
                Vector2(25.0, 25.0),
                Vector2(0.0, 25.0)
            )

            extend {

                val mousePosition = mouse.position.let {
                    Point(
                        it.x.roundToInt(),
                        it.y.roundToInt()
                    )
                }

                val image = webcam.image

                val homography = detector.detectQr(image)

                buffer.write(image)
                drawer.image(buffer)

                if (homography != null) {

                    drawer.stroke = ColorRGBa.BLUE
                    drawer.strokeWeight = 2.0

                    val corners = localCorners.map(homography)

                    if (corners.all { !it.x.isNaN() && !it.y.isNaN() })
                        drawer.lineLoop(corners)

                }

            }

        }


    }

    webcam.close()

}
