package com.xperia.prolog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.xperia.prolog.ui.MainCameraScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black // Dark theme optimized for filming
                ) {
                    var permissionsGranted by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

                    val permissionLauncher = androidx.compose.activity.compose.rememberLauncherForActivityResult(
                        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        permissionsGranted = permissions.values.all { it }
                    }

                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        permissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.CAMERA,
                                android.Manifest.permission.RECORD_AUDIO,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        )
                    }

                    if (permissionsGranted) {
                        MainCameraScreen()
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("Permissions are required to run Xperia ProLog.", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
