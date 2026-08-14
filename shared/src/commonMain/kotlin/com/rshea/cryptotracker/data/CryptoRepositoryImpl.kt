package com.rshea.cryptotracker.data

import com.rshea.cryptotracker.domain.CryptoAsset
import com.rshea.cryptotracker.domain.CryptoRepository
import com.rshea.cryptotracker.domain.UIResourceState

/**
 * Concrete implementation of the repository contract.
 * Coordinates the API service and safety exception mapping wrapper.
 */
class CryptoRepositoryImpl(
    private val apiService: CryptoApiService = CryptoApiService()
) : CryptoRepository {
    override suspend fun getTrackedCryptoAssets(): UIResourceState<List<CryptoAsset>> {
        // Wrap the network execution loop inside our safe engine helper
        return safeNetworkCall {
            // 1. fetch raw remote network DTO data list
            val rawDtos = apiService.fetchLiveMarketData()

            // 2. map the data upward into clean, formatted domain model
            rawDtos.toDomainModelList()
        }
    }
}