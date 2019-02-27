package tomasvolker.kr

import boofcv.alg.filter.binary.GThresholdImageOps
import boofcv.gui.binary.VisualizeBinaryData
import boofcv.io.image.ConvertBufferedImage
import boofcv.io.webcamcapture.UtilWebcamCapture
import boofcv.struct.ConfigLength
import boofcv.struct.image.GrayF32
import boofcv.struct.image.GrayU8
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import tomasvolker.kr.openrndr.write
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.generic.forEachIndex
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D

typealias GrayscaleImage = List<List<Boolean>>

fun GrayscaleImage.transpose() =
        List(this[0].size) { i ->
            List(size) { j ->
                this[j][i]
            }
        }

class QRPatternFinder {
    companion object {
        fun findPatterns(lines: GrayscaleImage, tolerance: Double = 0.2): List<QRPattern> =
                scanImageHorizontal(lines, tolerance).let { horizontal ->
                    horizontal + scanImageVertical(lines, tolerance).filter { !horizontal.contains(it) }
                }

        private fun scanImageHorizontal(lines: GrayscaleImage, tolerance: Double = 0.2): List<QRPattern> {
            val qrPatternList = mutableListOf<QRPattern>()

            lines.mapIndexed { index, line ->
                line.toLineSections(axisIndex = index)
                    .interleaved(overlap = 5)
                    .map {
                        if (it.isPattern(tolerance))
                            qrPatternList.add(QRPattern(it[2].center, index, it[0].unit))
                    }
                    .toList()
            }

            return qrPatternList.filter { it.unit > 10 }.also { println(it) }
        }

        private fun scanImageVertical(lines: GrayscaleImage, tolerance: Double = 0.2): List<QRPattern> =
            scanImageHorizontal(lines.transpose(), tolerance).map { it.invertAxis() }
    }
}

fun GrayU8.toGrayscaleImage(): List<List<Boolean>> =
        List(width) { i -> List(height) { j -> get(i, j) == 1 }}


fun main() {
    val webcam = UtilWebcamCapture.openDefault(640, 480)

    application {

        configure {
            windowResizable = true
        }

        program {

            val buffer = colorBuffer(640, 480)
            val binary = GrayU8(webcam.viewSize.width, webcam.viewSize.height)

            extend(PanZoom())

            extend {

                val image = webcam.image
                val input = ConvertBufferedImage.convertFromSingle(image, null, GrayF32::class.java)

                GThresholdImageOps.localMean(input, binary, ConfigLength.fixed(57.0), 1.0, true, null, null)
//                GThresholdImageOps.blockMinMax(input, binary, ConfigLength.fixed(21.0), 1.0, true, 15.0)

                val qrPatternList = QRPatternFinder.findPatterns(binary.toGrayscaleImage(), tolerance = 0.2)

                val gray = VisualizeBinaryData.renderBinary(binary, false, null)

                buffer.write(gray)
                drawer.image(buffer)
                drawer.stroke = ColorRGBa.GREEN
                drawer.strokeWeight = 2.0

                drawer.run {

                    fill = ColorRGBa.RED

                    qrPatternList.forEach {
                        println(it)
                        circle(x = it.x.toDouble(), y = it.y.toDouble(), radius = it.unit / 2.0)
                    }

                }

            }

        }


    }

    webcam.close()

}