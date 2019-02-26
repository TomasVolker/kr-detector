package tomasvolker.kr.boofcv

import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import tomasvolker.numeriko.core.primitives.indicator

fun DoubleArray2D.threshold(value: Double) =
        doubleArray2D(shape0, shape1) { i0, i1 -> (this[i0, i1] > value).indicator() }

