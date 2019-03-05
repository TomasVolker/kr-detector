package tomasvolker.kr.algorithm

import tomasvolker.numeriko.core.primitives.squared
import java.util.*


fun QrPattern.connectedTo(other: QrPattern): Boolean =
    ((this.x - other.x) / unitX).squared() + ((this.y - other.y) / unitY).squared() < 0.6.squared()

fun List<QrPattern>.cluster(): List<Set<QrPattern>> =
    connectedComponents(
        neighbors = { node -> this.filter { node.connectedTo(it) && it.connectedTo(node) } }
    )

fun <T> Iterable<T>.connectedComponents(
    neighbors: (T)->Iterable<T>,
    depthFirst: Boolean = false
): List<Set<T>> {

    val remaining = toMutableSet()

    val result = mutableListOf<Set<T>>()

    while(remaining.isNotEmpty()) {

        val component = connectedComponent(
            node = remaining.first(),
            neighbors = neighbors,
            depthFirst = depthFirst
        )

        result.add(component)
        remaining.removeAll(component)

    }

    return result
}

fun <T> connectedComponent(
    node: T,
    neighbors: (T)->Iterable<T>,
    depthFirst: Boolean = false
): Set<T> {

    val visited = mutableSetOf<T>()

    val queue = ArrayDeque<T>()
    queue.add(node)

    while(queue.isNotEmpty()) {

        val current = queue.pop()

        val newNodes = neighbors(current).filter { it !in visited }

        for(neighbor in newNodes) {

            if (neighbor !in visited) {
                visited.add(neighbor)

                if (depthFirst)
                    queue.addFirst(neighbor)
                else
                    queue.add(neighbor)

            }

        }

    }

    return visited
}
