package com.example.bingebox

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform