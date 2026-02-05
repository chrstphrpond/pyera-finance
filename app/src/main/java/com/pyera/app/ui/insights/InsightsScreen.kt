package com.pyera.app.ui.insights

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyera.app.domain.analysis.AnalysisPeriod
import com.pyera.app.ui.components.EmptyState
import com.pyera.app.ui.components.LoadingState
import com.pyera.app.ui.insights.components.*
import com.pyera.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Main Insights Screen that displays comprehensive spending analysis,
 * anomalies, tips, and visualizations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = hiltViewModel(),
    onNavigateToBudget: (() -> Unit)? = null,
    onNavigateToTransactions: (() -> Unit)? = null,
    onNavigateToSavings: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Spending Insights",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshInsights() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = NeonYellow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGreen
                )
            )
        },
        containerColor = DarkGreen
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.spendingInsights == null -> {
                LoadingState(
                    message = "Analyzing your spending...",
                    modifier = Modifier.padding(paddingValues)
                )
            }
            uiState.error != null && uiState.spendingInsights == null -> {
                ErrorState(
                    error = uiState.error!!,
                    onRetry = { viewModel.retry() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                ) {
                    // Period selector
                    PeriodSelector(
                        selectedPeriod = uiState.selectedPeriod,
                        onPeriodSelected = { viewModel.setPeriod(it) }
                    )

                    Spacer(modifier = Modifier.height(Spacing.Large))

                    // Main content
                    uiState.spendingInsights?.let { insights ->
                        // Summary card
                        InsightsSummaryCard(
                            totalSpending = insights.totalSpending,
                            averageDaily = insights.averageDaily,
                            transactionCount = insights.transactionCount,
                            trend = insights.spendingTrend,
                            percentageChange = insights.percentageChange,
                            modifier = Modifier.padding(horizontal = Spacing.ScreenPadding)
                        )

                        Spacer(modifier = Modifier.height(Spacing.XLarge))

                        // Period comparison
                        uiState.periodComparison?.let { comparison ->
                            ComparisonSection(comparison = comparison)
                            Spacer(modifier = Modifier.height(Spacing.XLarge))
                        }

                        // Anomalies/Alerts
                        if (uiState.anomalies.isNotEmpty()) {
                            AnomaliesSection(
                                anomalies = uiState.anomalies,
                                showAll = uiState.showAllAnomalies,
                                onShowAllToggle = { viewModel.toggleShowAllAnomalies() },
                                onDismissAnomaly = { viewModel.dismissAnomaly(it) },
                                onDismissAll = { viewModel.dismissAllAnomalies() }
                            )
                            Spacer(modifier = Modifier.height(Spacing.XLarge))
                        }

                        // Category breakdown
                        if (uiState.categoryInsights.isNotEmpty()) {
                            CategoryBreakdownSection(
                                categories = uiState.categoryInsights,
                                showAll = uiState.showAllCategories,
                                onShowAllToggle = { viewModel.toggleShowAllCategories() }
                            )
                            Spacer(modifier = Modifier.height(Spacing.XLarge))
                        }

                        // Budget adherence
                        uiState.budgetAdherence?.let { adherence ->
                            BudgetAdherenceSection(
                                adherence = adherence,
                                onViewBudgets = onNavigateToBudget
                            )
                            Spacer(modifier = Modifier.height(Spacing.XLarge))
                        }

                        // Personalized tips
                        if (uiState.tips.isNotEmpty()) {
                            TipsSection(
                                tips = uiState.tips,
                                onActionClick = { tip ->
                                    when {
                                        tip.actionRoute?.startsWith("budget") == true ->
                                            onNavigateToBudget?.invoke()
                                        tip.actionRoute?.startsWith("transactions") == true ->
                                            onNavigateToTransactions?.invoke()
                                        tip.actionRoute?.startsWith("savings") == true ->
                                            onNavigateToSavings?.invoke()
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(Spacing.XLarge))
                        }

                        Spacer(modifier = Modifier.height(Spacing.XXXLarge))
                    }
                }
            }
        }
    }
}

/**
 * Period selector chips
 */
