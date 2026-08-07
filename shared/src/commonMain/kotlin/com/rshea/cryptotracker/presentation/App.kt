package com.rshea.cryptotracker.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rshea.cryptotracker.data.CryptoApiService
import kotlinx.coroutines.launch
@Composable
fun App(
    // 1. Dependency Inversion: Pass the service into the screen function as a parameter boundary
    apiService: CryptoApiService = remember { CryptoApiService() }
) {
    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()
        var rawJsonResult by remember { mutableStateOf("Click button to load live data...") }
        var isLoading by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxSize().padding(40.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Structural Header Title Layout Element
            Text(
                text = "Crypto Tracker 2026",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // 2. Action Event Button Hook
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 56.dp),
                enabled = !isLoading
            ) {
                Text("Trigger API Pull Event")
            }

            // 3. Clear Empty Physical Layout Spacer (Replaces old XML Margin attributes)
            Spacer(modifier = Modifier.height(24.dp))

            // 4. Dynamic Screen Content-State Branching Execution Pipeline
            if (isLoading) {
                // Centered Loading Spinner Container View (XML FrameLayout Equivalent)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                // Themed Result Data Card Container Block
                Card(modifier = Modifier.fillMaxWidth().weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        item {
                            Text(
                                text = rawJsonResult,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
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