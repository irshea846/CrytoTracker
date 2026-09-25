package com.rshea.cryptotracker.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.rshea.cryptotracker.domain.ChartMathTransformer
import com.rshea.cryptotracker.domain.ChartViewportTransformer
import com.rshea.cryptotracker.domain.DataPoint

/**
 * A highly optimized, hardware-accelerated crypto price trend chart component.
 *
 * @param dataPoints Raw historical price snapshots fetched from our data layer.
 * @param modifier Custom structural layout constraints.
 */
@Composable
fun CryptoPriceChart(
    dataPoints: List<DataPoint>,
    modifier: Modifier = Modifier
) {
    // 1. Calculate structural boundaries and cache the configuration context
    val chartBounds = remember(dataPoints) {
        ChartViewportTransformer.calculateBounds(dataPoints)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        // 2. Access the canvas dimensions inside the DrawScope frame environment (Pixels)
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (dataPoints.isNotEmpty() && canvasWidth > 0f && canvasHeight > 0f) {

            // 3. Invoke our pure domain math transformer to acquire unboxed pixel vectors
            val pixelOffsets = ChartMathTransformer.transformToScreenPixels(
                points = dataPoints,
                bounds = chartBounds,
                componentWidth = canvasWidth,
                componentHeight = canvasHeight
            )

            // 4. Construct the vector geometry path natively on the stack loop
            val vectorPath = Path().apply {
                // Invariant Check: Teleport the drawing brush anchor to the first position
                val firstOffset = pixelOffsets.first()
                moveTo(firstOffset.x, firstOffset.y)

                // Loop over the remaining offsets and trace the linear connections sequentially
                for (i in 1 until pixelOffsets.size) {
                    val nextOffset = pixelOffsets[i]
                    lineTo(nextOffset.x, nextOffset.y)
                }
            }

            // 5. Commit the buffered path arrays to the GPU layout layer
            drawPath(
                path = vectorPath,
                color = Color(0xFF4CAF50), // Standard Neon Crypto Green
                style = Stroke(
                    width = 3.dp.toPx(), // Converts layout density dp sizes safely to raw pixels
                    miter = Stroke.DefaultMiter
                )
            )
        }
    }
}