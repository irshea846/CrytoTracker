package com.rshea.cryptotracker.util

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Formats a Double value cleanly to exactly two decimal places 
 * without relying on JVM-specific String.format logic.
 * Placed in the global util layer for clean, cross-layer reuse.
 */
fun Double.toTwoDecimalPlaces(): String {
    val rounded = (this * 100).roundToInt() / 100.0
    val str = rounded.toString()
    
    return if (str.contains(".")) {
        val parts = str.split(".")
        if (parts[1].length < 2) {
            str + "0"
        } else {
            str
        }
    } else {
        "$str.00"
    }
}

/**
 * Pure Kotlin Multiplatform utility to format a Double into a fixed 2-decimal String.
 * Bypasses JVM String.format() to run natively on both iOS and Android.
 */
fun Double.formatToTwoDecimals(): String {
    // Shifts the decimal two places right, rounds to nearest integer, and shifts back
    val rounded = (this * 100.0).roundToLong()
    val wholePart = rounded / 100
    val decimalPart = abs(rounded % 100)

    // Pads single-digit decimals with a trailing zero (e.g., .5 becomes .50)
    val decimalString = if (decimalPart < 10) "0$decimalPart" else "$decimalPart"
    return "$wholePart.$decimalString"
}