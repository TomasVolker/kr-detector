package tomasvolker.kr.algorithm.kdtree

import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import tomasvolker.numeriko.core.primitives.squared
import tomasvolker.openrndr.math.fromSides
import kotlin.math.max

fun KDTree.toIndentedString(indent: Int = 0): String = "|".repeat(indent) + when(this) {
    is KDList -> "-${values.size} values\n"
    is KDNode -> "-dim: $dimension less: $partition\n" + less.toIndentedString(indent+1) +
            "|".repeat(indent) + "-dim: $dimension greater: $partition\n" + greater.toIndentedString(indent+1)
}

fun KDTree.leafList(): List<List<Vector2>> = when(this) {
    is KDList -> listOf(values)
    is KDNode -> less.leafList() + greater.leafList()
}

fun KDTree.leafCount(): Int = when(this) {
    is KDList -> 1
    is KDNode -> less.leafCount() + greater.leafCount()
}

fun KDTree.pointCount(): Int = when(this) {
    is KDList -> values.size
    is KDNode -> less.pointCount() + greater.pointCount()
}

fun KDTree.nodeCount(): Int = when(this) {
    is KDList -> 0
    is KDNode -> 1 + less.nodeCount() + greater.nodeCount()
}

fun KDTree.emptyLeafs(): Int = when(this) {
    is KDList -> if (values.isEmpty()) 1 else 0
    is KDNode -> less.emptyLeafs() + greater.emptyLeafs()
}

fun KDTree.depth(): Int = when(this) {
    is KDList -> 0
    is KDNode -> 1 + max(less.depth(), greater.depth())
}

fun Rectangle.isEmpty(): Boolean =
        width <= 0.0 || height <= 0.0

fun KDTree.pointsInBounds(
    boundingBox: Rectangle
): List<Vector2> {

    if (boundingBox.isEmpty()) return emptyList()

    return when(this) {
        is KDList -> values.filter { it in boundingBox }
        is KDNode -> {
            val range = boundingBox.range(dimension)
            when {
                partition < range.min -> greater.pointsInBounds(boundingBox)
                range.max < partition -> less.pointsInBounds(boundingBox)
                else -> less.pointsInBounds(boundingBox) + greater.pointsInBounds(boundingBox)
            }
        }
    }
}

fun KDTree.pointsAround(
    point: Vector2,
    radius: Double
): List<Vector2> {

    val inBox = pointsInBounds(
        Rectangle.fromSides(
            left = point.x - radius,
            right = point.x + radius,
            top = point.y - radius,
            bottom = point.y + radius
        )
    )

    return inBox.filter { distanceLessThan(it, point, radius) }
}

fun distanceLessThan(point1: Vector2, point2: Vector2, distance: Double): Boolean =
    (point1.x - point2.x).squared() + (point1.y - point2.y).squared() < distance.squared()

fun KDTree.printStats() {

    println("""
        Point count: ${pointCount()}
        Leaf count: ${leafCount()}
        Node count: ${nodeCount()}
        Depth: ${depth()}
        Empty Leafs: ${emptyLeafs()}
    """.trimIndent())

}