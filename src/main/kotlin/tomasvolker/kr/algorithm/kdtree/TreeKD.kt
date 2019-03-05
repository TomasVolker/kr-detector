package tomasvolker.kr.algorithm.kdtree

import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import tomasvolker.kr.algorithm.maxDouble
import tomasvolker.kr.algorithm.minDouble
import tomasvolker.kr.openrndr.get
import tomasvolker.openrndr.math.*
import kotlin.math.max
import kotlin.math.min

sealed class KDTree

class KDNode(
    val dimension: Int = 0,
    val partition: Double = 0.0,
    val less: KDTree = KDList(),
    val greater: KDTree = KDList()
): KDTree()

class KDList(
    val values: List<Vector2> = emptyList()
): KDTree()


data class SpacePartition(
    val dimension: Int,
    val value: Double
)

typealias PartitionPolicy = (points: List<Vector2>, bounds: Rectangle)->SpacePartition?

class MidRangePolicy(val maxSize: Int): PartitionPolicy {

    override fun invoke(points: List<Vector2>, bounds: Rectangle): SpacePartition? {

        if (points.size <= maxSize) return null

        val (dimension, maxRange) = bounds.rangeList
            .asSequence()
            .mapIndexed { i, range -> i to range }
            .maxBy { it.second.length } ?: error("Rangelist cannot be empty")

        return SpacePartition(dimension, maxRange.centroid)
    }

}

fun List<Vector2>.computeBounds(): Rectangle =
    Rectangle.fromSides(
        left = minDouble { it.x },
        right = maxDouble { it.y },
        top = minDouble { it.y },
        bottom = maxDouble { it.y }
    )

fun List<Vector2>.buildKdTree(
    bounds: Rectangle = computeBounds(),
    partitionPolicy: PartitionPolicy = MidRangePolicy(maxSize = 100)
): KDTree {

    return when {

        size <= 1 -> KDList(this)

        else -> {
            val (dimension, partition) = partitionPolicy(this, bounds) ?: return KDList(this)

            val (less, greater) = this.partition { it[dimension] < partition }

            val (lowerBounds, upperBounds) = bounds.split(dimension, partition)

            return KDNode(
                dimension = dimension,
                partition = partition,
                less = less.buildKdTree(lowerBounds, partitionPolicy),
                greater = greater.buildKdTree(upperBounds, partitionPolicy)
            )
        }
    }

}

fun Rectangle.split(dimension: Int, value: Double): Pair<Rectangle, Rectangle> =
    kotlin.Pair(
        Rectangle.fromRangeList(
            rangeList.mapIndexed { i, range ->
                if (i == dimension)
                    range.min .. min(range.max, value)
                else
                    range
            }
        ),
        Rectangle.fromRangeList(
            rangeList.mapIndexed { i, range ->
                if (i == dimension)
                    max(range.min, value) .. range.max
                else
                    range
            }
        )
    )