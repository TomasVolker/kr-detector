package tomasvolker.kr

import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.Vector4

fun Vector2.toHomogeneous() = Vector3(x = x, y = y, z = 1.0)
fun Vector3.toHomogeneous() = Vector4(x = x, y = y, z = z, w = 1.0)

val Vector4.xy get() = Vector2(x, y)
