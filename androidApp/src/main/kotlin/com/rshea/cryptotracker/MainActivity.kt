package com.rshea.cryptotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rshea.cryptotracker.data.CryptoApiService
import com.rshea.cryptotracker.data.CryptoRepositoryImpl
import com.rshea.cryptotracker.database.DatabaseModule
import com.rshea.cryptotracker.presentation.App
import com.rshea.cryptotracker.presentation.CryptoListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val apiService = CryptoApiService()

            // 1. Instantiate your manual DatabaseModule, passing the Android Context parameter
            val databaseModule = DatabaseModule(applicationContext)

            // 2. Extract your fully initialized Room Data Access Object from the factory container
            val cryptoAssetDao = databaseModule.provideCryptoAssetDao()

            // 3. FIX: Instantiate CryptoRepositoryImpl passing strictly the single required Room DAO instance
            val repository = CryptoRepositoryImpl(cryptoAssetDao, apiService)

            // 2. Use Android's standard retained viewmodel factory block
            // This ensures your ViewModel survives native screen rotations!
            val cryptoViewModel: CryptoListViewModel = viewModel {
                CryptoListViewModel(repository = repository)
            }

            // 3. FIXED: Inject the initialized ViewModel straight into your shared UI entry point!
            App(viewModel = cryptoViewModel)
        }
    }
}

