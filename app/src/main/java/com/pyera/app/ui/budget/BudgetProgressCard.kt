package com.pyera.app.ui.budget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pyera.app.data.local.entity.BudgetPeriod
import com.pyera.app.data.local.entity.BudgetStatus
import com.pyera.app.data.local.entity.BudgetWithSpending
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.AccentGreen
import com.pyera.app.ui.theme.CardBackground
import com.pyera.app.ui.theme.ColorError
import com.pyera.app.ui.theme.ColorWarning
import com.pyera.app.ui.theme.ErrorContainer
import com.pyera.app.ui.theme.SuccessContainer
import com.pyera.app.ui.theme.TextPrimary
import com.pyera.app.ui.theme.TextSecondary
import com.pyera.app.ui.theme.TextTertiary
import com.pyera.app.ui.theme.WarningContainer
import java.text.NumberFormat

/**
 * Reusable budget progress card component that displays budget information
 * with visual progress indicators.
 */
@Composable
fun BudgetProgressCard(
    budget: BudgetWithSpending,
    modifier: Modifier = Modifier,
    showCircularProgress: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val progress by animateFloatAsState(
        targetValue = budget.progressPercentage.coerceIn(0f, 1f),
        label = "budget_progress"
    )

    val statusColor = budget.status.getStatusColor()

    PyeraCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with category info
            BudgetCardHeader(
                budget = budget,
                statusColor = statusColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress indicator
            if (showCircularProgress) {
                CircularBudgetProgress(
                    progress = progress,
                    percentage = budget.progressPercentage,
                    statusColor = statusColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LinearBudgetProgress(
                    progress = progress,
                    statusColor = statusColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount information
            BudgetAmountInfo(budget = budget)

            // Warning if over budget
            if (budget.isOverBudget) {
                OverBudgetWarning(
                    overAmount = kotlin.math.abs(budget.remainingAmount),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun BudgetCardHeader(
    budget: BudgetWithSpending,
    statusColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(budget.categoryColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = budget.categoryName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = budget.categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = budget.period.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Status indicator
        BudgetStatusBadge(status = budget.status)
    }
}

@Composable
private fun BudgetStatusBadge(status: BudgetStatus) {
    val (backgroundColor, textColor, label) = when (status) {
        BudgetStatus.HEALTHY -> Triple(SuccessContainer, AccentGreen, "Healthy")
        BudgetStatus.ON_TRACK -> Triple(SuccessContainer, AccentGreen, "On Track")
        BudgetStatus.WARNING -> Triple(WarningContainer, ColorWarning, "Warning")
        BudgetStatus.OVER_BUDGET -> Triple(ErrorContainer, ColorError, "Over")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LinearBudgetProgress(
    progress: Float,
    statusColor: Color
) {
    LinearProgressIndicator(
        progress = progress,
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = statusColor,
        trackColor = TextTertiary.copy(alpha = 0.2f)
    )
}

@Composable
private fun CircularBudgetProgress(
    progress: Float,
    percentage: Float,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(100.dp),
                color = statusColor,
                trackColor = TextTertiary.copy(alpha = 0.2f),
                strokeWidth = 8.dp
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.0f%%", percentage * 100),
                    style = MaterialTheme.typography.titleLarge,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "used",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun BudgetAmountInfo(budget: BudgetWithSpending) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AmountColumn(
            label = "Spent",
            amount = budget.spentAmount,
            color = if (budget.isOverBudget) ColorError else TextPrimary,
            icon = Icons.Default.TrendingDown
        )

        AmountColumn(
            label = "Budget",
            amount = budget.amount,
            color = TextPrimary,
            icon = null
        )
    }
}

@Composable
private fun AmountColumn(
    label: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector?
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun OverBudgetWarning(
    overAmount: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ErrorContainer)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = ColorError,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Over budget by ${formatCurrency(overAmount)}",
            color = ColorError,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Compact version of the budget progress card for use in dashboards
 */
@Composable
fun BudgetProgressCardCompact(
    budget: BudgetWithSpending,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val progress by animateFloatAsState(
        targetValue = budget.progressPercentage.coerceIn(0f, 1f),
        label = "compact_progress"
    )

    val statusColor = budget.status.getStatusColor()

    PyeraCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(budget.categoryColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = budget.categoryName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = budget.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = statusColor,
                    trackColor = TextTertiary.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Percentage
            Text(
                text = String.format("%.0f%%", budget.progressPercentage * 100),
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Mini budget indicator for use in lists or small spaces
 */
@Composable
fun BudgetProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    strokeWidth: Dp = 3.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "mini_progress"
    )

    val color = when {
        progress >= 1f -> ColorError
        progress >= 0.8f -> ColorWarning
        else -> AccentGreen
    }

    CircularProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier.size(size),
        color = color,
        trackColor = TextTertiary.copy(alpha = 0.2f),
        strokeWidth = strokeWidth
    )
}

/**
 * Helper function to get color based on budget status
 */
private fun BudgetStatus.getStatusColor(): Color = when (this) {
    BudgetStatus.HEALTHY, BudgetStatus.ON_TRACK -> AccentGreen
    BudgetStatus.WARNING -> ColorWarning
    BudgetStatus.OVER_BUDGET -> ColorError
}

private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance().format(amount)
}

// Preview data
private val previewBudget = BudgetWithSpending(
    id = 1,
    userId = "user1",
    categoryId = 1,
    categoryName = "Food & Dining",
    categoryColor = android.graphics.Color.parseColor("#FF6B6B"),
    categoryIcon = null,
    amount = 1000.0,
    period = BudgetPeriod.MONTHLY,
    startDate = System.currentTimeMillis(),
    isActive = true,
    alertThreshold = 0.8f,
    spentAmount = 750.0,
    remainingAmount = 250.0,
    progressPercentage = 0.75f,
    isOverBudget = false,
    daysRemaining = 15
)

@Preview
@Composable
private fun BudgetProgressCardPreview() {
    MaterialTheme {
        BudgetProgressCard(
            budget = previewBudget,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun BudgetProgressCardCompactPreview() {
    MaterialTheme {
        BudgetProgressCardCompact(
            budget = previewBudget.copy(
                spentAmount = 1200.0,
                remainingAmount = -200.0,
                progressPercentage = 1.2f,
                isOverBudget = true
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
