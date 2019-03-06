package tomasvolker.kr.algorithm

data class QrPattern(
    val x: Int,
    val y: Int,
    val unit: Double,
    val direction: Direction
) {

    enum class Direction {
        HORIZONTAL,
        VERTICAL
    }

}