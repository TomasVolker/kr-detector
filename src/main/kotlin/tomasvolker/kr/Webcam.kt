package tomasvolker.kr

import boofcv.io.webcamcapture.UtilWebcamCapture
import boofcv.struct.image.GrayF32
import boofcv.struct.image.GrayU8
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import tomasvolker.kr.geometry.Point
import tomasvolker.kr.boofcv.*
import tomasvolker.kr.openrndr.write
import tomasvolker.openrndr.math.extensions.CursorPosition
import tomasvolker.openrndr.math.extensions.FPSDisplay
import tomasvolker.openrndr.math.extensions.Grid2D
import tomasvolker.openrndr.math.extensions.PanZoom
import kotlin.math.roundToInt


fun main() {

    val webcam = UtilWebcamCapture.openDefault(640, 480)
    val imageWidth = webcam.viewSize.width
    val imageHeight = webcam.viewSize.height

    println("Webcam opened: $imageWidth x $imageHeight")

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
/*
                val reconstructed = input.reconstructMarker(
                    QrPattern(
                        x = mousePosition.x,
                        y = mousePosition.y,
                        unitX = 10.0,
                        unitY = 10.0
                    )
                )
*/
/*
                val verticalMarkers = input.detectQrPatterns()
                //val horizontalMarkers = input.detectHorizontalQrPatterns()
*/
                buffer.write(input.toBufferedImage())
                drawer.image(buffer)
/*
                drawer.fill = ColorRGBa.RED
                reconstructed.corners.forEach {
                    drawer.circle(it.x, it.y, 5.0)
                }*/
/*
                drawer.fill = ColorRGBa.BLUE
                horizontalMarkers.forEach {
                    drawer.circle(it.x.d, it.y.d, it.unit)
                }
*/
            }

        }


    }

    webcam.close()

}
