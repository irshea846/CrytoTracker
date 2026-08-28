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

}