package com.rshea.cryptotracker.domain

import androidx.compose.runtime.Immutable

/**
 * Enterprise-grade Sealed Interface pattern representing a single source of truth
 * for UI states, making illegal screen combinations compile-time impossible.
 */
@Immutable
sealed interface UIResourceState<out T> {
    object Loading : UIResourceState<Nothing>
    data class Success<out T>(
        val data: T,
        val isStale: Boolean = false
    ) : UIResourceState<T>
    data class Error(val message: String) : UIResourceState<Nothing>
}