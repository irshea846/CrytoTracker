package com.rshea.cryptotracker.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Hardware-accelerated, zero-dependency Compose Multiplatform Sparkline Chart.
 * Renders chronological price points with a dynamic trend stroke and subtle area gradient.
 */
@Composable
fun AssetSparklineChart(
    pricePoints: List<Double>,
    isPositiveTrend: Boolean,
    modifier: Modifier = Modifier
) {
    // Guard: A line requires at least two chronological price ticks
    if (pricePoints.size < 2) return

    val trendColor = if (isPositiveTrend) {
        Color(0xFF16A34A) // Crisp Emerald Green
    } else {
        Color(0xFFDC2626) // Crisp Crimson Red
    }

    // Pre-allocate Path objects across recompositions to avoid Canvas frame GC churn
    val strokePath = remember { Path() }
    val fillPath = remember { Path() }

    // Inside AssetSparklineChart.kt right above `if (pricePoints.size < 2) return`
    println("📈 [Sparkline] Points count: ${pricePoints.size} -> $pricePoints")

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val minPrice = pricePoints.min()
        val maxPrice = pricePoints.max()
        val priceRange = maxPrice - minPrice

        // Add 10% vertical padding so peak/trough strokes never clip at canvas edges
        val verticalPadding = height * 0.1f
        val drawableHeight = height - (verticalPadding * 2f)
        val stepX = width / (pricePoints.size - 1).toFloat()

        strokePath.reset()
        fillPath.reset()

        pricePoints.forEachIndexed { index, price ->
            val x = index * stepX

            // Normalize Y coordinate (Canvas Y=0 is top, so invert ratio)
            val normalizedRatio = if (priceRange == 0.0) {
                0.5f // Flat horizontal center line if all cached ticks have identical price
            } else {
                ((price - minPrice) / priceRange).toFloat()
            }
            val y = verticalPadding + drawableHeight * (1f - normalizedRatio)

            if (index == 0) {
                strokePath.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                strokePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == pricePoints.lastIndex) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }
        }

        // 1. Draw translucent area gradient beneath the trend line
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    trendColor.copy(alpha = 0.25f),
                    trendColor.copy(alpha = 0.0f)
                )
            )
        )

        // 2. Draw crisp anti-aliased trend stroke
        drawPath(
            path = strokePath,
            color = trendColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
