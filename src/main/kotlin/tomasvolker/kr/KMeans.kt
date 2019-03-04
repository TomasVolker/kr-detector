package tomasvolker.kr

import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import tomasvolker.numeriko.core.dsl.D
import tomasvolker.numeriko.core.functions.norm2
import tomasvolker.numeriko.core.interfaces.array1d.double.DoubleArray1D
import tomasvolker.numeriko.core.interfaces.array1d.double.elementWise
import tomasvolker.numeriko.core.interfaces.factory.nextGaussian
import tomasvolker.openrndr.math.plot.plotScatter
import tomasvolker.openrndr.math.plot.quickPlot2D
import kotlin.random.Random


typealias FeatureExtractor<T> = (T) -> DoubleArray1D

interface ClusteringAlgorithm<T> {
    fun cluster(data: Iterable<T>): List<ClusterSet<T>>
    fun step()
}

data class ClusterSet<T>(
    val label: Int,
    val data: Iterable<T>,
    val centroid: DoubleArray1D
)

fun <T> List<T>.randomElement(random: Random = Random.Default): T =
        elementAt((random.nextDouble() * lastIndex).toInt())

fun <T> MutableList<T>.replace(other: List<T>) {
    for (i in 0 until other.size) {
        when (i) {
            in 0 until size -> { removeAt(i); add(i, other[i]) }
            else -> { add(other[i]) }
        }
    }
}

class KMeans<T>(val nClusters: Int,
                val featureExtractor: FeatureExtractor<T>,
                val initCentroids: List<T>? = null):
    ClusteringAlgorithm<T> {

    val centroids = mutableListOf<DoubleArray1D>().apply {
        if (initCentroids != null) {
            require(initCentroids.size == nClusters) {
                "Invalid initial centroid configuration"
            }
            addAll(initCentroids.map(featureExtractor))
        }
    }
    private val labeledData = mutableListOf<LabeledVector>()
    private val dataFeaturesList = mutableListOf<Pair<T,LabeledVector>>()
    private val randomDataVector = RandomDataVector()
    private var currMovement: Double = 100.0

    fun fit(trainData: Iterable<T>) {
        val vectorData = trainData.map(featureExtractor)

        if (initCentroids == null && centroids.isEmpty()) {
            centroids.apply {
                addAll(initCentroidsFromData(vectorData))
            }
        }

        labeledData.clear()
        labeledData.addAll(List(vectorData.size) { i ->
            LabeledVector(vectorData[i], calculateClosestCentroid(vectorData[i]))
        })
        dataFeaturesList.clear()
        dataFeaturesList.addAll(List(labeledData.size) { i -> trainData.elementAt(i) to labeledData[i] })

        while (currMovement > 1E-2) {
            currMovement = 0.0
            step()
        }
    }

    override fun step() {
        labeledData.forEach { data -> data.updateClosestCentroid() }

        for (i in 0 until centroids.size) {
            val dataCluster = labeledData.filter { it.closestCentroid == i }

            if (dataCluster.isNotEmpty()) {
                val newCentroid = dataCluster
                    .fold(dataCluster[0].value) { acc, vector -> acc + vector.value } / dataCluster.size
                currMovement += (newCentroid - centroids[i]).norm2()

                centroids[i] = newCentroid
            } else {
                centroids[i] = randomDataVector.nextDataVector()
            }
        }
    }

    override fun cluster(data: Iterable<T>): List<ClusterSet<T>> {
        fit(data)

        return List(nClusters) { i ->
            ClusterSet(
                label = i,
                data = dataFeaturesList.filter { it.second.closestCentroid == i }.map { it.first },
                centroid = centroids[i]
            )
        }
    }

    private fun initCentroidsFromData(data: List<DoubleArray1D>): List<DoubleArray1D> {
        val list = mutableListOf<DoubleArray1D>()

        while (list.size < nClusters) {
            val nextCentroid = data.randomElement()

            if (nextCentroid !in list)
                list.add(nextCentroid)
        }

        return list.toList()
    }

    /**
     * Finds the index of the closest centroid without the traversing the
     * centroid list twice (one for max and another for linear search of the
     * element
     *
     * @param vector: data in the feature domain
     */

    private fun calculateClosestCentroid(vector: DoubleArray1D): Int {
        require(centroids.isNotEmpty()) { "centroids is not initialized" }
        var result = -1
        var minDistance = 100.0

        for (i in 0 until centroids.size) {
            val currDistance = (vector - centroids[i]).norm2()
            if (minDistance > currDistance) {
                minDistance = currDistance
                result = i
            }
        }

        return result
    }

    fun reset(initCentroids: List<T>? = null, data: Iterable<T>) {
        centroids.apply {
            clear()
            addAll(initCentroids?.map(featureExtractor) ?: initCentroidsFromData(data.map(featureExtractor)))
        }
    }

    inner class LabeledVector(val value: DoubleArray1D, var closestCentroid: Int) {
        fun updateClosestCentroid() {
            closestCentroid = calculateClosestCentroid(value)
        }
    }

    inner class RandomDataVector {
        lateinit var maxVectors: DoubleArray1D
        lateinit var minVectors: DoubleArray1D

        fun nextDataVector(): DoubleArray1D {
            if ((!::maxVectors.isInitialized || !::minVectors.isInitialized) && labeledData.isNotEmpty()) {
                maxVectors = labeledData.map { it.value }.maxBy { it.norm2() } ?: error("labeled data is empty")
                minVectors = labeledData.map { it.value }.minBy { it.norm2() } ?: error("labeled data is empty")
            }

            return if (!::maxVectors.isInitialized || !::minVectors.isInitialized)
                error("Model run before fitting") // TODO: make an Exception later on
            else {
                (maxVectors - minVectors).elementWise { it * Random.nextDouble() } + minVectors
            }
        }

    }
}

fun main() {

    val nClusters = 4
    val initCentroids = listOf(D[5.0, 2.0], D[-4.0, 2.0], D[2.0, -6.0], D[-7.0, -1.0])
    val dataSet = List(50) { D[10.0 + 2.0 * Random.nextGaussian(), 10.0 + 2.0 * Random.nextGaussian()] } +
            List(50) { D[-10.0 + 2.0 * Random.nextGaussian(), 10.0 + 2.0 * Random.nextGaussian()] } +
            List(50) { D[10.0 + 2.0 * Random.nextGaussian(), -10.0 + 2.0 * Random.nextGaussian()] } +
            List(50) { D[-10.0 + 2.0 * Random.nextGaussian(), -10.0 + 2.0 * Random.nextGaussian()] }

    val featureExtractor = { data: DoubleArray1D -> data }

    val kmeans = KMeans<DoubleArray1D>(
        nClusters = 4,
        featureExtractor = featureExtractor,
        initCentroids = initCentroids
    )

    val clusteredData = kmeans.cluster(dataSet)

    println("centroids: ${kmeans.centroids.map{it}}")
    println("dataset 1: ${clusteredData[0].data}")
    println("dataset 2: ${clusteredData[1].data}")
    println("dataset 3: ${clusteredData[2].data}")
    println("dataset 4: ${clusteredData[3].data}")

}