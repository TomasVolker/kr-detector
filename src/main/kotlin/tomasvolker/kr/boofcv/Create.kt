package tomasvolker.kr.boofcv

import boofcv.struct.image.GrayU8

inline fun grayU8(
    width: Int,
    height: Int,
    init: (x: Int, y: Int)->Int
): GrayU8 = GrayU8(width, height).apply {
    for (y in 0 until height) {
        for (x in 0 until width) {
            this[x, y] = init(x, y)
        }
    }
}