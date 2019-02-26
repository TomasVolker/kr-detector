package tomasvolker.kr

import boofcv.gui.image.ShowImages
import boofcv.io.image.ConvertBufferedImage
import boofcv.gui.binary.VisualizeBinaryData
import boofcv.struct.ConfigLength
import boofcv.alg.filter.binary.GThresholdImageOps
import boofcv.alg.misc.ImageStatistics
import boofcv.gui.ListDisplayPanel
import javax.swing.Spring.height
import boofcv.struct.image.GrayU8
import boofcv.struct.image.GrayF32
import boofcv.io.image.UtilImageIO
import boofcv.io.webcamcapture.UtilWebcamCapture
import java.awt.image.BufferedImage



fun main() {

    val webcam = UtilWebcamCapture.openDefault(640, 480)

    // Display multiple images in the same window
    val gui = ListDisplayPanel()

    ShowImages.showWindow(gui,"Threshold",true)

    while(true) {

        gui.removeAll()

        val image = webcam.image ?: break

        // convert into a usable format
        val input = ConvertBufferedImage.convertFromSingle(image, null, GrayF32::class.java)
        val binary = GrayU8(input.width, input.height)

        // Global Methods
        GThresholdImageOps.threshold(input, binary, ImageStatistics.mean(input).toDouble(), true)
        gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Global: Mean")
        GThresholdImageOps.threshold(input, binary, GThresholdImageOps.computeOtsu(input, 0.0, 255.0), true)
        gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Global: Otsu")
        GThresholdImageOps.threshold(input, binary, GThresholdImageOps.computeEntropy(input, 0.0, 255.0), true)
        gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Global: Entropy")

        // Local method
        GThresholdImageOps.localMean(input, binary, ConfigLength.fixed(57.0), 1.0, true, null, null)
        gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Local: Square")
        GThresholdImageOps.blockMinMax(input, binary, ConfigLength.fixed(21.0), 1.0, true, 15.0)
        gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Local: Block Min-Max")
        GThresholdImageOps.blockMean(input, binary, ConfigLength.fixed(21.0), 1.0, true)
        gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Local: Block Mean")
        GThresholdImageOps.blockOtsu(input, binary, false, ConfigLength.fixed(21.0), 0.5, 1.0, true)
        gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Local: Block Otsu")
        GThresholdImageOps.localGaussian(input, binary, ConfigLength.fixed(85.0), 1.0, true, null, null)
        gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Local: Gaussian")
        GThresholdImageOps.localSauvola(input, binary, ConfigLength.fixed(11.0), 0.30f, true)
        gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Local: Sauvola")
        GThresholdImageOps.localNick(input, binary, ConfigLength.fixed(11.0), -0.2f, true)
        gui.addImage(VisualizeBinaryData.renderBinary(binary, false, null), "Local: NICK")

        // Sauvola is tuned for text image.  Change radius to make it run better in others.

        // Show the image image for reference
        gui.addImage(ConvertBufferedImage.convertTo(input, null), "Input Image")

    }


}