package com.xperia.prolog.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun CameraOverlays(
    showFrameGuides: Boolean = true,
    aspectRatio: Float = 2.39f, // Cinema Scope standard
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (showFrameGuides) {
            // Draw cinema guides (e.g. 2.39:1 over a 16:9 or 21:9 sensor feed)
            val expectedHeight = canvasWidth / aspectRatio
            val verticalOffset = (canvasHeight - expectedHeight) / 2f
            
            if (verticalOffset > 0) {
                // Top border
                drawRect(
                    color = Color.Black.copy(alpha = 0.5f),
                    topLeft = Offset(0f, 0f),
                    size = Size(canvasWidth, verticalOffset)
                )
                // Bottom border
                drawRect(
                    color = Color.Black.copy(alpha = 0.5f),
                    topLeft = Offset(0f, canvasHeight - verticalOffset),
                    size = Size(canvasWidth, verticalOffset)
                )
                
                // Guide Lines
                drawLine(
                    color = Color.White.copy(alpha = 0.8f),
                    start = Offset(0f, verticalOffset),
                    end = Offset(canvasWidth, verticalOffset),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.8f),
                    start = Offset(0f, canvasHeight - verticalOffset),
                    end = Offset(canvasWidth, canvasHeight - verticalOffset),
                    strokeWidth = 2f
                )
            }
            
            // Standard rule of thirds inner guides
            val thirdWidth = canvasWidth / 3f
            val thirdHeight = canvasHeight / 3f
            
            for (i in 1..2) {
                // Vertical lines
                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(thirdWidth * i, 0f),
                    end = Offset(thirdWidth * i, canvasHeight),
                    strokeWidth = 1f
                )
                // Horizontal lines
                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(0f, thirdHeight * i),
                    end = Offset(canvasWidth, thirdHeight * i),
                    strokeWidth = 1f
                )
            }
            
            // Center crosshair
            val crossSize = 40f
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(canvasWidth / 2f - crossSize / 2f, canvasHeight / 2f),
                end = Offset(canvasWidth / 2f + crossSize / 2f, canvasHeight / 2f),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(canvasWidth / 2f, canvasHeight / 2f - crossSize / 2f),
                end = Offset(canvasWidth / 2f, canvasHeight / 2f + crossSize / 2f),
                strokeWidth = 2f
            )
        }
    }
}
