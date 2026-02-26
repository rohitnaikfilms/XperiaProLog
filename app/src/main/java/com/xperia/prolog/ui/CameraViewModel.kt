package com.xperia.prolog.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import android.hardware.camera2.CameraCharacteristics
import android.util.Range
import android.view.Surface
import com.xperia.prolog.camera.CameraDeviceManager
import com.xperia.prolog.camera.LensInfo
import com.xperia.prolog.camera.ProCameraManager
import com.xperia.prolog.media.EncoderConfig
import com.xperia.prolog.media.VideoEncoder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ExposureState(
    val iso: Int = 100,
    val isoRange: Range<Int> = Range(100, 3200),
    val shutterSpeed: Long = 1_000_000_000L / 60, // 1/60th sec in nanos
    val shutterRange: Range<Long> = Range(1_000_000L, 1_000_000_000L),
    val focusDistance: Float = 0f, // 0 = infinity
    val focusMode: Int = CameraCharacteristics.CONTROL_AF_MODE_OFF,
    val whiteBalanceKelvin: Int = 5500,
    val resolution: android.util.Size = android.util.Size(3840, 2160),
    val frameRate: Int = 60,
    val colorProfile: com.xperia.prolog.media.ColorProfile = com.xperia.prolog.media.ColorProfile.HLG_REC2020,
    val isRecording: Boolean = false,
    val currentLens: LensInfo? = null,
    val availableLenses: List<LensInfo> = emptyList()
)

import com.xperia.prolog.media.GL10BitRenderer

class CameraViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ExposureState())
    val uiState: StateFlow<ExposureState> = _uiState.asStateFlow()

    private val cameraDeviceManager = CameraDeviceManager(application.applicationContext)
    private val proCameraManager = ProCameraManager(application.applicationContext)
    private var videoEncoder: VideoEncoder? = null
    private var glRenderer: GL10BitRenderer? = null
    private var previewSurface: Surface? = null

    init {
        val lenses = cameraDeviceManager.getAvailableLenses()
        setLenses(lenses)
    }

    fun onSurfaceReady(surface: Surface) {
        previewSurface = surface
        startCameraPreview()
    }

    private fun startCameraPreview() {
        val lens = _uiState.value.currentLens
        val surface = previewSurface
        if (lens != null && surface != null) {
            proCameraManager.openCamera(lens.id, listOf(surface)) {
                proCameraManager.applyExposureState(_uiState.value)
            }
        }
    }

    fun updateIso(newIso: Int) {
        _uiState.update { it.copy(iso = newIso) }
        proCameraManager.applyExposureState(_uiState.value)
    }

    fun updateShutter(newShutterNanos: Long) {
        _uiState.update { it.copy(shutterSpeed = newShutterNanos) }
        proCameraManager.applyExposureState(_uiState.value)
    }

    fun updateFocusDistance(distance: Float) {
        _uiState.update { it.copy(focusDistance = distance, focusMode = CameraCharacteristics.CONTROL_AF_MODE_OFF) }
        proCameraManager.applyExposureState(_uiState.value)
    }

    fun updateWhiteBalance(kelvin: Int) {
         _uiState.update { it.copy(whiteBalanceKelvin = kelvin) }
         proCameraManager.applyExposureState(_uiState.value)
    }

    fun updateResolution(width: Int, height: Int) {
        _uiState.update { it.copy(resolution = android.util.Size(width, height)) }
        startCameraPreview()
    }

    fun updateFrameRate(fps: Int) {
        _uiState.update { it.copy(frameRate = fps) }
        proCameraManager.applyExposureState(_uiState.value)
    }

    fun updateColorProfile(profile: com.xperia.prolog.media.ColorProfile) {
        _uiState.update { it.copy(colorProfile = profile) }
    }

    private fun setLenses(lenses: List<LensInfo>) {
        val defaultLens = lenses.firstOrNull { it.isBackFacing && it.focalLengths.contains(24f) } ?: lenses.firstOrNull()
        
        _uiState.update { 
            it.copy(
                availableLenses = lenses,
                currentLens = defaultLens,
                isoRange = defaultLens?.isoRange ?: Range(100, 3200),
                shutterRange = defaultLens?.shutterSpeedRange ?: Range(1_000_000L, 1_000_000_000L)
            ) 
        }
    }

    fun selectLens(lensId: String) {
        val lens = _uiState.value.availableLenses.find { it.id == lensId }
        if (lens != null) {
            _uiState.update { 
                it.copy(
                    currentLens = lens,
                    isoRange = lens.isoRange ?: Range(100, 3200),
                    shutterRange = lens.shutterSpeedRange ?: Range(1_000_000L, 1_000_000_000L)
                ) 
            }
            startCameraPreview()
        }
    }

    fun toggleRecording() {
        val currentState = _uiState.value
        if (!currentState.isRecording) {
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
            val profileSuffix = currentState.colorProfile.name
            val filename = "VID_${timestamp}_$profileSuffix.mp4"
            
            val moviesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MOVIES)
            val appDir = java.io.File(moviesDir, "XperiaProLog")
            if (!appDir.exists()) appDir.mkdirs()
            
            val outputFile = java.io.File(appDir, filename)
            android.util.Log.d("CameraViewModel", "Starting actual video recording to: ${outputFile.absolutePath}")
            
            val config = EncoderConfig(
                width = currentState.resolution.width,
                height = currentState.resolution.height,
                frameRate = currentState.frameRate,
                profile = currentState.colorProfile
            )
            
            try {
                videoEncoder = VideoEncoder(config, outputFile)
                val encoderSurface = videoEncoder?.prepare() ?: return
                
                glRenderer = GL10BitRenderer(currentState.colorProfile)
                val cameraRecordingSurface = glRenderer?.prepare(encoderSurface) ?: return
                
                videoEncoder?.start()
                _uiState.update { it.copy(isRecording = true) }

                val surfaces = mutableListOf<Surface>()
                previewSurface?.let { surfaces.add(it) }
                surfaces.add(cameraRecordingSurface)
                
                val lens = _uiState.value.currentLens
                if (lens != null && surfaces.isNotEmpty()) {
                    proCameraManager.openCamera(lens.id, surfaces) {
                        proCameraManager.applyExposureState(_uiState.value)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CameraViewModel", "Failed to start recording, aborting", e)
            }
            
        } else {
            android.util.Log.d("CameraViewModel", "Stopping recording and saving file.")
            videoEncoder?.stop()
            videoEncoder = null
            
            glRenderer?.release()
            glRenderer = null
            
            _uiState.update { it.copy(isRecording = false) }
            startCameraPreview()
        }
    }

    override fun onCleared() {
        super.onCleared()
        proCameraManager.closeCamera()
        videoEncoder?.stop()
        glRenderer?.release()
    }
}
