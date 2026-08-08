package com.rshea.cryptotracker.data

import com.rshea.cryptotracker.domain.CryptoAssetDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText

/**
 * 3. Service layer handling live network input streams
 */
class CryptoApiService {

    // CoinGecko API base URL
    private val baseUrl = "https://api.coingecko.com/api/v3"

    suspend fun fetchLiveMarketData(): List<CryptoAssetDto> {
        val response = CryptoNetworkClient.httpClient.get("$baseUrl/coins/markets") {
            // Security & Format Headers
            header("x-cg-demo-api-key", CryptoNetworkClient.MY_API_KEY)
            header("Accept", "application/json")

            // Clean API Query Parameters
            parameter("vs_currency", "usd")
            parameter("ids", "bitcoin,ethereum,solana") // Example IDs
            parameter("order", "market_cap_desc")
        }
        return response.body()
    }

}