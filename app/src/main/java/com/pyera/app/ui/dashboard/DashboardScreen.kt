package com.pyera.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import com.pyera.app.ui.components.*
import com.pyera.app.ui.theme.*
import com.pyera.app.ui.util.bounceClick

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onAddTransactionClick: () -> Unit = {},
    onBillsClick: () -> Unit = {},
    onBudgetClick: () -> Unit = {},
    onScanReceiptClick: () -> Unit = {},
    onTransferClick: () -> Unit = {},
    onInvestmentsClick: () -> Unit = {},
    onViewAllTransactionsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onAnalysisClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onAccountSelectorClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(Spacing.ScreenPadding)
    ) {
        // Header with greeting and action icons
        DashboardHeader(
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick
        )

        Spacer(modifier = Modifier.height(Spacing.Large))

        // Account Selector Pill (centered)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AccountSelectorPill(
                accountName = "Main Account",
                accountIdentifier = "Pyera User",
                onClick = onAccountSelectorClick
            )
        }

        Spacer(modifier = Modifier.height(Spacing.XLarge))

        // Enhanced Balance Card with NeonYellow accent
        EnhancedBalanceCard(
            balance = state.totalBalance,
            income = state.totalIncome,
            expenses = state.totalExpense
        )

        Spacer(modifier = Modifier.height(Spacing.XLarge))

        // Quick Actions Row with proper colors
        QuickActionsRow(
            onAddTransaction = onAddTransactionClick,
            onScanReceipt = onScanReceiptClick,
            onViewAnalysis = onAnalysisClick,
            onViewInsights = onInsightsClick
        )

        Spacer(modifier = Modifier.height(Spacing.XLarge))

        // Recent Transactions Section with "View All" link
        RecentTransactionsSection(
            transactions = state.recentTransactions,
            onViewAll = onViewAllTransactionsClick,
            onAddTransaction = onAddTransactionClick
        )

        Spacer(modifier = Modifier.height(Spacing.XXXLarge))
    }
}

@Composable
private fun DashboardHeader(
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good Morning,",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = "User",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondary
                )
            }
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun EnhancedBalanceCard(
    balance: Double,
    income: Double,
    expenses: Double
) {
    PyeraCard(
        borderColor = NeonYellow.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.XLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Total Balance Label
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(Spacing.Small))

            // Balance Amount
            PyeraCurrencyText(
                amount = balance,
                style = MaterialTheme.typography.displaySmall,
                showSign = false
            )

            Spacer(modifier = Modifier.height(Spacing.Large))

            // Income/Expense Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IncomeExpenseIndicator(
                    label = "Income",
                    amount = income,
                    isPositive = true,
                    icon = Icons.Default.ArrowDownward
                )

                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = ColorBorder
                )

                IncomeExpenseIndicator(
                    label = "Expense",
                    amount = expenses,
                    isPositive = false,
                    icon = Icons.Default.ArrowUpward
                )
            }
        }
    }
}

@Composable
private fun IncomeExpenseIndicator(
    label: String,
    amount: Double,
    isPositive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPositive) ColorIncome else ColorExpense,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        PyeraCurrencyText(
            amount = amount,
            style = MaterialTheme.typography.titleMedium,
            isPositive = isPositive,
            showSign = false
        )
    }
}

@Composable
private fun QuickActionsRow(
    onAddTransaction: () -> Unit,
    onScanReceipt: () -> Unit,
    onViewAnalysis: () -> Unit,
    onViewInsights: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
    ) {
        QuickActionButton(
            icon = Icons.Default.Add,
            label = "Add",
            onClick = onAddTransaction,
            containerColor = NeonYellow,
            contentColor = DarkGreen,
            modifier = Modifier.weight(1f)
        )

        QuickActionButton(
            icon = Icons.Default.CameraAlt,
            label = "Scan",
            onClick = onScanReceipt,
            containerColor = SurfaceElevated,
            contentColor = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        QuickActionButton(
            icon = Icons.Default.Lightbulb,
            label = "Insights",
            onClick = onViewInsights,
            containerColor = SurfaceElevated,
            contentColor = TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = MaterialTheme.shapes.large,
        color = containerColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun RecentTransactionsSection(
    transactions: List<TransactionUiModel>,
    onViewAll: () -> Unit,
    onAddTransaction: () -> Unit
) {
    Column {
        // Header with "View All" link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            if (transactions.isNotEmpty()) {
                TextButton(onClick = onViewAll) {
                    Text("View All", color = NeonYellow)
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.Medium))

        // Content - Empty State or Transaction List
        if (transactions.isEmpty()) {
            EmptyState(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                title = "No transactions yet",
                subtitle = "Start tracking your expenses by adding your first transaction",
                actionLabel = "Add Transaction",
                onAction = onAddTransaction
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                transactions.take(5).forEach { transaction ->
                    TransactionListItem(transaction = transaction)
                }
            }
        }
    }
}

@Composable
private fun TransactionListItem(transaction: TransactionUiModel) {
    PyeraCard(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.CardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                // Category Icon with background
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (transaction.isIncome) 
                                ColorIncome.copy(alpha = 0.15f) 
                            else 
                                ColorExpense.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.isIncome)
                            Icons.Default.ArrowDownward
                        else
                            Icons.Default.ArrowUpward,
                        contentDescription = if (transaction.isIncome) "Income" else "Expense",
                        tint = if (transaction.isIncome) ColorIncome else ColorExpense,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${transaction.category} • ${transaction.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Amount
            val amountColor = if (transaction.isIncome) ColorIncome else TextPrimary
            val sign = if (transaction.isIncome) "+" else "-"
            Text(
                text = "$sign ₱${transaction.amount}",
                style = MaterialTheme.typography.bodyLarge,
                color = amountColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
