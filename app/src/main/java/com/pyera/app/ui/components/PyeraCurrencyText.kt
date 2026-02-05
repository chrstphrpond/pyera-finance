package com.pyera.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.pyera.app.ui.theme.*
import java.text.NumberFormat
import java.util.*

@Composable
fun PyeraCurrencyText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    isPositive: Boolean? = null,
    showSign: Boolean = true,
    currencyCode: String = "PHP"
) {
    val color = when (isPositive) {
        true -> ColorIncome
        false -> ColorExpense
        null -> TextPrimary
    }
    
    val sign = when {
        !showSign -> ""
        amount > 0 -> "+"
        amount < 0 -> "-"
        else -> ""
    }
    
    val formattedAmount = formatCurrency(kotlin.math.abs(amount), currencyCode)
    
    Text(
        text = "$sign$formattedAmount",
        style = style,
        color = color,
        modifier = modifier
    )
}

private fun formatCurrency(amount: Double, currencyCode: String): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
    format.currency = Currency.getInstance(currencyCode)
    return format.format(amount)
}
