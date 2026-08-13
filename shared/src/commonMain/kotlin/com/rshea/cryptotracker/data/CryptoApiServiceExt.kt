package com.rshea.cryptotracker.data

import com.rshea.cryptotracker.domain.UIResourceState
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.io.IOException

/**
 * Executes a network request block safely, mapping raw cellular hardware exceptions
 * directly into our type-safe UIResourceState sealed container.
 */
suspend fun <T> safeNetworkCall(block: suspend () -> T): UIResourceState<T> {
    return try {
        // Execute the network request operation block
        UIResourceState.Success(block())
    } catch (e: ConnectTimeoutException) {
        UIResourceState.Error("Connection Timeout: Please check your cellular connection network settings.")
    } catch (e: SocketTimeoutException) {
        UIResourceState.Error("Server Timeout: The network server is taking too long to respond.")
    } catch (e: IOException) {
        UIResourceState.Error("No Internet: Device is offline or cellular signal was dropped.")
    } catch (e: Exception) {
        UIResourceState.Error("Unknown Execution Error: ${e.message ?: "An unhandled exception occurred."}")
    }
}