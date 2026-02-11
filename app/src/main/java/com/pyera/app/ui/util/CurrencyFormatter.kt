package com.pyera.app.ui.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Utility object for formatting currency values throughout the app.
 * Currently configured for Philippine Peso (PHP).
 */
object CurrencyFormatter {
    const val SYMBOL = "\u20B1"
    private val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    private val numberFormatShort = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
    }

    /**
     * Formats a double amount as currency with 2 decimal places.
     * Example: 1234.5 -> "PHP 1,234.50"
     */
    fun format(amount: Double): String {
        return "$SYMBOL${numberFormat.format(amount)}"
    }

    /**
     * Formats a double amount as currency without decimal places.
     * Example: 1234.5 -> "PHP 1,235"
     */
    fun formatShort(amount: Double): String {
        return "$SYMBOL${numberFormatShort.format(amount)}"
    }

    /**
     * Formats a double amount with sign prefix (+ or -).
     * Example: 1234.5 -> "+PHP 1,234.50", -1234.5 -> "-PHP 1,234.50"
     */
    fun formatWithSign(amount: Double): String {
        val sign = if (amount >= 0) "+" else ""
        return "$sign$SYMBOL${numberFormat.format(kotlin.math.abs(amount))}"
    }

    /**
     * Formats a double amount as a compact representation.
     * Example: 1500.0 -> "PHP 1.5K", 1000000.0 -> "PHP 1M"
     */
    fun formatCompact(amount: Double): String {
        return when {
            amount >= 1_000_000 -> "$SYMBOL${String.format("%.1fM", amount / 1_000_000)}"
            amount >= 1_000 -> "$SYMBOL${String.format("%.1fK", amount / 1_000)}"
            else -> format(amount)
        }
    }

    /**
     * Formats just the number part without the currency symbol.
     * Example: 1234.5 -> "1,234.50"
     */
    fun formatNumberOnly(amount: Double): String {
        return numberFormat.format(amount)
    }
}
