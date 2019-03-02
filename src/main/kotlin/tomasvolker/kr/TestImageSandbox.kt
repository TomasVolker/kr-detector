package tomasvolker.kr

import boofcv.io.webcamcapture.UtilWebcamCapture
import boofcv.struct.image.GrayF32
import boofcv.struct.image.GrayU8
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import tomasvolker.kr.algorithms.Point
import tomasvolker.kr.algorithms.detectQrMarkers
import tomasvolker.kr.algorithms.reconstruct
import tomasvolker.kr.algorithms.reconstructMarker
import tomasvolker.kr.boofcv.*
import tomasvolker.kr.openrndr.write
import tomasvolker.openrndr.math.extensions.CursorPosition
import tomasvolker.openrndr.math.extensions.FPSDisplay
import tomasvolker.openrndr.math.extensions.Grid2D
import tomasvolker.openrndr.math.extensions.PanZoom
import tomasvolker.openrndr.math.primitives.d
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt


fun main() {

    val image = ImageIO.read(File("test_image.jpg"))

    val marker1 = Point(191, 237)

    application {

        configure {
            windowResizable = true
        }

        program {

            val buffer = colorBuffer(640, 480)

            val binary = GrayU8(image.width, image.height)

            backgroundColor = ColorRGBa.WHITE

            extend(FPSDisplay())

            extend(PanZoom())
            extend(Grid2D())
            extend(CursorPosition())


            extend {

                val mousePosition = mouse.position.let {
                    Point(it.x.roundToInt(), it.y.roundToInt())
                }

                val input = image
                    .convertToSingle<GrayU8>()
                    .localMeanThreshold(
                        size = 50.0,
                        scale = 0.95,
                        down = false,
                        destination = binary
                    )

                val marker = input.reconstructMarker(mousePosition)


                buffer.write(marker.toBufferedImage())
                drawer.image(buffer)

            }

        }


    }

}
