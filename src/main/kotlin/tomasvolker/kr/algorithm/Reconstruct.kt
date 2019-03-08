package tomasvolker.kr.algorithm

import boofcv.struct.image.GrayU8
import tomasvolker.kr.geometry.*
import java.util.*

fun GrayU8.reconstruct(
    seed: Point,
    neighborhood: (Point)->List<Point> = Point::neighboors8,
    invertImage: Boolean = false,
    destination: GrayU8? = null
) = reconstruct(
    seeds = listOf(seed),
    neighborhood = neighborhood,
    invertImage = invertImage,
    destination = destination
)

fun GrayU8.reconstruct(
    seeds: List<Point>,
    neighborhood: (Point)->List<Point> = Point::neighboors8,
    invertImage: Boolean = false,
    destination: GrayU8? = null
): GrayU8 {

    val result = destination ?: createSameShape()

    val frontier = ArrayDeque<Point>()

    seeds
        .filter { isInBounds(it) }
        .forEach {
            result[it] = 1
            frontier.add(it)
        }

    while(frontier.isNotEmpty()) {

        val value = frontier.pop()

        for(neighbor in neighborhood(value)) {

            if(isInBounds(neighbor)) {

                val imageValue = (this[neighbor] != 0) xor invertImage
                val resultValue = result[neighbor] != 0

                if (!resultValue && imageValue) {
                    result[neighbor] = 1
                    frontier.add(neighbor)
                }

            }
        }

    }

    return result
}

