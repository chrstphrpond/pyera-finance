package com.pyera.app.ui.insights.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pyera.app.domain.analysis.AnomalySeverity
import com.pyera.app.domain.analysis.AnomalyType
import com.pyera.app.domain.analysis.FinancialTip
import com.pyera.app.domain.analysis.SpendingAnomaly
import com.pyera.app.domain.analysis.TipType
import com.pyera.app.domain.analysis.TrendDirection
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.*
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens
import com.pyera.app.ui.util.CurrencyFormatter

/**
 * Card displaying a single spending insight with icon and description
 */
@Composable
fun InsightCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    PyeraCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(SpacingTokens.MediumLarge)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.MediumSmall)
            ) {
                // Icon with background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconBackgroundColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconBackgroundColor,
                        modifier = Modifier.size(SpacingTokens.Large)
                    )
                }

                // Text content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Optional action button
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))
                TextButton(
                    onClick = onActionClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = ColorTokens.Primary500)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

/**
 * Card displaying spending trend with visual indicator
 */
@Suppress("UNUSED_PARAMETER", "DEPRECATION")
@Composable
fun TrendCard(
    title: String,
    currentAmount: Double,
    previousAmount: Double,
    percentageChange: Double,
    trend: TrendDirection,
    modifier: Modifier = Modifier
) {
    val trendColor = when (trend) {
        TrendDirection.INCREASING -> ColorTokens.Warning500
        TrendDirection.DECREASING -> ColorTokens.Success500
        TrendDirection.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val trendIcon = when (trend) {
        TrendDirection.INCREASING -> Icons.AutoMirrored.Filled.TrendingUp
        TrendDirection.DECREASING -> Icons.AutoMirrored.Filled.TrendingDown
        TrendDirection.STABLE -> Icons.Default.TrendingFlat
    }

    PyeraCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(SpacingTokens.MediumLarge)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Small))

            Text(
                text = CurrencyFormatter.format(currentAmount),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Small))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(SpacingTokens.Medium)
                )
                Text(
                    text = "${String.format("%.1f", kotlin.math.abs(percentageChange))}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = trendColor,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "vs last period",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        }
    }
}

/**
 * Card for displaying budget adherence status
 */
