package com.rshea.cryptotracker.data

import com.rshea.cryptotracker.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 1. Shared HTTP Client Configuration
 * Ktor automatically selects OkHttp on Android and Darwin on iOS behind the scenes.
 */
object CryptoNetworkClient {
    const val MY_API_KEY = BuildConfig.MY_API_KEY
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true // CRITICAL: Prevents app crashes if the API adds new fields
            })
        }
    }
}