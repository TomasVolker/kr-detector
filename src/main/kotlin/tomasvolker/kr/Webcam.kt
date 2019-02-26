package tomasvolker.kr

import boofcv.io.image.ConvertBufferedImage
import boofcv.gui.image.ShowImages
import boofcv.io.webcamcapture.UtilWebcamCapture
import boofcv.alg.fiducial.qrcode.QrCode
import boofcv.alg.filter.binary.GThresholdImageOps
import boofcv.alg.misc.ImageStatistics
import boofcv.factory.fiducial.FactoryFiducial
import boofcv.gui.binary.VisualizeBinaryData
import boofcv.gui.feature.VisualizeShapes
import boofcv.gui.image.ImagePanel
import boofcv.struct.ConfigLength
import boofcv.struct.image.GrayF32
import boofcv.struct.image.GrayU8
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import org.openrndr.extensions.Debug3D
import org.openrndr.math.Vector2
import tomasvolker.kr.openrndr.write
import java.awt.BasicStroke
import java.awt.Color

fun main() {

    val webcam = UtilWebcamCapture.openDefault(640, 480)

    application {

        configure {
            windowResizable = true
        }

        program {

            val detector = FactoryFiducial.qrcode(null, GrayU8::class.java)

            val buffer = colorBuffer(640, 480)
            val binary = GrayU8(webcam.viewSize.width, webcam.viewSize.height)

            extend(PanZoom())

            extend {

                val image = webcam.image

                val input = ConvertBufferedImage.convertFromSingle(image, null, GrayF32::class.java)

                GThresholdImageOps.localMean(input, binary, ConfigLength.fixed(57.0), 1.0, true, null, null)

                val gray = VisualizeBinaryData.renderBinary(binary, false, null)
/*
                // Get's a list of all the qr codes it could successfully detect and decode
                val detections = detector.detections
                val failures = detector.failures
*/
                buffer.write(gray)
                drawer.image(buffer)


                drawer.stroke = ColorRGBa.GREEN
                drawer.strokeWeight = 2.0
/*
                for (qr in detections) {

                    drawer.lineLoop(
                        List(qr.bounds.size()) { i -> qr.bounds[i] }
                            .map { Vector2(it.x, it.y) }
                    )

                }

                drawer.stroke = ColorRGBa.RED
                drawer.strokeWeight = 2.0

                for (qr in failures) {

                    drawer.lineLoop(
                        List(qr.bounds.size()) { i -> qr.bounds[i] }
                            .map { Vector2(it.x, it.y) }
                    )

                }
*/
            }

        }


    }

    webcam.close()

}
