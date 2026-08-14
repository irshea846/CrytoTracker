package com.rshea.cryptotracker.domain

/**
 * Clean Architecture Repository Interface.
 * Defines the contract for fetching data without revealing the underlying data source (Ktor, DB, etc.).
 */
interface CryptoRepository {
    suspend fun getTrackedCryptoAssets(): UIResourceState<List<CryptoAsset>>
}