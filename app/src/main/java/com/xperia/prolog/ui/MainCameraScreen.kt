package com.xperia.prolog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainCameraScreen(
    cameraViewModel: CameraViewModel = viewModel()
) {
    val uiState by cameraViewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Preview Layer
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onSurfaceReady = { surface ->
                cameraViewModel.onSurfaceReady(surface)
            }
        )

        // UI Layer
        Box(modifier = Modifier.fillMaxSize()) {
            // Camera Overlays (Guides, Crosshairs)
            CameraOverlays(showFrameGuides = true)
            
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Status Bar (Battery, Storage, Codec info)
                TopStatusBar(
                    state = uiState,
                    onResolutionChanged = cameraViewModel::updateResolution,
                    onFrameRateChanged = cameraViewModel::updateFrameRate,
                    onColorProfileChanged = cameraViewModel::updateColorProfile,
                    onLensSelected = cameraViewModel::selectLens
                )

            Spacer(modifier = Modifier.weight(1f))

            // Lower thirds structure for 21:9 screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Exposure tray takes up bottom left/center
                Box(modifier = Modifier.weight(1f)) {
                    ControlTray(
                        exposureState = uiState,
                        onIsoChanged = cameraViewModel::updateIso,
                        onShutterChanged = cameraViewModel::updateShutter,
                        onFocusChanged = cameraViewModel::updateFocusDistance,
                        onWbChanged = cameraViewModel::updateWhiteBalance
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Record Button on right side
                RecordButton(
                    isRecording = uiState.isRecording,
                    onClick = cameraViewModel::toggleRecording
                )
            }
        }
    }
}

@Composable
fun TopStatusBar(
    state: ExposureState,
    onResolutionChanged: (Int, Int) -> Unit,
    onFrameRateChanged: (Int) -> Unit,
    onColorProfileChanged: (com.xperia.prolog.media.ColorProfile) -> Unit,
    onLensSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Resolution & FPS Dropdowns / Toggles
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = {
                // Toggle resolution 4K -> 1080p -> 4K
                if (state.resolution.width == 3840) {
                    onResolutionChanged(1920, 1080)
                } else {
                    onResolutionChanged(3840, 2160)
                }
            }) {
                Text(
                    text = if (state.resolution.width == 3840) "4K" else "1080p",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            TextButton(onClick = {
                // Cycle FPS: 24 -> 30 -> 60 -> 120 -> 24
                val nextFps = when (state.frameRate) {
                    24 -> 30
                    30 -> 60
                    60 -> 120
                    else -> 24
                }
                onFrameRateChanged(nextFps)
            }) {
                Text(
                    text = "${state.frameRate} FPS",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            TextButton(onClick = {
                // Cycle Color Profiles
                val nextProfile = when (state.colorProfile) {
                    com.xperia.prolog.media.ColorProfile.HLG_REC2020 -> com.xperia.prolog.media.ColorProfile.SLOG3_REC2020
                    com.xperia.prolog.media.ColorProfile.SLOG3_REC2020 -> com.xperia.prolog.media.ColorProfile.CINEON_LOG_REC2020
                    com.xperia.prolog.media.ColorProfile.CINEON_LOG_REC2020 -> com.xperia.prolog.media.ColorProfile.STANDARD_REC709
                    else -> com.xperia.prolog.media.ColorProfile.HLG_REC2020
                }
                onColorProfileChanged(nextProfile)
            }) {
                Text(
                    text = state.colorProfile.name.replace("_", " "),
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Lens Selector
        var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(
                    text = "LENS: ${state.currentLens?.focalLengths?.firstOrNull()?.let { "${it}mm" } ?: "Main"}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.DarkGray)
            ) {
                state.availableLenses.forEach { lens ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                "${lens.focalLengths.firstOrNull()?.let { "${it}mm" } ?: lens.id} (ID: ${lens.id})", 
                                color = Color.White 
                            ) 
                        },
                        onClick = {
                            onLensSelected(lens.id)
                            expanded = false
                        }
                    )
                }
            }
        }

        Text("1h 24m REMAINING", color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun RecordButton(isRecording: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRecording) Color.Red else Color.DarkGray
        ),
        modifier = Modifier.size(72.dp)
    ) {
        // Inner circle icon can be added here
        Box(
            modifier = Modifier
                .size(if (isRecording) 24.dp else 48.dp)
                .background(Color.Red, shape = if (isRecording) MaterialTheme.shapes.small else CircleShape)
        )
    }
}
