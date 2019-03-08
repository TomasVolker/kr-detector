package tomasvolker.kr

import boofcv.io.webcamcapture.UtilWebcamCapture
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.draw.colorBuffer
import tomasvolker.kr.openrndr.write
import java.io.File
import javax.imageio.ImageIO

fun main() {

    val webcam = UtilWebcamCapture.openDefault(640, 480)

    application {

        configure {
            windowResizable = true
        }

        program {

            val buffer = colorBuffer(640, 480)

            keyboard.keyDown.listen {
                if (it.key == KEY_SPACEBAR)
                    ImageIO.write(webcam.image, "jpg", File("camera_${System.currentTimeMillis()}.jpg"))
            }

            extend {
                buffer.write(webcam.image)
                drawer.image(buffer)

            }

        }


    }

    webcam.close()

}