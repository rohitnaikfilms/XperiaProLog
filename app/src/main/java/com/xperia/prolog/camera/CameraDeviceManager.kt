package com.xperia.prolog.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.DynamicRangeProfiles
import android.util.Log

class CameraDeviceManager(context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    fun getAvailableLenses(): List<LensInfo> {
        val lenses = mutableListOf<LensInfo>()
        
        try {
            val cameraIdList = cameraManager.cameraIdList
            for (id in cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(id)
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                val isBackFacing = facing == CameraCharacteristics.LENS_FACING_BACK
                
                // Xperia 1 V specific: We care primarily about the back-facing logical camera and its physical lenses.
                if (!isBackFacing) continue

                val capabilities = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES) ?: intArrayOf()
                val isLogical = capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA)
                
                val physicalIds = if (isLogical) {
                    chars.physicalCameraIds
                } else {
                    emptySet()
                }

                // Focal length and sensor size
                val focalLengths = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS) ?: floatArrayOf()
                val physicalSize = chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                
                // Exposure ranges
                val isoRange = chars.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
                val shutterRange = chars.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)

                // 10-bit support check (Android 13+ DynamicRangeProfiles)
                var supports10Bit = false
                val dynamicRangeProfiles = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_DYNAMIC_RANGE_PROFILES)
                if (dynamicRangeProfiles != null) {
                    val supportedProfiles = dynamicRangeProfiles.supportedProfiles
                    if (supportedProfiles.contains(DynamicRangeProfiles.HLG10)) {
                        supports10Bit = true
                    }
                }

                val configMap = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val formatSizes = mutableMapOf<Int, List<android.util.Size>>()
                configMap?.let {
                    // YUV_420_888 for preview/processing
                    formatSizes[ImageFormat.YUV_420_888] = it.getOutputSizes(ImageFormat.YUV_420_888)?.toList() ?: emptyList()
                    // RAW10 or RAW_SENSOR for custom extraction (if needed)
                    formatSizes[ImageFormat.RAW_SENSOR] = it.getOutputSizes(ImageFormat.RAW_SENSOR)?.toList() ?: emptyList()
                    // PRIVATE for MediaCodec/Surface
                    formatSizes[ImageFormat.PRIVATE] = it.getOutputSizes(ImageFormat.PRIVATE)?.toList() ?: emptyList()
                }

                val lensInfo = LensInfo(
                    id = id,
                    isLogical = isLogical,
                    physicalIds = physicalIds,
                    focalLengths = focalLengths,
                    physicalSize = physicalSize,
                    isoRange = isoRange,
                    shutterSpeedRange = shutterRange,
                    formatSizes = formatSizes,
                    supports10Bit = supports10Bit,
                    isBackFacing = true
                )
                
                lenses.add(lensInfo)
                Log.d("CameraDeviceManager", "Discovered Lens: $lensInfo")
            }
        } catch (e: Exception) {
            Log.e("CameraDeviceManager", "Failed to query cameras", e)
        }
        return lenses
    }
}
