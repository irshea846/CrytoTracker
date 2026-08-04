package com.rshea.cryptotracker

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

/**
 * 2. Data Transfer Object (DTO) matching the API response structure
 */
@Serializable
data class CryptoAssetDto(
    val id: String,
    val symbol: String,
    val name: String,
    val current_price: Double,
    val market_cap: Double,
    val price_change_percentage_24h: Double
)

/**
 * 3. Service layer handling live network input streams
 */
class CryptoApiService {

    // CoinGecko API base URL
    private val baseUrl = "https://api.coingecko.com/api/v3"

    suspend fun fetchLiveMarketData(): String {
        val response = CryptoNetworkClient.httpClient.get("$baseUrl/coins/markets") {
            header("x-cg-demo-api-key", CryptoNetworkClient.MY_API_KEY)
            header("Accept", "application/json")
            url {
                parameters.append("vs_currency", "usd")
                parameters.append("ids", "bitcoin,ethereum,solana") // Example IDs
            }
        }
        return response.bodyAsText()
    }

}