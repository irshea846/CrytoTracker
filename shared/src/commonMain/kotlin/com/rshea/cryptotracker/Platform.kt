package com.rshea.cryptotracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform