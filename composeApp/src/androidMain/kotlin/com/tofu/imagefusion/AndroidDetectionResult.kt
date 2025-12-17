package com.tofu.imagefusion

import android.graphics.BitmapFactory
import androidx.compose.ui.geometry.Offset
import imagefusion.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
actual suspend fun detectGreenDot(resourcePath: String): DetectionResult? =
    withContext(Dispatchers.Default) {
        try {
            val bytes = Res.readBytes("drawable/$resourcePath.png")
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: return@withContext null

            var minX = Int.MAX_VALUE
            var minY = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE
            var maxY = Int.MIN_VALUE
            var found = false

            val width = bitmap.width
            val height = bitmap.height

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel = bitmap.getPixel(x, y)

                    val red = (pixel shr 16) and 0xFF
                    val green = (pixel shr 8) and 0xFF
                    val blue = pixel and 0xFF

                    if (isGreenPixel(red, green, blue)) {
                        found = true
                        minX = minOf(minX, x)
                        minY = minOf(minY, y)
                        maxX = maxOf(maxX, x)
                        maxY = maxOf(maxY, y)
                    }
                }
            }

            bitmap.recycle()

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
    // Allow some tolerance for anti-aliasing and compression artifacts
    return red < 50 &&
            green > 200 &&
            blue in 70..150
}