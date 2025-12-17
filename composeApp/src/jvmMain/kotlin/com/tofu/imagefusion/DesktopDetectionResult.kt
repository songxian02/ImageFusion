package com.tofu.imagefusion

// src/desktopMain/kotlin/ImageUtils.desktop.kt

import androidx.compose.ui.geometry.Offset
import imagefusion.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

@OptIn(ExperimentalResourceApi::class)
actual suspend fun detectGreenDot(resourcePath: String): DetectionResult? =
    withContext(Dispatchers.Default) {
        try {
            val bytes = Res.readBytes("drawable/$resourcePath.png")
            val image = ImageIO.read(ByteArrayInputStream(bytes))
                ?: return@withContext null

            var minX = Int.MAX_VALUE
            var minY = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE
            var maxY = Int.MIN_VALUE
            var found = false

            val width = image.width
            val height = image.height

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val rgb = image.getRGB(x, y)

                    val red = (rgb shr 16) and 0xFF
                    val green = (rgb shr 8) and 0xFF
                    val blue = rgb and 0xFF

                    if (isGreenPixel(red, green, blue)) {
                        found = true
                        minX = minOf(minX, x)
                        minY = minOf(minY, y)
                        maxX = maxOf(maxX, x)
                        maxY = maxOf(maxY, y)
                    }
                }
            }

            if (found) {
                val centerX = (minX + maxX) / 2f
                val centerY = (minY + maxY) / 2f
                val dotWidth = (maxX - minX).toFloat()

                DetectionResult(
                    position = Offset(centerX / width, centerY / height),
                    size = dotWidth / width
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

private fun isGreenPixel(red: Int, green: Int, blue: Int): Boolean {
    // Target: #00FF6A (R:0, G:255, B:106)
    return red < 50 &&
            green > 200 &&
            blue in 70..150
}