package com.rshea.cryptotracker.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rshea.cryptotracker.domain.ChartMathTransformer
import com.rshea.cryptotracker.domain.ChartViewportTransformer
import com.rshea.cryptotracker.domain.DataPoint
import kotlin.math.abs

@Composable
fun InteractiveCryptoPriceChart(
    dataPoints: List<DataPoint>,
    modifier: Modifier = Modifier
) {
    val chartBounds = remember(dataPoints) {
        ChartViewportTransformer.calculateBounds(dataPoints)
    }

    var selectedIndex by remember { mutableStateOf(-1) }
    val pixelOffsets = remember { mutableStateListOf<Offset>() }

    fun updateSelectionFromTouchX(touchX: Float) {
        if (pixelOffsets.isEmpty()) return
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

    Column(modifier = modifier) {
        // Floating Price Readout Header (always reserves fixed height to avoid layout jumps)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (selectedIndex in dataPoints.indices) {
                val activePoint = dataPoints[selectedIndex]
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Tick #${selectedIndex + 1}: ${activePoint.textLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            } else {
                Text(
                    text = "Tap or slide across chart to inspect prices",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(dataPoints) {
                    detectTapGestures(
                        onPress = { offset ->
                            updateSelectionFromTouchX(offset.x)
                            tryAwaitRelease()
                            selectedIndex = -1
                        }
                    )
                }
                .pointerInput(dataPoints) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> updateSelectionFromTouchX(offset.x) },
                        onDragEnd = { selectedIndex = -1 },
                        onDragCancel = { selectedIndex = -1 },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            updateSelectionFromTouchX(change.position.x)
                        }
                    )
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            if (dataPoints.size >= 2 && canvasWidth > 0f && canvasHeight > 0f) {
                val offsets = ChartMathTransformer.transformToScreenPixels(
                    points = dataPoints,
                    bounds = chartBounds,
                    componentWidth = canvasWidth,
                    componentHeight = canvasHeight
                )

                if (pixelOffsets.size != offsets.size) {
                    pixelOffsets.clear()
                    pixelOffsets.addAll(offsets)
                }

                val lineColor = Color(0xFF16A34A)

                val strokePath = Path().apply {
                    moveTo(offsets.first().x, offsets.first().y)
                    for (i in 1 until offsets.size) {
                        lineTo(offsets[i].x, offsets[i].y)
                    }
                }

                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(offsets.last().x, canvasHeight)
                    lineTo(offsets.first().x, canvasHeight)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )

                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                if (selectedIndex in offsets.indices) {
                    val highlighted = offsets[selectedIndex]

                    drawLine(
                        color = lineColor.copy(alpha = 0.65f),
                        start = Offset(x = highlighted.x, y = 0f),
                        end = Offset(x = highlighted.x, y = canvasHeight),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = highlighted
                    )

                    drawCircle(
                        color = lineColor,
                        radius = 5.dp.toPx(),
                        center = highlighted
                    )
                }
            }
        }
    }
}
