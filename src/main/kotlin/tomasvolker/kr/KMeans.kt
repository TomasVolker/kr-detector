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

interface Classifier<R,T> {
    fun classify(testData: T): R
}

interface TrainableClassifier<R,T>: Classifier<R,T> {
    override fun classify(testData: T): R
    fun fit(trainData: Iterable<T>)
    fun step()
}

fun <T> Iterable<T>.randomElement(random: Random = Random.Default): T =
        elementAt(random.nextInt() * indexOf(last()))

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
                val initCentroids: List<T>): TrainableClassifier<T,T> {

    val centroids = mutableListOf<DoubleArray1D>().apply {
        require(initCentroids.size == nClusters) {
            "Invalid initial centroid configuration"
        }
        addAll(initCentroids.map(featureExtractor))
    }
    private val labeledData = mutableListOf<LabeledVector>()
    private val dataFeaturesList = mutableListOf<Pair<T,DoubleArray1D>>() // TODO: just keep 1 per centroid
    private val randomDataVector = RandomDataVector()
    private val currCentroids = mutableListOf<DoubleArray1D>()

    override fun fit(trainData: Iterable<T>) {
        val vectorData = trainData.map(featureExtractor)
        labeledData.clear()
        labeledData.addAll(List(vectorData.size) { i ->
            LabeledVector(vectorData[i], calculateClosestCentroid(vectorData[i]))
        })
        dataFeaturesList.clear()
        dataFeaturesList.addAll(List(labeledData.size) { i -> trainData.elementAt(i) to labeledData[i].value })
        currCentroids.apply { this.addAll(List(centroids.size) { randomDataVector.nextDataVector() }) }

        quickPlot2D {
            currCentroids.replace(centroids.toList())
            step()
            fill = ColorRGBa.RED
            plotScatter(
                labeledData.map { Vector2(it.value[0], it.value[1]) }
            )
            centroids.forEach {
                rectangle(Rectangle.fromCenter(Vector2(it[0], it[1]), 1.0, 1.0))
            }
        }

        /*while (centroidDiff(currCentroids) > 1E-2) {
            currCentroids = centroids.toList()
            step()
        }*/
    }

    override fun step() {
        labeledData.forEach { data -> data.updateClosestCentroid() }

        for (i in 0 until centroids.size) {
            val dataCluster = labeledData.filter { it.closestCentroid == i }

            if (dataCluster.isNotEmpty()) {
                currCentroids[i] = dataCluster
                    .fold(dataCluster[0].value) { acc, vector -> acc + vector.value } / dataCluster.size
            } else {
                currCentroids[i] = randomDataVector.nextDataVector()
                println("empty centroid: $i")
            }
        }

        centroids.replace(currCentroids)
    }

    // TODO: re write without the linear search
    override fun classify(testData: T): T =
        featureExtractor(testData).let { vector ->
            dataFeaturesList.find {
                it.second == labeledData.maxBy { (it.value - vector).norm2() }?.value ?: error("model not trained")
            }?.first ?: error("model not trained")
        }

    fun classify(testData: List<T>): List<T> =
        testData.map { classify(it) }

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

    private fun centroidDiff(oldCentroids: List<DoubleArray1D>): Double =
            List(centroids.size) { i -> (centroids[i] - oldCentroids[i]).norm2() }.max() ?: error("empty centroids")

    fun clear() {
        centroids.clear()
        centroids.addAll(initCentroids.map(featureExtractor))
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
    ).also { it.fit(dataSet) }

    println("centroids: ${kmeans.centroids.map{it}}")

}