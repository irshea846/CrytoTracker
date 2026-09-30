package com.rshea.cryptotracker.domain

import kotlinx.coroutines.flow.Flow

/**
 * The core domain infrastructure contract for real-time market data streaming.
 * This lives in the domain layer and has zero knowledge of Ktor or WebSockets.
 */
interface CryptoDataStreamService {

    /**
     * Establishes a long-running, persistent network stream connection.
     * Suspends until the connection is opened or fails.
     */
    suspend fun openStreamChannel()

    /**
     * Closes the active streaming channel and releases underlying radio resources cleanly.
     */
    suspend fun closeStreamChannel()

    /**
     * A cold asynchronous stream stream that emits live price ticker updates in real-time.
     * Higher presentation layers collect this Flow to update charts smoothly.
     */
    fun observePriceUpdates(): Flow<DataPoint>

}