@Composable
fun BudgetAdherenceCard(
    categoryName: String,
    budgetAmount: Double,
    spentAmount: Double,
    percentageUsed: Float,
    daysRemaining: Int,
    categoryColor: Int,
    modifier: Modifier = Modifier
) {
    val progressColor = when {
        percentageUsed > 1f -> ColorTokens.Error500
        percentageUsed > 0.8f -> ColorTokens.Warning500
        else -> ColorTokens.Success500
    }

    val remainingAmount = budgetAmount - spentAmount

    PyeraCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(SpacingTokens.MediumLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Small)
                ) {
                    // Category color indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(categoryColor))
                    )
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Status badge
                val statusText = when {
                    percentageUsed > 1f -> "Over Budget"
                    percentageUsed > 0.8f -> "Near Limit"
                    else -> "On Track"
                }
                val statusColor = when {
                    percentageUsed > 1f -> ColorTokens.Error500
                    percentageUsed > 0.8f -> ColorTokens.Warning500
                    else -> ColorTokens.Success500
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

            // Progress bar
            LinearProgressIndicator(
                progress = { percentageUsed.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = SurfaceOverlay
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Small))

            // Amount details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                    Text(
                        text = CurrencyFormatter.format(spentAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (remainingAmount >= 0) "Remaining" else "Over",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                    Text(
                        text = CurrencyFormatter.format(kotlin.math.abs(remainingAmount)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (remainingAmount >= 0) ColorTokens.Success500 else ColorTokens.Error500,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (daysRemaining > 0) {
                Spacer(modifier = Modifier.height(SpacingTokens.Small))
                Text(
                    text = "$daysRemaining days remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        }
    }
}

/**
 * Card displaying a detected anomaly/alert
 */
@Composable
fun AnomalyAlertCard(
    anomaly: SpendingAnomaly,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, iconColor, backgroundColor) = when (anomaly.severity) {
        AnomalySeverity.CRITICAL -> Triple(
            Icons.Default.Warning,
            ColorTokens.Error500,
            ColorErrorContainer
        )
        AnomalySeverity.HIGH -> Triple(
            Icons.Default.Error,
            ColorTokens.Warning500,
            ColorWarningContainer
        )
        AnomalySeverity.MEDIUM -> Triple(
            Icons.Default.Info,
            ColorTokens.Info500,
            ColorTokens.Info500.copy(alpha = 0.15f)
        )
        AnomalySeverity.LOW -> Triple(
            Icons.Default.Info,
            MaterialTheme.colorScheme.onSurfaceVariant,
            ColorTokens.SurfaceLevel2
        )
    }

    val typeLabel = when (anomaly.anomalyType) {
        AnomalyType.UNUSUAL_AMOUNT -> "Unusual Amount"
        AnomalyType.UNUSUAL_MERCHANT -> "New Merchant"
        AnomalyType.UNUSUAL_TIME -> "Unusual Time"
        AnomalyType.UNUSUAL_CATEGORY -> "Unusual Category"
        AnomalyType.DUPLICATE_TRANSACTION -> "Possible Duplicate"
        AnomalyType.FREQUENCY_SPIKE -> "High Activity"
    }

    PyeraCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = backgroundColor,
        cornerRadius = 12.dp,
        borderWidth = 0.dp
    ) {
        Column(modifier = Modifier.padding(SpacingTokens.MediumLarge)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.MediumSmall)
            ) {
                // Severity icon
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(SpacingTokens.Large)
                )

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = iconColor,
                            fontWeight = FontWeight.Medium
                        )
                        if (anomaly.severity == AnomalySeverity.CRITICAL || anomaly.severity == AnomalySeverity.HIGH) {
                            Surface(
                                color = iconColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = anomaly.severity.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = iconColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(SpacingTokens.ExtraSmall))

                    Text(
                        text = anomaly.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    anomaly.suggestedAction?.let { action ->
                        Spacer(modifier = Modifier.height(SpacingTokens.ExtraSmall))
                        Text(
                            text = action,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Dismiss button
                IconButton(onClick = onDismiss, modifier = Modifier.size(SpacingTokens.ExtraLarge)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Card displaying a personalized financial tip
 */
@Composable
fun TipCard(
    tip: FinancialTip,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (icon, iconColor) = when (tip.type) {
        TipType.SAVINGS_OPPORTUNITY -> Pair(Icons.Default.Savings, ColorTokens.Success500)
        TipType.BUDGET_ALERT -> Pair(Icons.Default.AccountBalanceWallet, ColorTokens.Warning500)
        TipType.SPENDING_PATTERN -> Pair(Icons.Default.Insights, ColorInfo)
        TipType.GOAL_PROGRESS -> Pair(Icons.Default.Flag, ColorTokens.Primary500)
        TipType.GENERAL -> Pair(Icons.Default.Lightbulb, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    val borderColor = when (tip.type) {
        TipType.SAVINGS_OPPORTUNITY -> ColorTokens.Success500.copy(alpha = 0.3f)
        TipType.BUDGET_ALERT -> ColorTokens.Warning500.copy(alpha = 0.3f)
        TipType.SPENDING_PATTERN -> ColorInfo.copy(alpha = 0.3f)
        TipType.GOAL_PROGRESS -> ColorTokens.Primary500.copy(alpha = 0.3f)
        TipType.GENERAL -> ColorBorder
    }

    PyeraCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = borderColor
    ) {
        Column(modifier = Modifier.padding(SpacingTokens.MediumLarge)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.MediumSmall)
            ) {
                // Tip icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tip.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(SpacingTokens.ExtraSmall))

                    Text(
                        text = tip.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action button
            if (onActionClick != null && tip.actionLabel != null) {
                Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onActionClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = ColorTokens.Primary500)
                    ) {
                        Text(tip.actionLabel)
                    }
                }
            }
        }
    }
}

/**
 * Summary card for the top of the insights screen
 */
@Suppress("DEPRECATION")
@Composable
fun InsightsSummaryCard(
    totalSpending: Double,
    averageDaily: Double,
    transactionCount: Int,
    trend: TrendDirection,
    percentageChange: Double,
    modifier: Modifier = Modifier
) {
    val trendColor = when (trend) {
        TrendDirection.INCREASING -> ColorTokens.Warning500
        TrendDirection.DECREASING -> ColorTokens.Success500
        TrendDirection.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    PyeraCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = ColorTokens.Primary500.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.MediumLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Spending",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Small))

            Text(
                text = CurrencyFormatter.format(totalSpending),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Small))

            // Trend indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val trendIcon = when (trend) {
                    TrendDirection.INCREASING -> Icons.AutoMirrored.Filled.TrendingUp
                    TrendDirection.DECREASING -> Icons.AutoMirrored.Filled.TrendingDown
                    TrendDirection.STABLE -> Icons.Default.Remove
                }

                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(SpacingTokens.Medium)
                )
                Text(
                    text = "${String.format("%.1f", kotlin.math.abs(percentageChange))}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = trendColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "vs last period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            HorizontalDivider(color = ColorBorder)

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Daily Avg",
                    value = CurrencyFormatter.format(averageDaily)
                )
                StatItem(
                    label = "Transactions",
                    value = transactionCount.toString()
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        )
        Spacer(modifier = Modifier.height(SpacingTokens.ExtraSmall))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
    }
}


