package com.rshea.cryptotracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rshea.cryptotracker.domain.CryptoAsset
import com.rshea.cryptotracker.domain.CryptoRepository
import com.rshea.cryptotracker.domain.UIResourceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CryptoListViewModel(
    private val repository: CryptoRepository
) : ViewModel() {

    // 1. Mutable state backing property confined to the ViewModel's execution context
    private val _screenState = MutableStateFlow<UIResourceState<List<CryptoAsset>>>(UIResourceState.Success(emptyList()))

    // 2. Read-only StateFlow exposed cleanly to your Compose view layer
    val screenState: StateFlow<UIResourceState<List<CryptoAsset>>> = _screenState.asStateFlow()

    fun loadCryptoMarketData() {
        // Transition state to Loading instantly on initialization
        _screenState.value = UIResourceState.Loading

        // Launch coroutine safely bound directly to the lifecycle of this ViewModel scope
        viewModelScope.launch {
            val result = repository.getTrackedCryptoAssets()
            _screenState.value = result
        }
    }

}