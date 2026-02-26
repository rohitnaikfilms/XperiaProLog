package com.xperia.prolog.camera

import android.hardware.camera2.CameraCharacteristics
import android.util.Size
import android.util.Range

data class LensInfo(
    val id: String,
    val isLogical: Boolean,
    val physicalIds: Set<String> = emptySet(),
    val focalLengths: FloatArray = floatArrayOf(),
    val physicalSize: android.util.SizeF? = null,
    val isoRange: Range<Int>? = null,
    val shutterSpeedRange: Range<Long>? = null,
    val formatSizes: Map<Int, List<Size>> = emptyMap(),
    val supports10Bit: Boolean = false,
    val isBackFacing: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LensInfo
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
