package com.rshea.cryptotracker.util

actual fun randomUUID() = java.util.UUID.randomUUID().toString()