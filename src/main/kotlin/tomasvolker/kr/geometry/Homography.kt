package tomasvolker.kr.geometry

import org.openrndr.math.Vector2
import tomasvolker.numeriko.core.dsl.D
import tomasvolker.numeriko.core.functions.inverse
import tomasvolker.numeriko.core.functions.matMul
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleIdentity

data class Homography(
    val matrix: DoubleArray2D
): (Vector2)->Vector2 {
    
    init {
        require(matrix.shape0 == 3 && matrix.shape1 == 3)
    }

    override fun invoke(input: Vector2): Vector2 =
        (matrix matMul D[input.x, input.y, 1.0]).let { Vector2(it[0] / it[2], it[1] / it[2]) }

    fun inverse() = Homography(matrix.inverse())

    companion object {
        val IDENTITY = Homography(doubleIdentity(3))
    }

}


