package com.rshea.cryptotracker.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.rshea.cryptotracker.database.CryptoDatabase
import com.rshea.cryptotracker.domain.CryptoApi
import com.rshea.cryptotracker.domain.CryptoAsset
import com.rshea.cryptotracker.domain.CryptoRepository
import com.rshea.cryptotracker.domain.UIResourceState
import com.rshea.cryptotracker.util.randomUUID
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext

/**
 * Concrete implementation of the repository contract.
 * Coordinates the API service and safety exception mapping wrapper.
 */
class CryptoRepositoryImpl(
    sqlDriver: SqlDriver,
    private val apiService: CryptoApi
) : CryptoRepository {

    // 1. Update your domain repository contract interface to declare this streaming channel:
    // fun observeCryptoAssetsStream(): Flow<List<CryptoAsset>>

    /**
     * Turns your local SQLite storage layer into a live Reactive Stream.
     * Whenever any transaction writes or deletes rows inside the 'cryptoAssetEntity' table,
     * this Flow instantly re-queries the table and emits the updated list to observers.
     */

    private val database = CryptoDatabase(sqlDriver)
    private val queries = database.cryptoDatabaseQueries

    // Line 1: Injects a private, cross-platform asynchronous Mutex instance
    private val repositoryWriteMutex = Mutex()

    override fun observeCryptoAssetsStream(): Flow<List<CryptoAsset>> {
        return queries.getAllCryptoAssets()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { cachedEntities ->
                cachedEntities.toDomainModelListFromEntities()
            }
    }

    override suspend fun getTrackedCryptoAssets(): UIResourceState<List<CryptoAsset>> {
        val workerId = randomUUID().take(4)
        println("🚀 [Worker $workerId] Arrived at Mutex Gate on Thread: ${currentCoroutineContext()}")

        // Line 2: Creates a strict concurrency perimeter around data transactions
        return repositoryWriteMutex.withLock {
            println("🔒 [Worker $workerId] ENTERED critical section lock successfully!")
            // 1. COLLAPSING LOGIC: Check if data was already fetched by another worker while waiting for the lock
            val cachedBefore = queries.getAllCryptoAssets().executeAsList()
            if (cachedBefore.isNotEmpty()) {
                println("✨ [Repository] Found cached data! Skipping network fetch.")
                return@withLock UIResourceState.Success(cachedBefore.toDomainModelListFromEntities())
            }

            println("🚀 [Repository] Hitting network card...")
            try {
                // 2. Fetch fresh payload from the remote Ktor networking service
                val rawDtos = apiService.fetchLiveMarketData()
                println("✅ [Worker $workerId] finished transaction and is releasing lock.")

                // 3. Clear stale records and write new metrics into local SQLite via an atomic transaction
                queries.transactionWithResult {
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

                    // 4. SINGLE SOURCE OF TRUTH: Read records back directly from the local database
                    val cachedEntities = queries.getAllCryptoAssets().executeAsList()
                    UIResourceState.Success(cachedEntities.toDomainModelListFromEntities())
                }
            } catch (e: Exception) {
                // 5. LOCAL OFFLINE FALLBACK: If network is broken, instantly load the last-saved local DB snapshots!
                val localEntities = queries.getAllCryptoAssets().executeAsList()
                if (localEntities.isNotEmpty()) {
                    UIResourceState.Success(localEntities.toDomainModelListFromEntities())
                } else {
                    println("❌ [Worker $workerId] hit error: ${e.message}")
                    UIResourceState.Error("Network failure and no local persistent cache records located: ${e.message}")
                }
            }
        }
    }
}
