package com.rshea.cryptotracker

import com.rshea.cryptotracker.domain.CryptoApi
import com.rshea.cryptotracker.domain.CryptoAssetDto

class FakeCryptoApiService : CryptoApi {
    private var fetchCount = 0

    // Synchronized wrapper to ensure test-state verification tracking is thread-safe
    @Synchronized
    fun getFetchInvocationCount(): Int = fetchCount

    override suspend fun fetchLiveMarketData(): List<CryptoAssetDto> {
        // [DEBUG LOG]: Prints the current execution thread slot
        println("🔍 [FakeAPI] Hitting network card on Thread: ${Thread.currentThread().name}")
        synchronized(this ) {
            fetchCount++
        }

        // Return a basic mock data row payload model matching your DTO schema properties
        return listOf(
            CryptoAssetDto(
                id = "bitcoin",
                symbol = "btc",
                name = "Bitcoin",
                current_price = 62000.50,
                market_cap = 1200000000000.0,
                price_change_percentage_24h = 2.45
            )
        )
    }
}