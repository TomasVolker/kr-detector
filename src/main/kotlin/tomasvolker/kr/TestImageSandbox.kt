package tomasvolker.kr

import boofcv.struct.image.GrayU8
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import org.openrndr.math.Vector2
import tomasvolker.kr.algorithms.*
import tomasvolker.kr.boofcv.*
import tomasvolker.kr.openrndr.write
import tomasvolker.openrndr.math.extensions.CursorPosition
import tomasvolker.openrndr.math.extensions.PanZoom
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.roundToInt


fun main() {

    val image = ImageIO.read(File("test_image.jpg"))


    val thresholded = image
        .convertToSingle<GrayU8>()
        .localMeanThreshold(
            size = 50.0,
            scale = 0.95,
            down = false
        )

    val patternList = listOf(
        QrPattern(
            x = 170,
            y = 355,
            unitX = 10.0,
            unitY = 10.0
        ),
        QrPattern(
            x = 193,
            y = 240,
            unitX = 10.0,
            unitY = 10.0
        ),
        QrPattern(
            x = 303,
            y = 258,
            unitX = 10.0,
            unitY = 10.0
        )
    )

    val corners = patternList.map {
        thresholded.reconstructMarker(it)
    }

    val markers = corners
        .sortedMarkers()
        .sortedCorners()

    val homography = markers.computeHomography()

    val markerSequence = listOf(0.0, 7.0, 18.0, 25.0)

    val localPointList = markerSequence.flatMap { x ->
        markerSequence.map { y ->
            Vector2(x, y)
        }
    }.map(homography)

    application {

        configure {
            windowResizable = true
        }

        program {

            val buffer = colorBuffer(640, 480)

            val bufferedImage = thresholded.toBufferedImage()
            buffer.write(bufferedImage)

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

                markers.forEachIndexed { i, marker ->
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

                drawer.fill = ColorRGBa.BLUE

                localPointList.forEach {
                    drawer.circle(
                        position = it,
                        radius = 3.0
                    )
                }




            }

        }


    }

}
