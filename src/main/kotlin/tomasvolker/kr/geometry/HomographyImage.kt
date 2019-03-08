package tomasvolker.kr.geometry

import boofcv.struct.image.GrayU8
import org.openrndr.math.Vector2
import tomasvolker.numeriko.core.performance.forEach
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

fun GrayU8.buildImageFromHomography(
    homography: Homography,
    width: Int,
    height: Int,
    scale: Double = 1.0
): GrayU8 = buildImageFromHomography(
    homography = homography,
    destination = GrayU8(ceil(width * scale).roundToInt(), ceil(height * scale).roundToInt()),
    scale = scale
)


fun GrayU8.buildImageFromHomography(
    homography: Homography,
    destination: GrayU8,
    scale: Double = 1.0
): GrayU8 {

    forEach(destination.width, destination.height) { lx, ly ->

        val (ix, iy) = homography(Vector2((lx + 0.5) / scale, (ly + 0.5) / scale))

        destination[lx, ly] = this[floor(ix).roundToInt(), floor(iy).roundToInt()]

    }

    return destination
}