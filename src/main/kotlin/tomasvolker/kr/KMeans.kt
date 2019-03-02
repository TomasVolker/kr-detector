package tomasvolker.kr

import tomasvolker.numeriko.core.dsl.D
import tomasvolker.numeriko.core.functions.norm2
import tomasvolker.numeriko.core.interfaces.array1d.double.DoubleArray1D
import tomasvolker.numeriko.core.interfaces.array1d.double.elementWise
import tomasvolker.numeriko.core.interfaces.factory.doubleArray1D
import tomasvolker.numeriko.core.interfaces.factory.nextGaussian
import java.lang.IllegalArgumentException
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

class KMeans<T>(val nClusters: Int,
                val featureExtractor: FeatureExtractor<T>,
                val initCentroids: List<T>): TrainableClassifier<T,T> {

    val centroids = mutableListOf<DoubleArray1D>().apply {
        if (initCentroids.size != nClusters)
            throw IllegalArgumentException("Invalid initial centroid configuration")
        else
            this.addAll(initCentroids.map(featureExtractor)) }
    private val labeledData = mutableListOf<LabeledVector>()
    private val dataFeaturesList = mutableListOf<Pair<T,DoubleArray1D>>()

    override fun fit(trainData: Iterable<T>) {
        val vectorData = trainData.map(featureExtractor)
        var oldCentroids = centroids.toList().shuffled()
        labeledData.clear()
        labeledData.addAll(List(vectorData.size) { i ->
            LabeledVector(vectorData[i], calculateClosestCentroid(vectorData[i]))
        })
        dataFeaturesList.clear()
        dataFeaturesList.addAll(List(labeledData.size) { i -> trainData.elementAt(i) to labeledData[i].value })

        while (centroidDiff(oldCentroids).also { println(it) } > 1E-2) {
            oldCentroids = centroids.toList()
            step()
            println(centroids)
        }
    }

    override fun step() {
        labeledData.forEach { data -> data.updateClosestCentroid() }

        for (i in 0 until centroids.size) {
            val dataCluster = labeledData.filter { it.closestCentroid == centroids[i] }

            if (dataCluster.isNotEmpty()) {
                val centroidNorm = centroids[i].norm2()

                centroids.removeAt(i)
                centroids.add(i,
                    dataCluster.fold(dataCluster[0].value)
                    { acc, vector -> acc + vector.value } / centroidNorm)
            }
        }
        /*centroids.replaceAll { centroid ->
            labeledData.filter { it.closestCentroid == centroid }
                .let {
                    if (it.isNotEmpty())
                        it.fold(it[0].value) { acc, vector -> acc + vector.value }.elementWise { it / centroid.norm2() }
                    else
                        centroid
                }
        }*/
    }

    override fun classify(testData: T): T =
        featureExtractor(testData).let { vector ->
            dataFeaturesList.find {
                it.second == labeledData.maxBy { (it.value - vector).norm2() }?.value ?: error("model not trained")
            }?.first ?: error("model not trained")
        }

    fun classify(testData: List<T>): List<T> =
        testData.map { classify(it) }

    private fun calculateClosestCentroid(vector: DoubleArray1D): DoubleArray1D =
        centroids.minBy { (it - vector).norm2() } ?: error("empty centroids")

    private fun centroidDiff(oldCentroids: List<DoubleArray1D>): Double =
            List(centroids.size) { i -> (centroids[i] - oldCentroids[i]).norm2() }.max() ?: error("empty centroids")

    fun clear() {
        centroids.clear()
        centroids.addAll(initCentroids.map(featureExtractor))
    }

    inner class LabeledVector(val value: DoubleArray1D, var closestCentroid: DoubleArray1D) {
        fun updateClosestCentroid() {
            closestCentroid = calculateClosestCentroid(value)
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