package com.rshea.cryptotracker.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rshea.cryptotracker.data.CryptoRepositoryImpl
import com.rshea.cryptotracker.domain.CryptoAsset
import com.rshea.cryptotracker.domain.CryptoRepository
import com.rshea.cryptotracker.domain.UIResourceState
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

@Composable
fun App(
    // 1. Dependency Inversion: Pass the service into the screen function as a parameter boundary
    cryptoRepository: CryptoRepository = remember { CryptoRepositoryImpl() }
) {
    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()

        // 2. Keep your clean domain model list for rendering
        var cryptoList by remember { mutableStateOf<List<CryptoAsset>>(emptyList()) }

        // 3. NEW: Add a state variable to track the raw wrapper status
        var screenState by remember { mutableStateOf<UIResourceState<List<CryptoAsset>>>(
            UIResourceState.Success(emptyList())) }

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
                    // Set state to Loading immediately when clicked
                    screenState = UIResourceState.Loading

                    coroutineScope.launch {
                        //Why This Fix Wins Senior Interviews
                        // If an interviewer asks you why you copied a repository response into an
                        // immutable local val inside an asynchronous scope, deliver this exact
                        // response:"In multithreaded or asynchronous Kotlin scopes, checking mutable
                        // properties directly breaks compiler smart casting because thread states
                        // can drift between verification and execution. By copying the network
                        // response into an immutable local reference (val), we guarantee thread
                        // isolation. The Kotlin compiler can then safely smart cast the object
                        // structure without forcing risky explicit typecasts (as), keeping our
                        // application safe from unexpected runtime exceptions."

                        // 1. Capture the response in an immutable local reference (val)
                        val repositoryResponse = cryptoRepository.getTrackedCryptoAssets()

                        // 2. Assign the response to your global mutable screen state tracker
                        screenState = repositoryResponse

                        // Unpack the list content ONLY if the state returns a Success signature
                        if (repositoryResponse is UIResourceState.Success) {
                            cryptoList = repositoryResponse.data
                        }

                        // The following portion of code is an antipattern.
                        // Your IDE suggested that code because it is fighting Kotlin's Smart Casting
                        // rules inside your coroutine scope!The compiler is warning you because
                        // result is a variable whose state can theoretically be modified across
                        // different background execution frames. When you perform a smart
                        // cast check (if (result is UIResourceState.Success)), the compiler cannot
                        // guarantee that result won't be modified by another asynchronous thread
                        // before your code reads .data.While your IDE's suggestion (as
                        // UIResourceState.Success) clears the error, forcing an explicit cast (as)
                        // is an antipattern that can cause unexpected app crashes if the state
                        // of result ever shifts unexpectedly.Here is the clean,
                        // production-grade way to fix this type-safe smart casting issue inside
                        // your coroutine scope.

                        // Because screenState is defined at the top of your function as a mutable
                        // state tracker (var screenState by remember ...), Kotlin blocks smart
                        // casting on it inside asynchronous blocks because its value can
                        // theoretically change mid-execution. That is why your IDE stepped in and
                        // tried to force an explicit cast (as UIResourceState.Success)
                        if (false) {
                            // Fetch data from the repository container contract
                            val result = cryptoRepository.getTrackedCryptoAssets()

                            // Assign the raw result to your screen state tracker
                            screenState = result

                            // Unpack the list content ONLY if the state returns a Success signature
                            if (screenState is UIResourceState.Success) {
                                cryptoList = (screenState as UIResourceState.Success<List<CryptoAsset>>).data
                            }
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

            // 4. CLEAN COMPRESSED STATE DRAW: Replaces your old messy nested if-else structures
            when (val state = screenState) {
                is UIResourceState.Loading -> {
                    // Centered Loading Spinner Container View (XML FrameLayout Equivalent)
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is UIResourceState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }

                is UIResourceState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        LazyColumn(modifier = Modifier.padding(16.dp)) {
                            // This loops through your list and binds each coin to its own row layout
                            items(cryptoList) { crypto ->
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                    Text(
                                        text = "${crypto.name} (${crypto.symbol})",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Price: ${crypto.priceUsd} | Cap: ${crypto.marketCapUsd}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = crypto.priceChange24hText,
                                        style = MaterialTheme.typography.bodySmall,
                                        // Dynamically styles text color based on business logic flags!
                                        color = if (crypto.isPricePositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(top = 8.dp),
                                        thickness = DividerDefaults.Thickness,
                                        color = DividerDefaults.color
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}