package com.rshea.cryptotracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

import cryptotracker.shared.generated.resources.Res
import cryptotracker.shared.generated.resources.compose_multiplatform
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        val apiService = remember { CryptoApiService() }
        val coroutineScope = rememberCoroutineScope()

        var rawJsonResult by remember { mutableStateOf("Click button to load live data...") }
        var isLoading by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxSize().padding(40.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    isLoading = true
                    coroutineScope.launch {
                        try {
                            // Asynchronously fetch raw data on a safe background thread context
                            rawJsonResult = apiService.fetchLiveMarketData()
                        } catch (e: Exception) {
                            rawJsonResult = "Network Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text(if (isLoading) "Fetching market statistics..." else "Pull Live Crypto Prices")
            }

            // Simple log display container replacing old TextView structures
            Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    item {
                        Text(text = rawJsonResult, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}








//@Preview
//fun App() {
//    // 1. Theme Configuration
//    MaterialTheme {
//        // 2. Mutable State - The absolute core concept of Compose
//        // When this variable changes, Compose automatically re-draws the screen
//        var isTextVisible by remember { mutableStateOf(false) }
//
//        // 3. Layout Structure (Replaces LinearLayout/ConstraintLayout)
//        Column(
//            modifier = Modifier.fillMaxSize().padding(16.dp),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Button(onClick = { isTextVisible = !isTextVisible }) {
//                Text(if (isTextVisible) "Hide Greeting" else "Show Greeting")
//            }
//
//            // 4. Reactive Animated UI
//            AnimatedVisibility(visible = isTextVisible) {
//                Text(
//                    text = "Hello from Compose Multiplatform!",
//                    style = MaterialTheme.typography.headlineMedium,
//                    modifier = Modifier.padding(top = 16.dp)
//                )
//            }
//        }
//    }
//}
//fun App() {
//    MaterialTheme {
//        var showContent by remember { mutableStateOf(false) }
//        Column(
//            modifier = Modifier
//                .background(MaterialTheme.colorScheme.primaryContainer)
//                .safeContentPadding()
//                .fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//        ) {
//            Button(onClick = { showContent = !showContent }) {
//                Text("Click me!")
//            }
//            AnimatedVisibility(showContent) {
//                val greeting = remember { Greeting().greet() }
//                Column(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                ) {
//                    Image(painterResource(Res.drawable.compose_multiplatform), null)
//                    Text("Compose: $greeting")
//                }
//            }
//        }
//    }
//}