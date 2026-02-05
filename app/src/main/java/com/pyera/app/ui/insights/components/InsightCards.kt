package com.pyera.app.ui.insights.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
        Column(modifier = Modifier.padding(Spacing.CardPadding)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
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
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Text content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Optional action button
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(Spacing.Medium))
                TextButton(
                    onClick = onActionClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = NeonYellow)
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
        TrendDirection.INCREASING -> ColorWarning
        TrendDirection.DECREASING -> ColorSuccess
        TrendDirection.STABLE -> TextSecondary
    }

    val trendIcon = when (trend) {
        TrendDirection.INCREASING -> Icons.Default.TrendingUp
        TrendDirection.DECREASING -> Icons.Default.TrendingDown
        TrendDirection.STABLE -> Icons.Default.TrendingFlat
    }

    PyeraCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.CardPadding)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(Spacing.Small))

            Text(
                text = "₱${String.format("%,.2f", currentAmount)}",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Spacing.Small))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(16.dp)
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
                    color = TextTertiary
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
        percentageUsed > 1f -> ColorError
        percentageUsed > 0.8f -> ColorWarning
        else -> ColorSuccess
    }

    val remainingAmount = budgetAmount - spentAmount

    PyeraCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.CardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
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
                        color = TextPrimary,
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
                    percentageUsed > 1f -> ColorError
                    percentageUsed > 0.8f -> ColorWarning
                    else -> ColorSuccess
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

            Spacer(modifier = Modifier.height(Spacing.Medium))

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

            Spacer(modifier = Modifier.height(Spacing.Small))

            // Amount details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Text(
                        text = "₱${String.format("%,.2f", spentAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (remainingAmount >= 0) "Remaining" else "Over",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Text(
                        text = "₱${String.format("%,.2f", kotlin.math.abs(remainingAmount))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (remainingAmount >= 0) ColorSuccess else ColorError,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (daysRemaining > 0) {
                Spacer(modifier = Modifier.height(Spacing.Small))
                Text(
                    text = "$daysRemaining days remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
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
            ColorError,
            ColorErrorContainer
        )
        AnomalySeverity.HIGH -> Triple(
            Icons.Default.Error,
            ColorWarning,
            ColorWarningContainer
        )
        AnomalySeverity.MEDIUM -> Triple(
            Icons.Default.Info,
            ColorInfo,
            Color(0xFF0A1A2A)
        )
        AnomalySeverity.LOW -> Triple(
            Icons.Default.Info,
            TextSecondary,
            SurfaceElevated
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

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.CardPadding)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                // Severity icon
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
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

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = anomaly.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )

                    anomaly.suggestedAction?.let { action ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = action,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Dismiss button
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = TextSecondary,
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
        TipType.SAVINGS_OPPORTUNITY -> Pair(Icons.Default.Savings, ColorSuccess)
        TipType.BUDGET_ALERT -> Pair(Icons.Default.AccountBalanceWallet, ColorWarning)
        TipType.SPENDING_PATTERN -> Pair(Icons.Default.Insights, ColorInfo)
        TipType.GOAL_PROGRESS -> Pair(Icons.Default.Flag, NeonYellow)
        TipType.GENERAL -> Pair(Icons.Default.Lightbulb, TextSecondary)
    }

    val borderColor = when (tip.type) {
        TipType.SAVINGS_OPPORTUNITY -> ColorSuccess.copy(alpha = 0.3f)
        TipType.BUDGET_ALERT -> ColorWarning.copy(alpha = 0.3f)
        TipType.SPENDING_PATTERN -> ColorInfo.copy(alpha = 0.3f)
        TipType.GOAL_PROGRESS -> NeonYellow.copy(alpha = 0.3f)
        TipType.GENERAL -> ColorBorder
    }

    PyeraCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = borderColor
    ) {
        Column(modifier = Modifier.padding(Spacing.CardPadding)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
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
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = tip.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Action button
            if (onActionClick != null && tip.actionLabel != null) {
                Spacer(modifier = Modifier.height(Spacing.Medium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onActionClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = NeonYellow)
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
        TrendDirection.INCREASING -> ColorWarning
        TrendDirection.DECREASING -> ColorSuccess
        TrendDirection.STABLE -> TextSecondary
    }

    PyeraCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = NeonYellow.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.XLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Spending",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(Spacing.Small))

            Text(
                text = "₱${String.format("%,.2f", totalSpending)}",
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Spacing.Small))

            // Trend indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val trendIcon = when (trend) {
                    TrendDirection.INCREASING -> Icons.Default.TrendingUp
                    TrendDirection.DECREASING -> Icons.Default.TrendingDown
                    TrendDirection.STABLE -> Icons.Default.Remove
                }

                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(16.dp)
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
                    color = TextTertiary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.Large))

            HorizontalDivider(color = ColorBorder)

            Spacer(modifier = Modifier.height(Spacing.Large))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Daily Avg",
                    value = "₱${String.format("%,.2f", averageDaily)}"
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
            color = TextTertiary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
