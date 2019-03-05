package tomasvolker.kr

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.isolated
import org.openrndr.math.Vector2
import tomasvolker.kr.algorithm.*
import tomasvolker.kr.boofcv.*
import tomasvolker.kr.geometry.Point
import tomasvolker.kr.geometry.buildImageFromHomography
import tomasvolker.kr.openrndr.write
import tomasvolker.openrndr.math.extensions.CursorPosition
import tomasvolker.openrndr.math.extensions.PanZoom
import tomasvolker.openrndr.math.primitives.d
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt


fun main() {

    val image = ImageIO.read(File("test_image.jpg"))

    val detector = QrDetector(image.width, image.height)

    val homography = detector.detectQr(image)

    val qrImage = detector.thresholded.buildImageFromHomography(
        homography = homography,
        width = 25,
        height = 25,
        scale = 10.0
    )

    val homographyLimits = listOf(
        Vector2(0.0, 0.0),
        Vector2(25.0, 0.0),
        Vector2(0.0, 25.0),
        Vector2(25.0, 25.0)
    ).map(homography)

    application {

        configure {
            windowResizable = true
        }

        program {

            val buffer = colorBuffer(640, 480)
            val bufferQr = colorBuffer(qrImage.width, qrImage.height)

            val bufferedImage = detector.thresholded.toBufferedImage()
            buffer.write(bufferedImage)

            bufferQr.write(qrImage.toBufferedImage())

            backgroundColor = ColorRGBa.WHITE

            extend(PanZoom())
            //extend(Grid2D())
            extend(CursorPosition())

            extend {

                val mousePosition = mouse.position.let {
                    Point(it.x.roundToInt(), it.y.roundToInt())
                }

                drawer.image(buffer)

                drawer.fill = ColorRGBa.RED
                drawer.stroke = ColorRGBa.RED

                drawer.fontMap = Resources.defaultFont

                detector.sortedCorners.forEachIndexed { i, marker ->
                    drawer.text(
                        text = "$i",
                        position = marker.position
                    )

                    marker.corners.forEachIndexed { j, corner ->
                        drawer.text(
                            text = "$j",
                            position = corner
                        )
                    }

                }

                drawer.isolated {

                    stroke = ColorRGBa.BLUE

                    lineSegment(
                        homographyLimits[0],
                        homographyLimits[1]
                    )

                    lineSegment(
                        homographyLimits[1],
                        homographyLimits[3]
                    )

                    lineSegment(
                        homographyLimits[0],
                        homographyLimits[2]
                    )

                    lineSegment(
                        homographyLimits[2],
                        homographyLimits[3]
                    )

                }

                drawer.isolated {

                    translate(x = 640.0, y = 0.0)

                    image(bufferQr)

                    fill = null
                    stroke = ColorRGBa.BLUE

                    rectangle(Vector2.ZERO, bufferQr.width.d, bufferQr.height.d)

                }

            }

        }


    }

}
