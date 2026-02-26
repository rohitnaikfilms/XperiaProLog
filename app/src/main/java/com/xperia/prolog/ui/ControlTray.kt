package com.xperia.prolog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun ControlTray(
    exposureState: ExposureState,
    onIsoChanged: (Int) -> Unit,
    onShutterChanged: (Long) -> Unit,
    onFocusChanged: (Float) -> Unit,
    onWbChanged: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(16.dp)
    ) {
        // Shutter & ISO Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlItem(
                label = "ISO",
                value = exposureState.iso.toString(),
                onMinus = {
                    val currentIdx = STANDARD_ISOS.indexOf(exposureState.iso)
                    if (currentIdx > 0) onIsoChanged(STANDARD_ISOS[currentIdx - 1])
                },
                onPlus = {
                    val currentIdx = STANDARD_ISOS.indexOf(exposureState.iso)
                    if (currentIdx < STANDARD_ISOS.size - 1) onIsoChanged(STANDARD_ISOS[currentIdx + 1])
                }
            )

            ControlItem(
                label = "SHUTTER",
                value = formatShutter(exposureState.shutterSpeed),
                onMinus = {
                    val currentIdx = STANDARD_SHUTTERS.indexOf(exposureState.shutterSpeed)
                    if (currentIdx > 0) onShutterChanged(STANDARD_SHUTTERS[currentIdx - 1])
                },
                onPlus = {
                    val currentIdx = STANDARD_SHUTTERS.indexOf(exposureState.shutterSpeed)
                    if (currentIdx < STANDARD_SHUTTERS.size - 1) onShutterChanged(STANDARD_SHUTTERS[currentIdx + 1])
                }
            )
            
            ControlItem(
                label = "WB",
                value = "${exposureState.whiteBalanceKelvin}K",
                onMinus = { onWbChanged((exposureState.whiteBalanceKelvin - 100).coerceAtLeast(2000)) },
                onPlus = { onWbChanged((exposureState.whiteBalanceKelvin + 100).coerceAtMost(10000)) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Focus Slider Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("FOCUS", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = exposureState.focusDistance,
                onValueChange = onFocusChanged,
                valueRange = 0f..10f, // Map this to diopters for Camera2
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (exposureState.focusDistance == 0f) "INF" else String.format("%.1fm", 1f/exposureState.focusDistance), color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
fun ControlItem(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onMinus, contentPadding = PaddingValues(0.dp)) {
                Text("-", color = Color.White, fontSize = 18.sp)
            }
            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            TextButton(onClick = onPlus, contentPadding = PaddingValues(0.dp)) {
                Text("+", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

// Helper constants for cinema standard stops
val STANDARD_ISOS = listOf(50, 64, 100, 160, 200, 320, 400, 640, 800, 1250, 1600, 3200, 6400)
val STANDARD_SHUTTERS = listOf(
    1_000_000_000L / 24,
    1_000_000_000L / 48,  // 180 deg at 24fps
    1_000_000_000L / 50,
    1_000_000_000L / 60,
    1_000_000_000L / 120, // 180 deg at 60fps
    1_000_000_000L / 250,
    1_000_000_000L / 500,
    1_000_000_000L / 1000
).sorted()

fun formatShutter(nanos: Long): String {
    val denominator = (1_000_000_000.0 / nanos).roundToInt()
    return "1/$denominator"
}
