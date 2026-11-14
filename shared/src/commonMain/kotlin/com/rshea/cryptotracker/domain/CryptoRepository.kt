package com.rshea.cryptotracker.domain

import kotlinx.coroutines.flow.Flow

/**
 * Clean Architecture Repository Interface.
 * Defines the contract for fetching data without revealing the underlying data source (Ktor, DB, etc.).
 */
interface CryptoRepository {
    suspend fun getTrackedCryptoAssets(): UIResourceState<List<CryptoAsset>>

    // ADD THIS LINE: Exposes the live data stream channel uniformly to observers!
    fun observeCryptoAssetsStream(): Flow<List<CryptoAsset>>

    // (Setting 65_000L ms—just above your 60_000L CACHE_TTL_MILLIS—ensures each scheduled tick
    // cleanly clears your TTL guard and records a fresh historical point in Room!)
    fun startMarketTickerFlow(intervalMillis: Long = 65_000L): Flow<UIResourceState<List<CryptoAsset>>>

}