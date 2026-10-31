package com.rshea.cryptotracker.data

import com.rshea.cryptotracker.database.CryptoAssetDao
import com.rshea.cryptotracker.database.CryptoAssetRoomEntity
import com.rshea.cryptotracker.database.toCryptoAssetList
import com.rshea.cryptotracker.domain.CryptoAsset
import com.rshea.cryptotracker.domain.CryptoRepository
import com.rshea.cryptotracker.domain.UIResourceState
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random // Standard KMP random utility tool
import kotlin.random.nextInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CryptoRepositoryImpl(
    // 1. Core Constructor Parameters remain cleanly structured
    private val cryptoAssetDao: CryptoAssetDao,
    private val apiService: CryptoApiService
) : CryptoRepository {

    private val repositoryWriteMutex = Mutex()

    override fun observeCryptoAssetsStream(): Flow<List<CryptoAsset>> {
        return cryptoAssetDao.getAllCryptoAssetsFlow().map { entityList ->
            entityList.toCryptoAssetList()
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun getTrackedCryptoAssets(): UIResourceState<List<CryptoAsset>> {
        val workerId = Random.nextInt(1000..9999).toString() // Platform-safe random tracking string wrapper
        println("🚀 [Worker $workerId] Arrived at Mutex Gate on Thread: ${currentCoroutineContext()}")

        return repositoryWriteMutex.withLock {
            println("🔒 [Worker $workerId] ENTERED critical section lock successfully!")
            println("🚀 [Repository] Hitting network card...")

            try {
                // 2. Fetch fresh live market payload from the remote Ktor networking service
                val rawDtos = apiService.fetchLiveMarketData()
                println("✅ [Worker $workerId] finished transaction and is releasing lock.")

                // 3. Convert incoming Network DTO packets directly into Room high-precision database rows
                var currentRank = 1
                val roomEntities = rawDtos.map { dto ->
                    CryptoAssetRoomEntity(
                        id = dto.id,
                        rank =  currentRank++,
                        symbol = dto.symbol.uppercase(),
                        name = dto.name,
                        priceUsd = dto.current_price,
                        marketCapUsd = dto.market_cap,
                        changePercent24Hr = dto.price_change_percentage_24h,
                        supply = 0.0, // Default value matching your baseline specification
                        maxSupply = null, // Nullable parameter mapped seamlessly inside your Room configuration
                        lastUpdatedTimestamp = Clock.System.now().toEpochMilliseconds() // Captures synchronization moment natively!
                    )
                }

                // 4. ATOMIC DATABASE UPDATE PASS: Room natively runs bulk inserts using OnConflictStrategy.REPLACE
                cryptoAssetDao.insertCryptoAssets(roomEntities)

                // 5. SINGLE SOURCE OF TRUTH: Read storage data back directly using our local mappers
                // (Note: Since our UI layer already observes observeCryptoAssetsStream(), this instantly forces screen redraws!)
                UIResourceState.Success(roomEntities.toCryptoAssetList())

            } catch (e: Exception) {
                // 6. FALLBACK PROTECTION: If the server is unreachable or network drops a packet,
                // instantly query Room disk snapshots!
                // To fetch a flat, synchronous data lookup pass, we query your DAO context
                println("❌ [Worker $workerId] hit network error: ${e.message}. Attempting Room offline fallback lookup...")

                val cachedRows = cryptoAssetDao.getAllCryptoAssetsFlow().first()
                if (cachedRows.isNotEmpty()) {
                    println("⚠️ [Repository] Network broken, returning ${cachedRows.size} cached Room assets.")
                    UIResourceState.Success(cachedRows.toCryptoAssetList())
                } else {
                    UIResourceState.Error("Network failure and no local persistent cache records located in Room: ${e.message}")
                }
            }
        }
    }
}
