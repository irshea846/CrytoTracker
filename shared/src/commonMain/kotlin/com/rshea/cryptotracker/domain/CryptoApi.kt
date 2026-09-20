package com.rshea.cryptotracker.domain

interface CryptoApi {
    suspend fun fetchLiveMarketData(): List<CryptoAssetDto>
}