@Composable
private fun PeriodSelector(
    selectedPeriod: AnalysisPeriod,
    onPeriodSelected: (AnalysisPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
    ) {
        AnalysisPeriod.values().filter { it != AnalysisPeriod.CUSTOM }.forEach { period ->
            val isSelected = period == selectedPeriod
            FilterChip(
                selected = isSelected,
                onClick = { onPeriodSelected(period) },
                label = {
                    Text(
                        text = when (period) {
                            AnalysisPeriod.WEEKLY -> "Weekly"
                            AnalysisPeriod.MONTHLY -> "Monthly"
                            AnalysisPeriod.QUARTERLY -> "Quarterly"
                            AnalysisPeriod.YEARLY -> "Yearly"
                            AnalysisPeriod.CUSTOM -> "Custom"
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonYellow,
                    selectedLabelColor = DarkGreen,
                    containerColor = SurfaceElevated,
                    labelColor = TextPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) NeonYellow else ColorBorder
                )
            )
        }
    }
}

/**
 * Period comparison section
 */
@Composable
private fun ComparisonSection(comparison: com.pyera.app.domain.analysis.PeriodComparison) {
    Column(modifier = Modifier.padding(horizontal = Spacing.ScreenPadding)) {
        Text(
            text = "Period Comparison",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(Spacing.Medium))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
        ) {
            TrendCard(
                title = "Current Period",
                currentAmount = comparison.currentSpending,
                previousAmount = comparison.previousSpending,
                percentageChange = comparison.percentageChange,
                trend = comparison.trendDirection,
                modifier = Modifier.weight(1f)
            )

            // Change indicator card
            PyeraCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.CardPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(Spacing.Small))

                    val isIncrease = comparison.absoluteChange > 0
                    val changeColor = if (isIncrease) ColorWarning else ColorSuccess
                    val changeIcon = if (isIncrease) Icons.Default.TrendingUp else Icons.Default.TrendingDown

                    Icon(
                        imageVector = changeIcon,
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.height(Spacing.Small))

                    Text(
                        text = "${if (isIncrease) "+" else ""}₱${String.format("%,.2f", kotlin.math.abs(comparison.absoluteChange))}",
                        style = MaterialTheme.typography.titleMedium,
                        color = changeColor,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${String.format("%.1f", kotlin.math.abs(comparison.percentageChange))}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

/**
 * Anomalies/Alerts section
 */
@Composable
private fun AnomaliesSection(
    anomalies: List<com.pyera.app.domain.analysis.SpendingAnomaly>,
    showAll: Boolean,
    onShowAllToggle: () -> Unit,
    onDismissAnomaly: (Long) -> Unit,
    onDismissAll: () -> Unit
) {
    val displayedAnomalies = if (showAll) anomalies else anomalies.take(2)

    Column(modifier = Modifier.padding(horizontal = Spacing.ScreenPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                Text(
                    text = "Alerts",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                if (anomalies.isNotEmpty()) {
                    Badge(
                        containerColor = ColorError
                    ) {
                        Text(
                            text = anomalies.size.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Row {
                if (anomalies.size > 2) {
                    TextButton(onClick = onShowAllToggle) {
                        Text(
                            text = if (showAll) "Show Less" else "Show All",
                            color = NeonYellow
                        )
                    }
                }
                TextButton(onClick = onDismissAll) {
                    Text(
                        text = "Dismiss All",
                        color = TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.Medium))

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
            displayedAnomalies.forEach { anomaly ->
                AnomalyAlertCard(
                    anomaly = anomaly,
                    onDismiss = { onDismissAnomaly(anomaly.id) }
                )
            }
        }
    }
}

/**
 * Category breakdown section
 */
@Composable
private fun CategoryBreakdownSection(
    categories: List<com.pyera.app.domain.analysis.CategoryInsight>,
    showAll: Boolean,
    onShowAllToggle: () -> Unit
) {
    val displayedCategories = if (showAll) categories else categories.take(5)

    Column(modifier = Modifier.padding(horizontal = Spacing.ScreenPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Top Spending Categories",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            if (categories.size > 5) {
                TextButton(onClick = onShowAllToggle) {
                    Text(
                        text = if (showAll) "Show Less" else "Show All",
                        color = NeonYellow
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.Medium))

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
            displayedCategories.forEach { category ->
                CategoryBreakdownItem(category = category)
            }
        }
    }
}

/**
 * Individual category breakdown item
 */
@Composable
private fun CategoryBreakdownItem(
    category: com.pyera.app.domain.analysis.CategoryInsight
) {
    val totalSpending = category.currentPeriodSpending + category.previousPeriodSpending
    val percentage = if (totalSpending > 0) {
        (category.currentPeriodSpending / totalSpending * 100)
    } else 0.0

    PyeraCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
        ) {
            // Category color indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(androidx.compose.ui.graphics.Color(category.categoryColor))
            )

            // Category info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )

                if (category.transactionCount > 0) {
                    Text(
                        text = "${category.transactionCount} transactions",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }

            // Amount and trend
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₱${String.format("%,.2f", category.currentPeriodSpending)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                if (category.percentageChange != 0.0) {
                    val isIncrease = category.percentageChange > 0
                    val trendColor = if (isIncrease) ColorWarning else ColorSuccess
                    val trendIcon = if (isIncrease) Icons.Default.TrendingUp else Icons.Default.TrendingDown

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = trendIcon,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${String.format("%.1f", kotlin.math.abs(category.percentageChange))}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = trendColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Budget adherence section
 */
@Composable
private fun BudgetAdherenceSection(
    adherence: com.pyera.app.domain.analysis.BudgetAdherence,
    onViewBudgets: (() -> Unit)?
) {
    Column(modifier = Modifier.padding(horizontal = Spacing.ScreenPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Budget Status",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            onViewBudgets?.let {
                TextButton(onClick = it) {
                    Text(
                        text = "View All",
                        color = NeonYellow
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.Medium))

        // Overall progress
        val overallPercentage = (adherence.overallAdherencePercentage * 100).toInt()
        val overallColor = when {
            overallPercentage < 50 -> ColorError
            overallPercentage < 80 -> ColorWarning
            else -> ColorSuccess
        }

        PyeraCard {
            Column(modifier = Modifier.padding(Spacing.CardPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overall Budget Health",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "$overallPercentage%",
                        style = MaterialTheme.typography.titleMedium,
                        color = overallColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.Small))

                LinearProgressIndicator(
                    progress = { adherence.overallAdherencePercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = overallColor,
                    trackColor = SurfaceOverlay
                )

                Spacer(modifier = Modifier.height(Spacing.Small))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Spent: ₱${String.format("%,.2f", adherence.totalSpent)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Text(
                        text = "Budget: ₱${String.format("%,.2f", adherence.totalBudgetAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.Medium))

        // Over budget alerts
        adherence.overBudgetCategories.take(2).forEach { budget ->
            BudgetAdherenceCard(
                categoryName = budget.categoryName,
                budgetAmount = budget.budgetAmount,
                spentAmount = budget.spentAmount,
                percentageUsed = budget.percentageUsed,
                daysRemaining = budget.daysRemaining,
                categoryColor = budget.categoryColor
            )
            Spacer(modifier = Modifier.height(Spacing.Small))
        }

        // Near limit alerts
        adherence.nearLimitCategories.take(1).forEach { budget ->
            BudgetAdherenceCard(
                categoryName = budget.categoryName,
                budgetAmount = budget.budgetAmount,
                spentAmount = budget.spentAmount,
                percentageUsed = budget.percentageUsed,
                daysRemaining = budget.daysRemaining,
                categoryColor = budget.categoryColor
            )
        }
    }
}

/**
 * Personalized tips section
 */
@Composable
private fun TipsSection(
    tips: List<FinancialTip>,
    onActionClick: (FinancialTip) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = Spacing.ScreenPadding)) {
        Text(
            text = "Personalized Tips",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(Spacing.Medium))

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
            tips.take(3).forEach { tip ->
                TipCard(
                    tip = tip,
                    onActionClick = if (tip.actionLabel != null) {
                        { onActionClick(tip) }
                    } else null
                )
            }
        }
    }
}

/**
 * Error state view
 */
@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        EmptyState(
            icon = Icons.Default.Error,
            title = "Failed to Load Insights",
            subtitle = error,
            actionLabel = "Try Again",
            onAction = onRetry
        )
    }
}
