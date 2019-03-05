package tomasvolker.kr.algorithm

import tomasvolker.numeriko.core.functions.inverse
import tomasvolker.numeriko.core.functions.matMul
import tomasvolker.numeriko.core.functions.outer
import tomasvolker.numeriko.core.functions.transpose
import tomasvolker.numeriko.core.interfaces.array1d.double.DoubleArray1D
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.elementWise
import tomasvolker.numeriko.core.interfaces.factory.doubleArray1D
import tomasvolker.numeriko.core.interfaces.factory.doubleIdentity
import tomasvolker.numeriko.core.interfaces.factory.doubleZeros
import tomasvolker.numeriko.core.interfaces.factory.nextGaussian
import kotlin.random.Random

class KalmanFilter(
    val transitionMatrix: DoubleArray2D,
    val processNoiseMatrix: DoubleArray2D,
    val measurementMatrix: DoubleArray2D,
    val measurementNoiseMatrix: DoubleArray2D,
    val processNoise: Double,
    val measurementNoise: Double
) {
    var estimatedX: DoubleArray1D = doubleZeros(transitionMatrix.shape0)
    var covarianceMatrix: DoubleArray2D = doubleIdentity(estimatedX.size).elementWise { it * 10.0 }
    val processCov: DoubleArray2D = doubleIdentity(estimatedX.size).elementWise { it * processNoise }
    val measurementCov: DoubleArray2D = doubleIdentity(estimatedX.size).elementWise { it * measurementNoise }

    fun step(measurement: DoubleArray1D): DoubleArray1D {
        predict()
        update(measurement)
        return estimatedX
    }

    fun predict() {
        estimatedX = (transitionMatrix matMul estimatedX)
        covarianceMatrix = (transitionMatrix matMul covarianceMatrix matMul transitionMatrix.transpose()) + processCov
    }

    fun update(measurement: DoubleArray1D) {
        val innovation = measurement - (measurementMatrix matMul estimatedX)
        val innovationCov = measurementCov +
                (measurementMatrix matMul covarianceMatrix matMul measurementMatrix.transpose())
        val gain = covarianceMatrix matMul measurementMatrix.transpose() matMul innovationCov.inverse()
        estimatedX += (gain matMul innovation)
        covarianceMatrix = (doubleIdentity(covarianceMatrix.shape0) - (gain matMul measurementMatrix)) matMul
                covarianceMatrix matMul (doubleIdentity(covarianceMatrix.shape0)
                - (gain matMul measurementMatrix)).transpose() + (gain matMul measurementCov matMul gain.transpose())
    }

    fun nextProcessNoise() =
            doubleArray1D(estimatedX.size) { processNoise * Random.nextGaussian() }

    fun nextMeasurementNoise() =
            doubleArray1D(estimatedX.size) { measurementNoise * Random.nextGaussian() }
}