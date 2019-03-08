package tomasvolker.kr.geometry

import org.openrndr.math.Vector2
import org.openrndr.math.Vector3

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
