package com.pyera.app.ui.components
import com.pyera.app.ui.theme.tokens.ColorTokens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pyera.app.ui.theme.NegativeChange
import com.pyera.app.ui.theme.PositiveChange
import com.pyera.app.ui.util.CurrencyFormatter
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun BalanceDisplayLarge(
    balance: Double,
    currencySymbol: String = CurrencyFormatter.SYMBOL,
    label: String = "Current balance",
    percentageChange: Float? = null,
    changeTimeframe: String = "1d",
    modifier: Modifier = Modifier
) {
    val animatedBalance = com.pyera.app.ui.util.animateCountUp(targetValue = balance)
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    val wholePart = numberFormat.format(animatedBalance.toLong())
    val decimalPart = String.format("%02d", ((animatedBalance * 100) % 100).toInt().let { abs(it) })

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Balance amount
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("$currencySymbol$wholePart")
                }
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append(".$decimalPart")
                }
            }
        )

        // Percentage change badge
        if (percentageChange != null) {
            Spacer(modifier = Modifier.height(12.dp))
            PercentageChangeBadge(
                percentage = percentageChange,
                timeframe = changeTimeframe
            )
        }
    }
}

@Suppress("DEPRECATION")
@Composable
private fun PercentageChangeBadge(
    percentage: Float,
    timeframe: String,
    modifier: Modifier = Modifier
) {
    val isPositive = percentage >= 0
    val color = if (isPositive) PositiveChange else NegativeChange
    val icon = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
    val sign = if (isPositive) "+" else ""

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "$sign${String.format("%.2f", percentage)}%",
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "($timeframe)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        )
    }
}



