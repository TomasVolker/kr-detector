package tomasvolker.kr.algorithms

import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import tomasvolker.numeriko.core.dsl.D
import tomasvolker.numeriko.core.functions.inverse
import tomasvolker.numeriko.core.functions.matMul
import tomasvolker.numeriko.core.functions.solve
import tomasvolker.numeriko.core.functions.transpose
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.operations.concat
import tomasvolker.numeriko.core.operations.stack

data class Line(
    val homogeneos: Vector3
) {

    companion object {

        fun fromHomogeneos(
            x: Double,
            y: Double,
            z: Double
        ) = Line(Vector3(x, y, z))

        fun fromPointAndDirection(
            point: Vector2,
            direction: Vector2
        ) = fromHomogeneos(
            x = -direction.y,
            y = direction.x,
            z = point.x * direction.y - point.y * direction.x
        )
    }

}

infix fun Line.intersection(other: Line): Vector2 =
    (this.homogeneos cross other.homogeneos).let { Vector2(it.x / it.z, it.y / it.z) }

data class Homography(
    val matrix: DoubleArray2D
): (Vector2)->Vector2 {
    
    init {
        require(matrix.shape0 == 3 && matrix.shape1 == 3)
    }

    override fun invoke(input: Vector2): Vector2 =
        (matrix matMul D[input.x, input.y, 1.0]).let { Vector2(it[0] / it[2], it[1] / it[2]) }

    fun inverse() = Homography(matrix.inverse())

}

fun List<MarkerCorners>.projectCorner(): Vector2 {

    val corner0 = this[0].corners[0]
    val corner1 = this[1].corners[1]
    val corner2 = this[2].corners[2]

    val bottomLine = Line.fromPointAndDirection(
        point = corner2,
        direction = this[2].corners[3] - corner2
    )

    val rightLine = Line.fromPointAndDirection(
        point = corner1,
        direction = this[1].corners[3] - corner1
    )

    return bottomLine intersection rightLine
}


fun List<MarkerCorners>.computeHomography(): Homography {

     val mappings = this.mapIndexed { i, marker ->

        val offset = when(i) {
            0 -> Vector2(0.0, 0.0)
            1 -> Vector2(18.0, 0.0)
            2 -> Vector2(0.0, 18.0)
            else -> error("input is not of size 3")
        }

        listOf(
            offset to marker.corners[0],
            (offset + Vector2(7.0, 0.0)) to marker.corners[1],
            (offset + Vector2(0.0, 7.0)) to marker.corners[2],
            (offset + Vector2(7.0, 7.0)) to marker.corners[3]
        )
    }.flatten()

    val matrix = mappings.flatMap {
        val local = it.first
        val image = it.second
        listOf(
            D[local.x, local.y, 1.0, 0.0    , 0.0    , 0.0, - image.x * local.x, - image.x * local.y],
            D[0.0    , 0.0    , 0.0, local.x, local.y, 1.0, - image.y * local.x, - image.y * local.y]
        )
    }.stack()

    val result = mappings
        .map { D[it.second.x, it.second.y] }
        .reduce { acc, array1D -> acc concat array1D }

    val entries = (matrix.transpose() matMul matrix).solve(matrix.transpose() matMul result)
    
    return Homography(
        D[D[entries[0], entries[1], entries[2]],
          D[entries[3], entries[4], entries[5]],
          D[entries[6], entries[7],        1.0]]
    )
}
