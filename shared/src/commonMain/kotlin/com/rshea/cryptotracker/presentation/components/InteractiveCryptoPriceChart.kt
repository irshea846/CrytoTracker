package com.rshea.cryptotracker.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.rshea.cryptotracker.domain.ChartMathTransformer
import com.rshea.cryptotracker.domain.ChartViewportTransformer
import com.rshea.cryptotracker.domain.DataPoint
import kotlin.math.abs

@Composable
fun InteractiveCryptoPriceChart(
    dataPoints: List<DataPoint>, // Accepts the flat list directly
    modifier: Modifier = Modifier
) {
    // 1. Calculate structural boundaries and cache the context
    val chartBounds = remember(dataPoints) {
        ChartViewportTransformer.calculateBounds(dataPoints)
    }

    // 2. Structural tracking states for interactive brush metrics
    var selectedIndex by remember { mutableStateOf(-1) }
    var isDragging by remember { mutableStateOf(false) }

    // Maintain a mutable list state to pass coordinates dynamically from DrawScope to gesture layer
    val pixelOffsets = remember { mutableListOf<Offset>() }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(pixelOffsets) {
                    // Intercept and track continuous slide gestures across the canvas
                    detectDragGesturesAfterLongPress(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            selectedIndex = -1
                        },
                        onDragCancel = {
                            isDragging = false
                            selectedIndex = -1
                        },
                        onDrag = { change, _ ->
                            val touchX = change.position.x

                            // Scan optimization loop to find the nearest point index
                            if (pixelOffsets.isNotEmpty()) {
                                var closestIndex = 0
                                var minDelta = Float.MAX_VALUE

                                for (i in pixelOffsets.indices) {
                                    val delta = abs(pixelOffsets[i].x - touchX)
                                    if (delta < minDelta) {
                                        minDelta = delta
                                        closestIndex = i
                                    }
                                }
                                selectedIndex = closestIndex
                            }
                        }
                    )
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            if (dataPoints.isNotEmpty() && canvasWidth > 0f && canvasHeight > 0f) {

                // 3. Compute your unboxed pixel vectors dynamically inside DrawScope
                val offsets = ChartMathTransformer.transformToScreenPixels(
                    points = dataPoints,
                    bounds = chartBounds,
                    componentWidth = canvasWidth,
                    componentHeight = canvasHeight
                )

                // Cache coordinates back into the parent-remembered collection for gesture lookups
                if (pixelOffsets.size != offsets.size) {
                    pixelOffsets.clear()
                    pixelOffsets.addAll(offsets)
                }

                // 4. DAY 38 REMNANT: Construct the vector geometry path natively
                val vectorPath = Path().apply {
                    val firstOffset = offsets.first()
                    moveTo(firstOffset.x, firstOffset.y)

                    for (i in 1 until offsets.size) {
                        lineTo(offsets[i].x, offsets[i].y)
                    }
                }

                // Render the primary neon green asset trend line
                drawPath(
                    path = vectorPath,
                    color = Color(0xFF4CAF50),
                    style = Stroke(width = 3.dp.toPx())
                )

                // 5. DAY 39 INTERACTIVE BRUSH: Render tracking indicator overlay
                if (isDragging && selectedIndex in offsets.indices) {
                    val highlightedOffset = offsets[selectedIndex]

                    // Draw vertical tracking guide rule
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.6f),
                        start = Offset(x = highlightedOffset.x, y = 0f),
                        end = Offset(x = highlightedOffset.x, y = canvasHeight),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Draw localized center anchor circular node marker
                    drawCircle(
                        color = Color(0xFF4CAF50),
                        radius = 6.dp.toPx(),
                        center = highlightedOffset
                    )
                }
            }
        }
    }
}
