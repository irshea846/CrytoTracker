package com.rshea.cryptotracker.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.rshea.cryptotracker.database.CryptoDatabase
import com.rshea.cryptotracker.database.DatabaseDriverFactory
import com.rshea.cryptotracker.domain.CryptoAsset
import com.rshea.cryptotracker.domain.CryptoRepository
import com.rshea.cryptotracker.domain.UIResourceState

/**
 * Concrete implementation of the repository contract.
 * Coordinates the API service and safety exception mapping wrapper.
 */
class CryptoRepositoryImpl(
    driverFactory: DatabaseDriverFactory,
    private val apiService: CryptoApiService = CryptoApiService()
) : CryptoRepository {

    // 1. Update your domain repository contract interface to declare this streaming channel:
    // fun observeCryptoAssetsStream(): Flow<List<CryptoAsset>>

    /**
     * Turns your local SQLite storage layer into a live Reactive Stream.
     * Whenever any transaction writes or deletes rows inside the 'cryptoAssetEntity' table,
     * this Flow instantly re-queries the table and emits the updated list to observers.
     */

    private val database = CryptoDatabase(driverFactory.createDriver())
    private val queries = database.cryptoDatabaseQueries

    override fun observeCryptoAssetsStream(): Flow<List<CryptoAsset>> {
        return queries.getAllCryptoAssets()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { cachedEntities ->
                cachedEntities.map { entity ->
                    CryptoAsset(
                        id = entity.id,
                        symbol = entity.symbol,
                        name = entity.name,
                        priceUsd = "$${entity.priceUsd}",
                        marketCapUsd = entity.marketCapUsd.toString(),
                        priceChange24hText = "${entity.priceChange24hText}%",
                        isPricePositive = entity.isPricePositive == 1L
                   )
                }
            }
    }

    override suspend fun getTrackedCryptoAssets(): UIResourceState<List<CryptoAsset>> {
        return try {
            // 1. Fetch fresh payload from the remote Ktor networking service
            val rawDtos = apiService.fetchLiveMarketData()

            // 2. Clear stale records and write new metrics into local SQLite via an atomic transaction
            queries.transactionWithResult {
                queries.getAllCryptoAssets()
                rawDtos.forEach { dto ->
                    queries.insertCryptoAsset(
                        id = dto.id,
                        symbol = dto.symbol.uppercase(),
                        name = dto.name,
                        priceUsd = dto.current_price,
                        marketCapUsd = dto.market_cap,
                        changePercent24Hr = dto.price_change_percentage_24h,
                        supply = 0.0, // Default value as not in DTO
                        volumeUsd24Hr = 0.0, // Default value as not in DTO
                        priceChange24hText = "${dto.price_change_percentage_24h}%",
                        isPricePositive = if (dto.price_change_percentage_24h >= 0) 1L else 0L // SQLite Maps Booleans as Long 1/0
                    )
                }

                // 3. SINGLE SOURCE OF TRUTH: Read records back directly from the local database
                val cachedEntities = queries.getAllCryptoAssets().executeAsList()

                // 4. Map DB entities out cleanly into your domain model structures
                val domainAssets = cachedEntities.map { entity ->
                    CryptoAsset(
                        id = entity.id,
                        symbol = entity.symbol,
                        name = entity.name,
                        priceUsd = "$${entity.priceUsd}", // Your custom rounding extension formats apply smoothly here
                        marketCapUsd = entity.marketCapUsd.toString(),
                        priceChange24hText = entity.priceChange24hText,
                        isPricePositive = entity.isPricePositive == 1L
                    )
                }
                UIResourceState.Success(domainAssets)
            }
        } catch (e: Exception) {
            // 5. LOCAL OFFLINE FALLBACK: If network is broken, instantly load the last-saved local DB snapshots!
            val localEntities = queries.getAllCryptoAssets().executeAsList()
            if (localEntities.isNotEmpty()) {
                val domainAssets = localEntities.map { entity ->
                    CryptoAsset(
                        id = entity.id,
                        symbol = entity.symbol,
                        name = entity.name,
                        priceUsd = "$${entity.priceUsd}",
                        marketCapUsd = entity.marketCapUsd.toString(),
                        priceChange24hText = entity.priceChange24hText,
                        isPricePositive = entity.isPricePositive == 1L
                    )
                }
                UIResourceState.Success(domainAssets)
            } else {
                UIResourceState.Error("Network failure and no local persistent cache records located.")
            }
        }
    }
}
