package com.tofu.imagefusion

import androidx.compose.ui.geometry.Offset
import imagefusion.composeapp.generated.resources.Res
import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.CoreFoundation.CFDataRef
import platform.CoreGraphics.*
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

@OptIn(ExperimentalResourceApi::class, ExperimentalForeignApi::class)
actual suspend fun detectQRPosition(resourcePath: String): DetectionResult? =
    withContext(Dispatchers.Default) {
        try {
            val bytes = Res.readBytes("drawable/$resourcePath.png")
            val nsData = bytes.usePinned { pinned ->
                NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
            }

            val dataProvider = CGDataProviderCreateWithCFData(nsData as CFDataRef)
            val image = CGImageCreateWithPNGDataProvider(dataProvider, null, true, CGColorRenderingIntent.kCGRenderingIntentDefault)
                ?: return@withContext null

            val width = CGImageGetWidth(image).toInt()
            val height = CGImageGetHeight(image).toInt()

            val colorSpace = CGColorSpaceCreateDeviceRGB()
            val bytesPerPixel = 4
            val bytesPerRow = bytesPerPixel * width
            val bitsPerComponent = 8

            val rawData = ByteArray(height * bytesPerRow)

            rawData.usePinned { pinned ->
                val context = CGBitmapContextCreate(
                    pinned.addressOf(0),
                    width.toULong(),
                    height.toULong(),
                    bitsPerComponent.toULong(),
                    bytesPerRow.toULong(),
                    colorSpace,
                    CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
                )
                CGContextDrawImage(context, CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()), image)
                CGContextRelease(context)
            }

            CGColorSpaceRelease(colorSpace)
            CGImageRelease(image)
            CGDataProviderRelease(dataProvider)

            var minX = Int.MAX_VALUE
            var minY = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE
            var maxY = Int.MIN_VALUE
            var found = false

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val index = (y * bytesPerRow) + (x * bytesPerPixel)
                    val red = rawData[index].toInt() and 0xFF
                    val green = rawData[index + 1].toInt() and 0xFF
                    val blue = rawData[index + 2].toInt() and 0xFF

                    if (detectQRPixels(red, green, blue)) {
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

@OptIn(ExperimentalResourceApi::class, ExperimentalForeignApi::class)
actual suspend fun detectReferralPosition(resourcePath: String): DetectionResult? =
    withContext(Dispatchers.Default) {
        try {
            val bytes = Res.readBytes("drawable/$resourcePath.png")
            val nsData = bytes.usePinned { pinned ->
                NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
            }

            val dataProvider = CGDataProviderCreateWithCFData(nsData as CFDataRef)
            val image = CGImageCreateWithPNGDataProvider(dataProvider, null, true, CGColorRenderingIntent.kCGRenderingIntentDefault)
                ?: return@withContext null

            val width = CGImageGetWidth(image).toInt()
            val height = CGImageGetHeight(image).toInt()

            val colorSpace = CGColorSpaceCreateDeviceRGB()
            val bytesPerPixel = 4
            val bytesPerRow = bytesPerPixel * width
            val bitsPerComponent = 8

            val rawData = ByteArray(height * bytesPerRow)

            rawData.usePinned { pinned ->
                val context = CGBitmapContextCreate(
                    pinned.addressOf(0),
                    width.toULong(),
                    height.toULong(),
                    bitsPerComponent.toULong(),
                    bytesPerRow.toULong(),
                    colorSpace,
                    CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
                )
                CGContextDrawImage(context, CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()), image)
                CGContextRelease(context)
            }

            CGColorSpaceRelease(colorSpace)
            CGImageRelease(image)
            CGDataProviderRelease(dataProvider)

            var minX = Int.MAX_VALUE
            var minY = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE
            var maxY = Int.MIN_VALUE
            var found = false

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val index = (y * bytesPerRow) + (x * bytesPerPixel)
                    val red = rawData[index].toInt() and 0xFF
                    val green = rawData[index + 1].toInt() and 0xFF
                    val blue = rawData[index + 2].toInt() and 0xFF

                    if (detectReferralPixels(red, green, blue)) {
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

private fun detectQRPixels(red: Int, green: Int, blue: Int): Boolean {
    // Target: #00FF6A (R:0, G:255, B:106)
    return red < 50 &&
            green > 200 &&
            blue in 70..150
}

private fun detectReferralPixels(red: Int, green: Int, blue: Int): Boolean {
    // Target: #0000FF (R:0, G:0, B:255)
    return red < 50 &&
            green < 50 &&
            blue > 200
}