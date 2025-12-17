package com.tofu.imagefusion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import imagefusion.composeapp.generated.resources.Res
import imagefusion.composeapp.generated.resources.poster1
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import qrgenerator.qrkitpainter.rememberQrKitPainter

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var detectionResult by remember { mutableStateOf<DetectionResult?>(null) }
        var isDetecting by remember { mutableStateOf(true) }
        var imageSize by remember { mutableStateOf(IntSize.Zero) }

        val qrPainter = rememberQrKitPainter(data = "https://yoursite.com")
        val density = LocalDensity.current

        // Detect green dot on launch
        LaunchedEffect(Unit) {
            isDetecting = true
            detectionResult = detectGreenDot("poster1")
            isDetecting = false
        }

        LaunchedEffect(Unit) {
            isDetecting = true
            detectionResult = detectGreenDot("poster1")

            // Debug logging
            detectionResult?.let {
                println("DEBUG: Green dot found at position (${it.position.x}, ${it.position.y}), size: ${it.size}")
            } ?: println("DEBUG: No green dot detected")

            isDetecting = false
        }

        Column(
            modifier = Modifier
                .background(Color.White)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }

            // Show loading indicator while detecting
            if (isDetecting) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            AnimatedVisibility(showContent && !isDetecting) {
                Box(modifier = Modifier.wrapContentSize().clipToBounds()) {
                    // Poster image
                    Image(
                        painter = painterResource(Res.drawable.poster1),
                        contentDescription = "Poster",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .wrapContentWidth()
                            .onSizeChanged { imageSize = it }
                    )

                    // QR code overlay at detected green dot position
                    detectionResult?.let { result ->
                        if (imageSize != IntSize.Zero) {
                            val qrSizePx = imageSize.width * 0.25f  // 20% of image width
                            val qrSizeDp = with(density) { qrSizePx.toDp() }

                            Image(
                                painter = qrPainter,
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .size(qrSizeDp)
                                    .offset {
                                        IntOffset(
                                            // Green dot marks the LEFT edge of QR, so don't subtract half width
                                            x = (imageSize.width * result.position.x - qrSizePx / 2).toInt(),
                                            // Green dot marks the vertical CENTER, so subtract half height
                                            y = (imageSize.height * result.position.y - qrSizePx / 2).toInt()
                                        )
                                    }
                            )
                        }
                    }

                    // Show message if no green dot found
                    if (detectionResult == null && !isDetecting) {
                        Text(
                            text = "No green dot detected",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}