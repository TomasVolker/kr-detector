package tomasvolker.kr.algorithm

import boofcv.struct.image.GrayU8
import tomasvolker.openrndr.math.primitives.d

fun GrayU8.scanRowsForQrPattern(tolerance: Double = 0.2): List<QrPattern> =
    (0 until height)
        .asSequence()
        .flatMap { y ->
            rowPixelSequence(y)
                .sections()
                .interleaved(5)
                .filter { it.isPattern(tolerance) }
                .map { QrPattern(it[2].center, y, it[0].unit.d, QrPattern.Direction.HORIZONTAL) }
        }
        .toList()

fun GrayU8.scanColumnsForQrPattern(tolerance: Double = 0.2): List<QrPattern> =
    (0 until width)
        .asSequence()
        .flatMap { x ->
            columnPixelSequence(x)
                .sections()
                .interleaved(5)
                .filter { it.isPattern(tolerance) }
                .map { QrPattern(x, it[2].center, it[0].unit.d, QrPattern.Direction.VERTICAL) }
        }
        .toList()

fun GrayU8.scanForQrPattern(tolerance: Double = 0.2): List<QrPattern> =
    scanRowsForQrPattern(tolerance) + scanColumnsForQrPattern(tolerance)

fun GrayU8.rowPixelSequence(row: Int): Sequence<Boolean> = sequence {
    for (x in 0 until width) {
        yield(get(x, row) != 0)
    }
}

fun GrayU8.columnPixelSequence(column: Int): Sequence<Boolean> = sequence {
    for (y in 0 until height) {
        yield(get(column, y) != 0)
    }
}
