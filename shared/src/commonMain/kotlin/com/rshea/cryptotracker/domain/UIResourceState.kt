package com.rshea.cryptotracker.domain

/**
 * Enterprise-grade Sealed Interface pattern representing a single source of truth
 * for UI states, making illegal screen combinations compile-time impossible.
 */
sealed interface UIResourceState<out T> {
    object Loading : UIResourceState<Nothing>
    data class Success<T>(val data: T) : UIResourceState<T>
    data class Error(val message: String) : UIResourceState<Nothing>
}