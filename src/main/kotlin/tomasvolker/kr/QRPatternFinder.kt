package tomasvolker.kr

import boofcv.alg.filter.binary.GThresholdImageOps
import boofcv.factory.fiducial.FactoryFiducial
import boofcv.gui.binary.VisualizeBinaryData
import boofcv.io.image.ConvertBufferedImage
import boofcv.io.webcamcapture.UtilWebcamCapture
import boofcv.struct.ConfigLength
import boofcv.struct.image.GrayF32
import boofcv.struct.image.GrayU8
import com.github.tomasvolker.parallel.mapParallel
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import tomasvolker.kr.openrndr.write
import tomasvolker.numeriko.core.dsl.I
import tomasvolker.numeriko.core.functions.transpose
import tomasvolker.numeriko.core.interfaces.array1d.double.DoubleArray1D
import tomasvolker.numeriko.core.interfaces.array1d.integer.IntArray1D
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.generic.forEachIndex
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import tomasvolker.numeriko.core.operations.stack
import tomasvolker.numeriko.core.operations.unstack


class QRPatternFinder() {
    companion object {
        fun findPatterns(image: DoubleArray2D, tolerance: Double = 0.2): List<QRPattern> {
            val lines = image.unstack()

            return scanImageHorizontal(lines, tolerance).let { horizontal ->
                horizontal + scanImageVertical(lines, tolerance).filter { !horizontal.contains(it) }
            }
        }

        private fun scanImageHorizontal(lines: List<DoubleArray1D>, tolerance: Double = 0.2): List<QRPattern> {
            val qrPatternList = mutableListOf<QRPattern>()

            lines.mapIndexed { index, line ->
                line.toLineSections(axisIndex = index)
                    .interleaved(overlap = 4)
                    .map {
                        if (it.isPattern(tolerance))
                            qrPatternList.add(QRPattern(it[2].centerX, it[2].centerY, it[0].unit))
                    }
            }

            return qrPatternList.filterIndexed { index, qrPattern ->
                !qrPattern.isSame(qrPatternList[index + 1])
            }.map { it.center(axis = 1) }
        }

        private fun scanImageVertical(lines: List<DoubleArray1D>, tolerance: Double = 0.2): List<QRPattern> =
            scanImageHorizontal(lines.stack().transpose().unstack(), tolerance).map { it.invertAxis() }
    }
}


fun DoubleArray2D.toGrayF32(): GrayF32 =
        GrayF32(shape0, shape1).also { this.forEachIndex { i0, i1 ->
            it.set(i0, i1, this[i0,i1].toFloat())
        } }

fun GrayF32.toDoubleArray2D(): DoubleArray2D =
        doubleArray2D(width, height) { i0, i1 -> get(i0, i1) }


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

                val qrPatternList = QRPatternFinder.findPatterns(input.toDoubleArray2D(), tolerance = 0.2)

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