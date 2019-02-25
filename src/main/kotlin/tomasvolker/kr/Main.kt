package tomasvolker.kr

import boofcv.alg.fiducial.qrcode.QrCode
import boofcv.factory.fiducial.FactoryFiducial
import boofcv.gui.feature.VisualizeShapes
import boofcv.gui.image.ShowImages
import boofcv.io.image.ConvertBufferedImage
import boofcv.io.image.UtilImageIO
import boofcv.struct.image.GrayU8
import java.awt.BasicStroke
import java.awt.Color

fun main() {


    val input = UtilImageIO.loadImage("data/IMG_2730.JPG")
    val gray = ConvertBufferedImage.convertFrom(input, null as GrayU8?)

    val detector = FactoryFiducial.qrcode(null, GrayU8::class.java)

    detector.process(gray)

    // Get's a list of all the qr codes it could successfully detect and decode
    val detections = detector.detections

    val g2 = input.createGraphics()
    val strokeWidth = Math.max(4, input.width / 200) // in large images the line can be too thin
    g2.color = Color.GREEN
    g2.stroke = BasicStroke(strokeWidth.toFloat())
    for (qr in detections) {
        // The message encoded in the marker
        println("message: " + qr.message);

        // Visualize its location in the image
        VisualizeShapes.drawPolygon(qr.bounds, true, 1.0, g2);
    }

    // List of objects it thinks might be a QR Code but failed for various reasons
    val failures = detector.failures
    g2.color = Color.RED
    for(qr in failures) {
        // If the 'cause' is ERROR_CORRECTION or later then it's probably a real QR Code that
        if( qr.failureCause.ordinal < QrCode.Failure.ERROR_CORRECTION.ordinal)
            continue

        VisualizeShapes.drawPolygon(qr.bounds, true, 1.0, g2);
    }

    ShowImages.showWindow(input,"Example QR Codes", true);

}


