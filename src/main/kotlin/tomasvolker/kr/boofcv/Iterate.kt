package tomasvolker.kr.boofcv

import boofcv.struct.image.GrayF32
import boofcv.struct.image.GrayF64
import boofcv.struct.image.GrayU8
import boofcv.struct.image.ImageBase
import tomasvolker.kr.algorithms.Point

inline fun ImageBase<*>.forEachIndex(
    block: (index: Int)->Unit
) {
    val rows = height
    val columns = width

    for (y in 0 until rows) {
        var index = startIndex + y * stride

        val indexEnd = index + columns
        while (index < indexEnd) {
            block(index)
            index++
        }
    }

}

inline fun ImageBase<*>.forEachIndexXY(
    block: (index: Int, x: Int, y: Int)->Unit
) {
    val rows = height
    val columns = width

    for (y in 0 until rows) {
        var x = 0
        var index = startIndex + y * stride

        val indexEnd = index + columns
        while (index < indexEnd) {
            block(index, x, y)
            x++
            index++
        }
    }

}

inline fun GrayU8.forEachPixel(block: (value: Int)->Unit) {
    forEachIndex { index ->
        block(data[index].toInt() and 0xFF)
    }
}

inline fun GrayU8.forEachPixel(block: (x: Int, y: Int, value: Int)->Unit) {
    forEachIndexXY { index, x, y ->
        block(x, y, data[index].toInt() and 0xFF)
    }
}

inline fun GrayF32.forEachPixel(block: (x: Int, y: Int, value: Float)->Unit) {
    forEachIndexXY { index, x, y ->
        block(x, y, data[index])
    }
}

inline fun GrayF64.forEachPixel(block: (x: Int, y: Int, value: Double)->Unit) {
    forEachIndexXY { index, x, y ->
        block(x, y, data[index])
    }
}

inline fun GrayU8.points(
    predicate: (x: Int, y: Int, value: Int)->Boolean
): List<Point> = mutableListOf<Point>().apply {
    forEachPixel { x, y, value ->
        if(predicate(x, y, value)) add(Point(x, y))
    }
}

fun GrayU8.nonZeroPoints() = points { x, y, value -> value != 0 }
