package com.tofu.imagefusion

import androidx.compose.ui.geometry.Offset
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi


data class DetectionResult(
    val position: Offset,
    val size: Float
)

@OptIn(ExperimentalResourceApi::class)
expect suspend fun detectGreenDot(resourcePath: String): DetectionResult?
