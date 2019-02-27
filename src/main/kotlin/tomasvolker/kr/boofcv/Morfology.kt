package tomasvolker.kr.boofcv

import boofcv.alg.filter.binary.BinaryImageOps
import boofcv.alg.filter.binary.BinaryImageOps.*
import boofcv.struct.image.GrayU8

fun GrayU8.erode4(times: Int = 1, destination: GrayU8? = null): GrayU8 =
        erode4(this, times, destination)

fun GrayU8.erode8(times: Int = 1, destination: GrayU8? = null): GrayU8 =
    erode8(this, times, destination)

fun GrayU8.dilate4(times: Int = 1, destination: GrayU8? = null): GrayU8 =
    dilate4(this, times, destination)

fun GrayU8.dilate8(times: Int = 1, destination: GrayU8? = null): GrayU8 =
    dilate8(this, times, destination)

fun GrayU8.close4(times: Int = 1, destination: GrayU8? = null): GrayU8 =
        this.dilate4(times)
            .erode4(times, destination)

fun GrayU8.close8(times: Int = 1, destination: GrayU8? = null): GrayU8 =
    this.dilate8(times)
        .erode8(times, destination)

fun GrayU8.open4(times: Int = 1, destination: GrayU8? = null): GrayU8 =
    this.erode4(times)
        .dilate4(times, destination)

fun GrayU8.open8(times: Int = 1, destination: GrayU8? = null): GrayU8 =
    this.erode8(times)
        .dilate8(times, destination)

fun GrayU8.edge4(destination: GrayU8? = null): GrayU8 =
    edge4(this, destination)

fun GrayU8.edge8(destination: GrayU8? = null): GrayU8 =
    edge8(this, destination)

fun GrayU8.invert(destination: GrayU8? = null): GrayU8 =
    invert(this, destination)

fun GrayU8.thin(maxIterations: Int = -1, destination: GrayU8? = null): GrayU8 =
    thin(this, maxIterations, destination)

fun GrayU8.removePointNoise(destination: GrayU8? = null) =
    removePointNoise(this, destination)

fun GrayU8.logicAnd(other: GrayU8, destination: GrayU8? = null) =
        logicAnd(this, other, destination)

fun GrayU8.logicOr(other: GrayU8, destination: GrayU8? = null) =
    logicOr(this, other, destination)

fun GrayU8.logicXor(other: GrayU8, destination: GrayU8? = null) =
    logicXor(this, other, destination)
