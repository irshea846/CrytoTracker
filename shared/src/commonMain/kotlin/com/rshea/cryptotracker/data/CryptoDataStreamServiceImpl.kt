package com.rshea.cryptotracker.data

import com.rshea.cryptotracker.domain.CryptoDataStreamService
import com.rshea.cryptotracker.domain.DataPoint
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CryptoDataStreamServiceImpl(
    private val httpClient: HttpClient
) : CryptoDataStreamService {

    // 1. Thread-safe event flow to stream parsed updates downstream securely
    private val _priceUpdates = MutableSharedFlow<DataPoint>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var activeSession: DefaultClientWebSocketSession? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override suspend fun openStreamChannel() {
        if (activeSession != null) return // Already connected, shield from double activation

        serviceScope.launch {
            try {
                // 2. Establish a permanent, persistent secure WebSocket transport tunnel to the gateway server
                val session = httpClient.webSocketSession(
                    method = io.ktor.http.HttpMethod.Get,
                    host = "api.cryptotracker.io", // Target production gateway address placeholder
                    path = "/v1/market-data/stream"
                )
                activeSession = session

                // 3. Persistent reader loop: suspends until a new string frame arrives from the network
                for (frame in session.incoming) {
                    if (frame is Frame.Text) {
                        val rawTextString = frame.readText()

                        // TEMPORARY TRACE LOG: Verifies network frames hit the data layer cleanly
                        println("📡 [WebSocketStream] Received raw text frame: $rawTextString")

                        // NOTE: Tomorrow (Day 43), we will pass this string to our unboxed binary/JSON math parser!
                        // For tonight, we emit a mock coordinate entry to prove the lifecycle tunnel is open.
                        _priceUpdates.emit(DataPoint(x = 0f, y = 0f, "Raw Ticker Connected"))
                    }
                }
            } catch (e: Exception) {
                println("⚠️ [WebSocketStream] Channel execution encountered an exception: ${e.message}")
            } finally {
                activeSession?.close()
                activeSession = null
            }
        }
    }

    override suspend fun closeStreamChannel() {
        activeSession?.close(CloseReason(CloseReason.Codes.NORMAL, "Client triggered graceful disconnect"))
        activeSession = null
    }

    override fun observePriceUpdates(): Flow<DataPoint> = _priceUpdates.asSharedFlow()
}