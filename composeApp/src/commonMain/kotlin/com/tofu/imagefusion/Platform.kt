package com.tofu.imagefusion

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform