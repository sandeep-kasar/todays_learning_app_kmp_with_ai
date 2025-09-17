package com.todays.learning

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform