package com.rshea.cryptotracker.domain

// 1. Represents a localized chronological price entry point matching your
// live Ktor DTO responses
data class DataPoint(
    val x: Float,   // Will represent normalized time coordinates
    val y: Float,   // Will represent normalized price coordinates
    val textLabel: String   // Parsed data timestamp string for interactive crosshair brushes
)

// 2. Encapsulates bounds definitions to isolate layout canvas scaling calculations
data class ChartBounds(
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float
)
