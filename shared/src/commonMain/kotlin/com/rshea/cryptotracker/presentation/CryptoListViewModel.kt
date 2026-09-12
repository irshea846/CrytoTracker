package com.rshea.cryptotracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rshea.cryptotracker.domain.CryptoAsset
import com.rshea.cryptotracker.domain.CryptoRepository
import com.rshea.cryptotracker.domain.UIResourceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CryptoListViewModel(
    // LINE BREAKDOWN 1: We pass the abstract domain contract interface via the constructor.
    // This allows us to inject fake/mock repositories easily during automated unit testing.
    private val repository: CryptoRepository
) : ViewModel() {

    // Mutable state backing property confined to the ViewModel's execution context
    private val _screenState = MutableStateFlow<UIResourceState<List<CryptoAsset>>>(UIResourceState.Loading)

    // Read-only StateFlow exposed cleanly to your Compose view layer
    // FIXED: The entire reactive pipeline is now defined as a single declarative property!
    val screenState: StateFlow<UIResourceState<List<CryptoAsset>>> = repository
        .observeCryptoAssetsStream()  // 1. Read the cold database Flow stream channel
        .map { cryptoList -> // 2. Catch emissions and map them to our type-safe State wrapper
            if (cryptoList.isEmpty()) UIResourceState.Loading else UIResourceState.Success(cryptoList)
        }
        .stateIn( // 3. Transform the cold Flow pipeline into a hot, read-only StateFlow!
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UIResourceState.Loading
        )


    // LINE BREAKDOWN 2: The Init Block executes immediately when the ViewModel is created.
//    init {
//        observeDatabasePipeline()
//    }
//
//    private fun observeDatabasePipeline() {
//        // LINE BREAKDOWN 3: viewModelScope binds this asynchronous coroutine listener strictly
//        // to the lifecycle of this ViewModel. If the user leaves the screen, this listener completes
//        // automatically, completely preventing memory leaks!
//        viewModelScope.launch {
//            // LINE BREAKDOWN 4: repository.observeCryptoAssetsStream() returns a cold Kotlin Flow channel.
//            // Calling .collect { ... } turns the faucet on. The loop suspends and waits.
//            repository.observeCryptoAssetsStream().collect { assets ->
//                // LINE BREAKDOWN 5: The millisecond the local database emits a new list, this block catches it.
//                // We wrap the list inside our type-safe Success wrapper and push it directly down the StateFlow.
//                _screenState.value = UIResourceState.Success(assets)
//            }
//        }
//    }

    fun loadCryptoMarketData() {
        viewModelScope.launch {
            // LINE BREAKDOWN 6: This button action no longer returns data directly to the ViewModel.
            // It simply fires a background network request. The repository will catch the payload,
            // write it to the database, and our observeDatabasePipeline() above handles the rest!
            repository.getTrackedCryptoAssets()
        }
    }

}