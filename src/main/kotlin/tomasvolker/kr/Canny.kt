package tomasvolker.kr

import boofcv.factory.feature.detect.edge.FactoryEdgeDetectors
import boofcv.io.webcamcapture.UtilWebcamCapture
import boofcv.struct.image.GrayF32
import boofcv.struct.image.GrayS16
import boofcv.struct.image.GrayU8
import org.openrndr.application
import org.openrndr.draw.colorBuffer
import tomasvolker.kr.boofcv.convertToSingle
import tomasvolker.kr.boofcv.createSameShapeOf
import tomasvolker.kr.boofcv.toBufferedImage
import tomasvolker.kr.openrndr.write
import tomasvolker.openrndr.math.extensions.FPSDisplay
import tomasvolker.openrndr.math.extensions.PanZoom
import java.awt.image.BufferedImage


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

            val work0 = binary.createSameShapeOf<GrayU8>()
            val work1 = binary.createSameShapeOf<GrayF32>()
            val work2 = binary.createSameShapeOf<GrayF32>()

            val result = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)

            extend(FPSDisplay())

            extend(PanZoom())

            extend {

                val image = webcam.image

                val input = image.convertToSingle(work0)

                val edgeImage = input.createSameShape()

                val canny = FactoryEdgeDetectors.canny(2, true, true, GrayU8::class.java, GrayS16::class.java)
                canny.process(input, 0.1f, 0.3f, edgeImage)

                buffer.write(edgeImage.toBufferedImage())
                drawer.image(buffer)

            }

        }


    }

    webcam.close()

}