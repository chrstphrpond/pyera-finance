package com.pyera.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pyera.app.ui.theme.MoneyLarge
import com.pyera.app.ui.theme.MoneyMedium
import com.pyera.app.ui.theme.MoneySmall
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens
import com.pyera.app.ui.util.CurrencyFormatter
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

/**
 * Size variants for money display.
 */
enum class MoneySize {
    /** Large size (40sp) for hero sections and total balances. */
    Large,

    /** Medium size (28sp) for list items and cards. */
    Medium,

    /** Small size (16sp) for compact displays and tables. */
    Small
}

/**
 * Displays monetary amounts with consistent formatting and styling.
 *
 * Features:
 * - Currency symbol prefix
 * - Decimal part shown smaller
 * - Color coding for positive/negative
 * - Optional count-up animation
 * - Tabular number alignment
 *
 * @param amount The monetary amount to display
 * @param currency Currency symbol (default: PHP)
 * @param size Size variant (Large, Medium, Small)
 * @param isPositive Optional color hint (true=green, false=red, null=default)
 * @param showSign Whether to show + or - prefix
 * @param animate Whether to animate the amount counting up
 * @param modifier Modifier for the component
 */
@Composable
fun MoneyDisplay(
    amount: Double,
    currency: String = CurrencyFormatter.SYMBOL,
    size: MoneySize = MoneySize.Medium,
    isPositive: Boolean? = null,
    showSign: Boolean = false,
    animate: Boolean = false,
    modifier: Modifier = Modifier
) {
    val displayAmount = if (animate) {
        val animatedValue by animateFloatAsState(
            targetValue = amount.toFloat(),
            animationSpec = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            ),
            label = "money_animation"
        )
        animatedValue.toDouble()
    } else {
        amount
    }

    val absAmount = abs(displayAmount)
    val formatted = formatMoney(absAmount, currency)

    val decimalIndex = formatted.lastIndexOf('.')
    val (wholePart, decimalPart) = if (decimalIndex > 0) {
        formatted.substring(0, decimalIndex) to formatted.substring(decimalIndex)
    } else {
        formatted to ".00"
    }

    val color = when {
        isPositive == true -> ColorTokens.Success500
        isPositive == false -> ColorTokens.Error500
        else -> LocalContentColor.current
    }

    val (wholeStyle, decimalStyle) = when (size) {
        MoneySize.Large -> MoneyLarge to MoneyLarge.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp
        )
        MoneySize.Medium -> MoneyMedium to MoneyMedium.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp
        )
        MoneySize.Small -> MoneySmall to MoneySmall.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        )
    }

    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
    ) {
        if (showSign) {
            Text(
                text = if (amount >= 0) "+" else "-",
                style = wholeStyle,
                color = color,
                modifier = Modifier.padding(end = SpacingTokens.ExtraSmall)
            )
        }

        Text(
            text = currency,
            style = wholeStyle.copy(fontWeight = FontWeight.Normal),
            color = color.copy(alpha = 0.7f)
        )

        Text(
            text = wholePart.removePrefix(currency),
            style = wholeStyle,
            color = color
        )

        Text(
            text = decimalPart,
            style = decimalStyle,
            color = color.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

/**
 * Formats a monetary amount with proper decimal places.
 */
private fun formatMoney(amount: Double, symbol: String): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    formatter.maximumFractionDigits = 2
    formatter.minimumFractionDigits = 2
    val formatted = formatter.format(amount)

    return formatted.replace(Regex("[^\\d.,]"), symbol)
}
