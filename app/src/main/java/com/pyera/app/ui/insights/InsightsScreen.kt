package com.pyera.app.ui.insights

import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.graphics.Color
import com.pyera.app.domain.analysis.AnalysisPeriod
import com.pyera.app.domain.analysis.FinancialTip
import com.pyera.app.ui.components.EmptyState
import com.pyera.app.ui.components.LoadingState
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.insights.components.*
import com.pyera.app.ui.theme.*
import com.pyera.app.ui.util.CurrencyFormatter
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
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshInsights() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = ColorTokens.Primary500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = Color.Transparent
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

                    Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                    // Main content
                    uiState.spendingInsights?.let { insights ->
                        // Summary card
                        InsightsSummaryCard(
                            totalSpending = insights.totalSpending,
                            averageDaily = insights.averageDaily,
                            transactionCount = insights.transactionCount,
                            trend = insights.spendingTrend,
                            percentageChange = insights.percentageChange,
                            modifier = Modifier.padding(horizontal = SpacingTokens.MediumLarge)
                        )

                        Spacer(modifier = Modifier.height(SpacingTokens.MediumLarge))

                        // Period comparison
                        uiState.periodComparison?.let { comparison ->
                            ComparisonSection(comparison = comparison)
                            Spacer(modifier = Modifier.height(SpacingTokens.MediumLarge))
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
                            Spacer(modifier = Modifier.height(SpacingTokens.MediumLarge))
                        }

                        // Category breakdown
                        if (uiState.categoryInsights.isNotEmpty()) {
                            CategoryBreakdownSection(
                                categories = uiState.categoryInsights,
                                showAll = uiState.showAllCategories,
                                onShowAllToggle = { viewModel.toggleShowAllCategories() }
                            )
                            Spacer(modifier = Modifier.height(SpacingTokens.MediumLarge))
                        }

                        // Budget adherence
                        uiState.budgetAdherence?.let { adherence ->
                            BudgetAdherenceSection(
                                adherence = adherence,
                                onViewBudgets = onNavigateToBudget
                            )
                            Spacer(modifier = Modifier.height(SpacingTokens.MediumLarge))
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
                            Spacer(modifier = Modifier.height(SpacingTokens.MediumLarge))
                        }

                        Spacer(modifier = Modifier.height(SpacingTokens.ExtraLarge))
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
            .padding(horizontal = SpacingTokens.MediumLarge),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Small)
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
                    selectedContainerColor = ColorTokens.Primary500,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = ColorTokens.SurfaceLevel2,
                    labelColor = MaterialTheme.colorScheme.onBackground
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) ColorTokens.Primary500 else ColorBorder
                )
            )
        }
    }
}

/**
 * Period comparison section
 */
@Suppress("DEPRECATION")
@Composable
private fun ComparisonSection(comparison: com.pyera.app.domain.analysis.PeriodComparison) {
    Column(modifier = Modifier.padding(horizontal = SpacingTokens.MediumLarge)) {
        Text(
            text = "Period Comparison",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.MediumSmall)
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
                        .padding(SpacingTokens.MediumLarge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Change",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(SpacingTokens.Small))

                    val isIncrease = comparison.absoluteChange > 0
                    val changeColor = if (isIncrease) ColorTokens.Warning500 else ColorTokens.Success500
                    val changeIcon = if (isIncrease) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
                    val signedChange = if (isIncrease) {
                        CurrencyFormatter.formatWithSign(kotlin.math.abs(comparison.absoluteChange))
                    } else {
                        CurrencyFormatter.formatWithSign(-kotlin.math.abs(comparison.absoluteChange))
                    }

                    Icon(
                        imageVector = changeIcon,
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(SpacingTokens.Large)
                    )

                    Spacer(modifier = Modifier.height(SpacingTokens.Small))

                    Text(
                        text = signedChange,
                        style = MaterialTheme.typography.titleMedium,
                        color = changeColor,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${String.format("%.1f", kotlin.math.abs(comparison.percentageChange))}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
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

    Column(modifier = Modifier.padding(horizontal = SpacingTokens.MediumLarge)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Small)
            ) {
                Text(
                    text = "Alerts",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )
                if (anomalies.isNotEmpty()) {
                    Badge(
                        containerColor = ColorTokens.Error500
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
                            color = ColorTokens.Primary500
                        )
                    }
                }
                TextButton(onClick = onDismissAll) {
                    Text(
                        text = "Dismiss All",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.Small)) {
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

    Column(modifier = Modifier.padding(horizontal = SpacingTokens.MediumLarge)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Top Spending Categories",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )

            if (categories.size > 5) {
                TextButton(onClick = onShowAllToggle) {
                    Text(
                        text = if (showAll) "Show Less" else "Show All",
                        color = ColorTokens.Primary500
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.Small)) {
            displayedCategories.forEach { category ->
                CategoryBreakdownItem(category = category)
            }
        }
    }
}

/**
 * Individual category breakdown item
 */
@Suppress("DEPRECATION")
@Composable
private fun CategoryBreakdownItem(
    category: com.pyera.app.domain.analysis.CategoryInsight
) {
    PyeraCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.MediumLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.MediumSmall)
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
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )

                if (category.transactionCount > 0) {
                    Text(
                        text = "${category.transactionCount} transactions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }

            // Amount and trend
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(category.currentPeriodSpending),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )

                if (category.percentageChange != 0.0) {
                    val isIncrease = category.percentageChange > 0
                    val trendColor = if (isIncrease) ColorTokens.Warning500 else ColorTokens.Success500
                    val trendIcon = if (isIncrease) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown

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
    Column(modifier = Modifier.padding(horizontal = SpacingTokens.MediumLarge)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Budget Status",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )

            onViewBudgets?.let {
                TextButton(onClick = it) {
                    Text(
                        text = "View All",
                        color = ColorTokens.Primary500
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

        // Overall progress
        val overallPercentage = (adherence.overallAdherencePercentage * 100).toInt()
        val overallColor = when {
            overallPercentage < 50 -> ColorTokens.Error500
            overallPercentage < 80 -> ColorTokens.Warning500
            else -> ColorTokens.Success500
        }

        PyeraCard {
            Column(modifier = Modifier.padding(SpacingTokens.MediumLarge)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Overall Budget Health",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "$overallPercentage%",
                        style = MaterialTheme.typography.titleMedium,
                        color = overallColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(SpacingTokens.Small))

                LinearProgressIndicator(
                    progress = { adherence.overallAdherencePercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = overallColor,
                    trackColor = SurfaceOverlay
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Small))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Spent: ${CurrencyFormatter.format(adherence.totalSpent)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "Budget: ${CurrencyFormatter.format(adherence.totalBudgetAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

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
            Spacer(modifier = Modifier.height(SpacingTokens.Small))
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
    Column(modifier = Modifier.padding(horizontal = SpacingTokens.MediumLarge)) {
        Text(
            text = "Personalized Tips",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.Small)) {
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



