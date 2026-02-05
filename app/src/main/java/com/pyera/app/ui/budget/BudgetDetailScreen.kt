package com.pyera.app.ui.budget

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.pyera.app.data.local.entity.BudgetWithSpending
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.AccentGreen
import com.pyera.app.ui.theme.CardBackground
import com.pyera.app.ui.theme.ColorError
import com.pyera.app.ui.theme.ColorWarning
import com.pyera.app.ui.theme.DeepBackground
import com.pyera.app.ui.theme.ErrorContainer
import com.pyera.app.ui.theme.SuccessContainer
import com.pyera.app.ui.theme.TextPrimary
import com.pyera.app.ui.theme.TextSecondary
import com.pyera.app.ui.theme.TextTertiary
import com.pyera.app.ui.theme.WarningContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailScreen(
    budgetId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val budget by viewModel.selectedBudget.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showMenu by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(budgetId) {
        viewModel.loadBudgetDetail(budgetId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Details", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = TextPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(CardBackground)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = AccentGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Edit", color = TextPrimary)
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToEdit(budgetId)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = ColorError,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Delete", color = ColorError)
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBackground
                )
            )
        },
        containerColor = DeepBackground
    ) { padding ->
        if (isLoading || budget == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentGreen)
            }
        } else {
            budget?.let { budgetData ->
                BudgetDetailContent(
                    budget = budgetData,
                    modifier = Modifier.padding(padding)
                )
            }
        }

        if (showDeleteDialog) {
            DeleteBudgetDialog(
                onConfirm = {
                    budget?.let {
                        viewModel.deleteBudget(
                            com.pyera.app.data.local.entity.BudgetEntity(
                                id = it.id,
                                userId = it.userId,
                                categoryId = it.categoryId,
                                amount = it.amount,
                                period = it.period,
                                startDate = it.startDate,
                                isActive = it.isActive
                            )
                        )
                    }
                    showDeleteDialog = false
                    onNavigateBack()
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
private fun BudgetDetailContent(
    budget: BudgetWithSpending,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Category Header Card
        CategoryHeaderCard(budget = budget)

        // Progress Overview Card
        ProgressOverviewCard(budget = budget)

        // Budget Details Card
        BudgetDetailsCard(budget = budget)

        // Status Card
        StatusCard(status = budget.status, remaining = budget.remainingAmount)
    }
}

@Composable
private fun CategoryHeaderCard(budget: BudgetWithSpending) {
    PyeraCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(budget.categoryColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = budget.categoryName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = budget.categoryName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = budget.period.name.lowercase()
                        .replaceFirstChar { it.uppercase() } + " Budget",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ProgressOverviewCard(budget: BudgetWithSpending) {
    val progress = budget.progressPercentage.coerceIn(0f, 1f)
    val statusColor = when (budget.status) {
        BudgetStatus.HEALTHY, BudgetStatus.ON_TRACK -> AccentGreen
        BudgetStatus.WARNING -> ColorWarning
        BudgetStatus.OVER_BUDGET -> ColorError
    }

    PyeraCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Spending Progress",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Circle with Percentage
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(140.dp),
                        color = statusColor,
                        trackColor = TextTertiary.copy(alpha = 0.2f),
                        strokeWidth = 12.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = String.format("%.0f%%", budget.progressPercentage * 100),
                        style = MaterialTheme.typography.headlineLarge,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "of budget used",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Linear progress bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = statusColor,
                trackColor = TextTertiary.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun BudgetDetailsCard(budget: BudgetWithSpending) {
    PyeraCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Budget Breakdown",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Budget Amount
            BudgetDetailRow(
                label = "Budget Limit",
                amount = budget.amount,
                color = AccentGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Spent Amount
            BudgetDetailRow(
                label = "Amount Spent",
                amount = budget.spentAmount,
                color = if (budget.isOverBudget) ColorError else TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Remaining Amount
            BudgetDetailRow(
                label = "Remaining",
                amount = budget.remainingAmount,
                color = when {
                    budget.remainingAmount < 0 -> ColorError
                    budget.remainingAmount < budget.amount * 0.2 -> ColorWarning
                    else -> AccentGreen
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.Divider(
                color = TextTertiary.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Alert Threshold
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alert Threshold",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = String.format("%.0f%%", budget.alertThreshold * 100),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun BudgetDetailRow(
    label: String,
    amount: Double,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
        Text(
            text = formatCurrencyDetailed(amount),
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusCard(
    status: BudgetStatus,
    remaining: Double
) {
    val (backgroundColor, iconColor, title, message) = when (status) {
        BudgetStatus.HEALTHY -> Quadruple(
            SuccessContainer,
            AccentGreen,
            "On Track",
            "You're managing your budget well! Keep it up."
        )
        BudgetStatus.ON_TRACK -> Quadruple(
            SuccessContainer,
            AccentGreen,
            "Looking Good",
            "You're within budget. Stay mindful of your spending."
        )
        BudgetStatus.WARNING -> Quadruple(
            WarningContainer,
            ColorWarning,
            "Approaching Limit",
            "You're close to your budget limit. Consider reducing spending."
        )
        BudgetStatus.OVER_BUDGET -> Quadruple(
            ErrorContainer,
            ColorError,
            "Over Budget",
            "You've exceeded your budget. Review your expenses."
        )
    }

    PyeraCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = iconColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DeleteBudgetDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text(
                text = "Delete Budget",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete this budget? This action cannot be undone.",
                color = TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = ColorError, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private fun formatCurrencyDetailed(amount: Double): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.getDefault())
    return formatter.format(amount)
}
