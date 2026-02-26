package com.xperia.prolog.camera

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.*
import android.util.Log
import android.view.Surface
import com.xperia.prolog.ui.ExposureState

@SuppressLint("MissingPermission")
class ProCameraManager(private val context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var requestBuilder: CaptureRequest.Builder? = null
    private var currentSurfaces: List<Surface> = emptyList()

    fun openCamera(lensId: String, surfaces: List<Surface>, onOpened: () -> Unit) {
        closeCamera()
        currentSurfaces = surfaces
        try {
            cameraManager.openCamera(lensId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    startCaptureSession(onOpened)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    closeCamera()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e("ProCameraManager", "Camera Error: $error")
                    closeCamera()
                }
            }, null)
        } catch (e: Exception) {
            Log.e("ProCameraManager", "Failed to open camera: ${e.message}")
        }
    }

    private fun startCaptureSession(onReady: () -> Unit) {
        val camera = cameraDevice ?: return
        val surfaces = currentSurfaces
        if (surfaces.isEmpty()) return
        
        try {
            requestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            surfaces.forEach { requestBuilder?.addTarget(it) }

            camera.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    onReady()
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e("ProCameraManager", "Capture session configure failed")
                }
            }, null)
        } catch (e: Exception) {
            Log.e("ProCameraManager", "Failed to create session: ${e.message}")
        }
    }

    fun applyExposureState(state: ExposureState) {
        val session = captureSession ?: return
        val builder = requestBuilder ?: return

        try {
            // Apply Manual Controls
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
            builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, state.shutterSpeed)
            builder.set(CaptureRequest.SENSOR_SENSITIVITY, state.iso)

            builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF)
            // AWB manual control typically requires interpolating color correction gains, 
            // but we turn off auto mode for now.

            builder.set(CaptureRequest.CONTROL_AF_MODE, state.focusMode)
            if (state.focusMode == CaptureRequest.CONTROL_AF_MODE_OFF) {
                builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, state.focusDistance)
            }

            session.setRepeatingRequest(builder.build(), null, null)
        } catch (e: Exception) {
            Log.e("ProCameraManager", "Failed to apply exposure: ${e.message}")
        }
    }

    fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
    }
}
