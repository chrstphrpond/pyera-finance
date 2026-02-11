package com.pyera.app.ui.budget

import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pyera.app.data.local.entity.BudgetPeriod
import com.pyera.app.data.local.entity.BudgetStatus
import com.pyera.app.data.local.entity.BudgetSummary
import com.pyera.app.data.local.entity.BudgetWithSpending
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.CardBackground
import com.pyera.app.ui.theme.SuccessContainer
import com.pyera.app.ui.theme.ErrorContainer
import com.pyera.app.ui.theme.WarningContainer
import com.pyera.app.ui.util.pyeraBackground
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun BudgetListScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val budgets by viewModel.budgets.collectAsState()
    val summary by viewModel.budgetSummary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = ColorTokens.Primary500,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pyeraBackground()
                .padding(padding)
        ) {
            // Header
            BudgetListHeader(
                summary = summary,
                selectedPeriod = selectedPeriod,
                onPeriodChange = { viewModel.setPeriod(it) },
                statusFilter = statusFilter,
                onStatusFilterChange = { viewModel.setStatusFilter(it) }
            )

            // Budget List
            if (isLoading && budgets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ColorTokens.Primary500)
                }
            } else if (budgets.isEmpty()) {
                EmptyBudgetState(onCreateClick = onNavigateToCreate)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(SpacingTokens.Medium)
                ) {
                    items(budgets, key = { it.id }) { budget ->
                        BudgetListItem(
                            budget = budget,
                            onClick = { onNavigateToDetail(budget.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetListHeader(
    summary: BudgetSummary?,
    selectedPeriod: BudgetPeriod,
    onPeriodChange: (BudgetPeriod) -> Unit,
    statusFilter: BudgetStatus?,
    onStatusFilterChange: (BudgetStatus?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SpacingTokens.Medium)
    ) {
        // Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Budgets",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track your spending limits",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Period Dropdown
            PeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodChange = onPeriodChange
            )
        }

        Spacer(modifier = Modifier.height(SpacingTokens.Medium))

        // Summary Cards
        if (summary != null) {
            BudgetSummaryCards(summary = summary)
        }

        Spacer(modifier = Modifier.height(SpacingTokens.Medium))

        // Filter Chips
        BudgetFilterChips(
            selectedFilter = statusFilter,
            onFilterChange = onStatusFilterChange
        )
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: BudgetPeriod,
    onPeriodChange: (BudgetPeriod) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(CardBackground)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedPeriod.name.lowercase().replaceFirstChar { it.uppercase() },
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select period",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CardBackground)
        ) {
            BudgetPeriod.values().forEach { period ->
                DropdownMenuItem(
                    text = {
                        Text(
                            period.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    onClick = {
                        onPeriodChange(period)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BudgetSummaryCards(summary: BudgetSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total Budget Card
        SummaryCard(
            title = "Total Budget",
            amount = summary.totalBudgetAmount,
            modifier = Modifier.weight(1f),
            color = ColorTokens.Primary500
        )

        // Spent Card
        SummaryCard(
            title = "Spent",
            amount = summary.totalSpent,
            modifier = Modifier.weight(1f),
            color = if (summary.totalSpent > summary.totalBudgetAmount) ColorTokens.Error500 else ColorTokens.Warning500
        )

        // Remaining Card
        SummaryCard(
            title = "Remaining",
            amount = summary.totalRemaining,
            modifier = Modifier.weight(1f),
            color = if (summary.totalRemaining < 0) ColorTokens.Error500 else ColorTokens.Primary500
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Double,
    modifier: Modifier = Modifier,
    color: Color = ColorTokens.Primary500
) {
    PyeraCard(
        modifier = modifier,
        cornerRadius = 12.dp
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BudgetFilterChips(
    selectedFilter: BudgetStatus?,
    onFilterChange: (BudgetStatus?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            label = "All",
            isSelected = selectedFilter == null,
            onClick = { onFilterChange(null) }
        )

        FilterChip(
            label = "Warning",
            isSelected = selectedFilter == BudgetStatus.WARNING,
            onClick = { onFilterChange(BudgetStatus.WARNING) },
            color = ColorTokens.Warning500
        )

        FilterChip(
            label = "Over Budget",
            isSelected = selectedFilter == BudgetStatus.OVER_BUDGET,
            onClick = { onFilterChange(BudgetStatus.OVER_BUDGET) },
            color = ColorTokens.Error500
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color = ColorTokens.Primary500
) {
    val backgroundColor = if (isSelected) color.copy(alpha = 0.2f) else CardBackground
    val textColor = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = SpacingTokens.Medium, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun BudgetListItem(
    budget: BudgetWithSpending,
    onClick: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = budget.progressPercentage.coerceIn(0f, 1f),
        label = "progress"
    )

    val statusColor = when (budget.status) {
        BudgetStatus.HEALTHY -> ColorTokens.Primary500
        BudgetStatus.ON_TRACK -> ColorTokens.Primary500.copy(alpha = 0.7f)
        BudgetStatus.WARNING -> ColorTokens.Warning500
        BudgetStatus.OVER_BUDGET -> ColorTokens.Error500
    }

    PyeraCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(SpacingTokens.Medium)
                .fillMaxWidth()
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
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
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = budget.period.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Indicator
                BudgetStatusIndicator(status = budget.status)
            }

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            // Progress Bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f).copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Amounts Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(budget.spentAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (budget.isOverBudget) ColorTokens.Error500 else MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Budget",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(budget.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Over Budget Warning
            AnimatedVisibility(
                visible = budget.isOverBudget,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ErrorContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ColorTokens.Error500,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Over budget by ${formatCurrency(kotlin.math.abs(budget.remainingAmount))}",
                        color = ColorTokens.Error500,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetStatusIndicator(status: BudgetStatus) {
    val (backgroundColor, textColor, label) = when (status) {
        BudgetStatus.HEALTHY -> Triple(
            SuccessContainer,
            ColorTokens.Primary500,
            "On Track"
        )
        BudgetStatus.ON_TRACK -> Triple(
            SuccessContainer,
            ColorTokens.Primary500,
            "Good"
        )
        BudgetStatus.WARNING -> Triple(
            WarningContainer,
            ColorTokens.Warning500,
            "Warning"
        )
        BudgetStatus.OVER_BUDGET -> Triple(
            com.pyera.app.ui.theme.ErrorContainer,
            ColorTokens.Error500,
            "Over"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
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
private fun EmptyBudgetState(onCreateClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(SpacingTokens.ExtraLarge)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            Text(
                text = "No budgets yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Set up budgets to track your spending and stay on top of your finances",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(SpacingTokens.Large))
            androidx.compose.material3.Button(
                onClick = onCreateClick,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = ColorTokens.Primary500
                )
            ) {
                Text(
                    "Create Budget",
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.getDefault())
    return formatter.format(amount)
}




