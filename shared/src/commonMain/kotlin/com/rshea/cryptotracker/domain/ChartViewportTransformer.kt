package com.rshea.cryptotracker.domain

object ChartViewportTransformer {

    fun calculateBounds(points: List<DataPoint>): ChartBounds {
        if (points.isEmpty()) return ChartBounds(0f, 0f, 0f, 0f)

        // Computes the absolute bounding perimeter of a dataset to prevent rendering truncation
        return ChartBounds(
            minX =  points.minOf { it.x },
            maxX =  points.maxOf { it.x },
            minY =  points.minOf { it.y },
            maxY =  points.maxOf { it.y }
        )
    }
}