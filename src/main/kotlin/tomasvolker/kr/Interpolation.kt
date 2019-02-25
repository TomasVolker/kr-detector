package tomasvolker.kr

import boofcv.gui.image.ShowImages
import boofcv.io.image.ConvertBufferedImage
import boofcv.core.image.border.BorderType
import boofcv.factory.interpolate.FactoryInterpolation
import boofcv.struct.image.GrayF32
import boofcv.alg.interpolate.InterpolationType
import boofcv.gui.ListDisplayPanel
import boofcv.io.UtilIO
import boofcv.io.image.UtilImageIO



fun main() {

    val buffered = UtilImageIO.loadImage("data/IMG_2713.JPG")
    val gui = ListDisplayPanel()

    gui.addImage(buffered, "Original")

    // For sake of simplicity assume it's a gray scale image.  Interpolation functions exist for planar and
    // interleaved color images too
    val input = ConvertBufferedImage.convertFrom(buffered, null as GrayF32?)
    val scaled = input.createNew(500, 500 * input.height / input.width)

    for (type in InterpolationType.values()) {
        // Create the single band (gray scale) interpolation function for the input image
        val interp = FactoryInterpolation.createPixelS<GrayF32>(0.0, 255.0, type, BorderType.EXTENDED, input.dataType)

        // Tell it which image is being interpolated
        interp.image = input

        // Manually apply scaling to the input image.  See FDistort() for a built in function which does
        // the same thing and is slightly more efficient
        for (y in 0 until scaled.height) {
            // iterate using the 1D index for added performance.  Altertively there is the set(x,y) operator
            var indexScaled = scaled.startIndex + y * scaled.stride
            val origY = y * input.height / scaled.height.toFloat()

            for (x in 0 until scaled.width) {
                val origX = x * input.width / scaled.width.toFloat()

                scaled.data[indexScaled++] = interp.get(origX, origY)
            }
        }

        // Add the results to the output
        val out = ConvertBufferedImage.convertTo(scaled, null, true)
        gui.addImage(out, type.toString())
    }

    ShowImages.showWindow(gui, "Example Interpolation", true)

}