package com.rshea.cryptotracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rshea.cryptotracker.domain.CryptoAsset
import com.rshea.cryptotracker.domain.CryptoRepository
import com.rshea.cryptotracker.domain.UIResourceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CryptoListViewModel(
    private val repository: CryptoRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isStale = MutableStateFlow(false)

    val screenState: StateFlow<UIResourceState<List<CryptoAsset>>> = combine(
        repository.observeCryptoAssetsStream(),
        _isStale
    ) { cryptoList, isStale ->
        if (cryptoList.isEmpty()) {
            UIResourceState.Loading
        } else {
            UIResourceState.Success(data = cryptoList, isStale = isStale)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UIResourceState.Loading
    )

    fun loadCryptoMarketData() {
        viewModelScope.launch {
            val result = repository.getTrackedCryptoAssets(forceRefresh = false)
            if (result is UIResourceState.Success) {
                _isStale.value = result.isStale
            }
        }
    }

    fun refreshMarketData() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val result = repository.getTrackedCryptoAssets(forceRefresh = true)
                if (result is UIResourceState.Success) {
                    _isStale.value = result.isStale
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    suspend fun observeLiveMarketTicker() {
        repository.startMarketTickerFlow().collect { state ->
            if (state is UIResourceState.Success) {
                _isStale.value = state.isStale
            }
        }
    }
}
