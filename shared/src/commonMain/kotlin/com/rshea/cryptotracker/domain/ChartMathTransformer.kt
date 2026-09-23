package com.rshea.cryptotracker.domain

import androidx.compose.ui.geometry.Offset

object ChartMathTransformer {

    /**
     * Translates a list of raw domain DataPoints into concrete UI screen offsets (Pixels)
     * using linear interpolation based on the layout's active structural boundaries.
     * 
     * @param points The raw asset dataset containing pricing and timestamps.
     * @param bounds The absolute min/max limits calculated for the dataset.
     * @param componentWidth The physical width of the drawing canvas in pixels.
     * @param componentHeight The physical height of the drawing canvas in pixels.
     */
    fun transformToScreenPixels(
        points: List<DataPoint>,
        bounds: ChartBounds,
        componentWidth: Float,
        componentHeight: Float
    ): List<Offset> {
        if (points.isEmpty() || componentWidth <= 0f || componentHeight <= 0f) return emptyList()

        // 1. Pre-calculate search delta denominators to prevent repetitive divisions in the loop body
        val xDelta = bounds.maxX - bounds.minX
        val yDelta = bounds.maxY - bounds.minY

        // Fallback protectors if min and max values are identical to prevent a division-by-zero crash
        val safeXDenominator = if (xDelta == 0f) 1f else xDelta
        val safeYDenominator = if (yDelta == 0f) 1f else yDelta

        // 2. Maps points utilizing a pre-sized array constructor for zero allocation jitter
        return ArrayList<Offset>(points.size).apply {
            points.forEach { point ->
                // Step A: Calculate percentage location ratios (0.0 .. 1.0)
                val normalizedX = (point.x - bounds.minX) / safeXDenominator
                val normalizedY = (point.y - bounds.minY) / safeYDenominator

                // Step B: Project directly onto canvas coordinate boundaries
                val pixelX = normalizedX * componentWidth

                // Step C: Invert the Y axis to keep higher prices rendering at the top of the canvas
                val pixelY = componentHeight - (normalizedY * componentHeight)

                // Instantiate the UI coordinate pixel position representation block
                add(Offset(x = pixelX, y = pixelY))
            }
        }
    }

}