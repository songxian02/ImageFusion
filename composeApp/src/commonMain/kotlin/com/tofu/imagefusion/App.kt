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
import androidx.compose.ui.text.font.FontWeight
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
        var greenDotResult by remember { mutableStateOf<DetectionResult?>(null) }
        var grayDotResult by remember { mutableStateOf<DetectionResult?>(null) }
        var isDetecting by remember { mutableStateOf(true) }
        var imageSize by remember { mutableStateOf(IntSize.Zero) }

        val qrPainter = rememberQrKitPainter(data = "https://yoursite.com")
        val referralCode = "SKIBIDI"
        val density = LocalDensity.current

        // Detect both dots on launch
        LaunchedEffect(Unit) {
            isDetecting = true
            greenDotResult = detectQRPosition("poster1")
            grayDotResult = detectReferralPosition("poster1")

            // Debug logging
            greenDotResult?.let {
                println("DEBUG: Green dot found at position (${it.position.x}, ${it.position.y}), size: ${it.size}")
            } ?: println("DEBUG: No green dot detected")

            grayDotResult?.let {
                println("DEBUG: Gray dot found at position (${it.position.x}, ${it.position.y}), size: ${it.size}")
            } ?: println("DEBUG: No gray dot detected")

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
                    greenDotResult?.let { result ->
                        if (imageSize != IntSize.Zero) {
                            val qrSizePx = imageSize.width * 0.25f
                            val qrSizeDp = with(density) { qrSizePx.toDp() }

                            Image(
                                painter = qrPainter,
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .size(qrSizeDp)
                                    .offset {
                                        IntOffset(
                                            x = (imageSize.width * result.position.x - qrSizePx / 2).toInt(),
                                            y = (imageSize.height * result.position.y - qrSizePx / 2).toInt()
                                        )
                                    }
                            )
                        }
                    }

                    // Referral code at detected gray dot position
                    // Referral code at detected magenta dot position
                    grayDotResult?.let { result ->
                        if (imageSize != IntSize.Zero) {
                            var textSize by remember { mutableStateOf(IntSize.Zero) }
                            val fontSize = imageSize.width * 0.04f

                            Text(
                                text = referralCode,
                                color = Color.Black,
                                fontSize = with(density) { fontSize.toSp() },
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .onSizeChanged { textSize = it }
                                    .offset {
                                        IntOffset(
                                            x = (imageSize.width * result.position.x - textSize.width / 2).toInt(),
                                            y = (imageSize.height * result.position.y - textSize.height / 2).toInt()
                                        )
                                    }
                            )
                        }
                    }

                    // Show message if no dots found
                    if (greenDotResult == null && grayDotResult == null && !isDetecting) {
                        Text(
                            text = "No dots detected",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}