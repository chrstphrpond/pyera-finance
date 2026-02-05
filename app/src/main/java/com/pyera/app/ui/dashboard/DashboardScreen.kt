package com.pyera.app.ui.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyera.app.ui.components.*
import com.pyera.app.ui.dashboard.QuickTemplatesRow
import com.pyera.app.ui.navigation.Screen
import com.pyera.app.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.pyera.app.R
import com.pyera.app.ui.util.bounceClick
import java.util.Calendar

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
    onSettingsClick: () -> Unit = {},
    onTemplatesClick: () -> Unit = {}
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
            userName = state.userName,
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
                accountName = stringResource(R.string.dashboard_main_account),
                accountIdentifier = stringResource(R.string.dashboard_account_identifier),
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

        Spacer(modifier = Modifier.height(Spacing.Large))

        // Quick Stats Row showing transaction count, budgets, and goals
        QuickStatsRow(
            transactionCount = state.transactionCount,
            activeBudgetsCount = state.activeBudgetsCount,
            savingsGoalsCount = state.savingsGoalsCount
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

        // Quick Templates Row - shows most used templates for one-tap entry
        QuickTemplatesRow(
            onTemplateClick = { templateId ->
                // Navigate to add transaction screen with template ID
                // Template data will be pre-filled
                onAddTransactionClick()
            },
            onSeeAllClick = onTemplatesClick,
            onAddTemplateClick = onTemplatesClick
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
    userName: String,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val greeting = getGreeting()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "$greeting,",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.dashboard_settings_content_desc),
                    tint = TextSecondary
                )
            }
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.dashboard_profile_content_desc),
                    tint = TextSecondary
                )
            }
        }
    }
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> stringResource(R.string.dashboard_greeting_morning).trimEnd(',')
        in 12..17 -> stringResource(R.string.dashboard_greeting_afternoon).trimEnd(',')
        else -> stringResource(R.string.dashboard_greeting_evening).trimEnd(',')
    }
}

@Composable
private fun EnhancedBalanceCard(
    balance: Double,
    income: Double,
    expenses: Double
) {
    // Scale animation for balance changes
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "balance_scale"
    )
    
    PyeraCard(
        borderColor = NeonYellow.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.XLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Total Balance Label
            Text(
                text = stringResource(R.string.dashboard_total_balance),
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(Spacing.Small))

            // Balance Amount with animation
            AnimatedContent(
                targetState = balance,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith 
                    fadeOut(animationSpec = tween(300))
                },
                label = "balance_animation"
            ) { targetBalance ->
                PyeraCurrencyText(
                    amount = targetBalance,
                    style = MaterialTheme.typography.displaySmall,
                    showSign = false,
                    modifier = Modifier.scale(scale)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.Large))

            // Income/Expense Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IncomeExpenseIndicator(
                    label = stringResource(R.string.dashboard_income),
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
                    label = stringResource(R.string.dashboard_expense),
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
                contentDescription = if (isPositive) "Income indicator" else "Expense indicator",
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
        horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main Add Button - Using FilledIconButton with NeonYellow as specified
        FilledIconButton(
            onClick = onAddTransaction,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = NeonYellow,
                contentColor = DarkGreen
            ),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.dashboard_quick_action_add))
        }

        // Other quick action buttons
        QuickActionButton(
            icon = Icons.Default.CameraAlt,
            label = stringResource(R.string.dashboard_quick_action_scan),
            onClick = onScanReceipt,
            containerColor = SurfaceElevated,
            contentColor = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        QuickActionButton(
            icon = Icons.Default.Lightbulb,
            label = stringResource(R.string.dashboard_quick_action_insights),
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
                text = stringResource(R.string.dashboard_recent_transactions),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            if (transactions.isNotEmpty()) {
                TextButton(onClick = onViewAll) {
                    Text(stringResource(R.string.dashboard_view_all), color = NeonYellow)
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.Medium))

        // Content - Empty State or Transaction List
        if (transactions.isEmpty()) {
            // Using transaction-appropriate icon with proper accessibility
            PyeraEmptyState(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                title = stringResource(R.string.transaction_list_empty_title),
                description = stringResource(R.string.empty_state_transactions_description),
                actionLabel = stringResource(R.string.empty_state_transactions_button),
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
private fun QuickStatsRow(
    transactionCount: Int,
    activeBudgetsCount: Int,
    savingsGoalsCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
    ) {
        QuickStatItem(
            icon = Icons.Default.ReceiptLong,
            value = transactionCount.toString(),
            label = stringResource(R.string.dashboard_stat_transactions),
            modifier = Modifier.weight(1f)
        )
        QuickStatItem(
            icon = Icons.Default.AccountBalanceWallet,
            value = activeBudgetsCount.toString(),
            label = stringResource(R.string.dashboard_stat_budgets),
            modifier = Modifier.weight(1f)
        )
        QuickStatItem(
            icon = Icons.Default.Savings,
            value = savingsGoalsCount.toString(),
            label = stringResource(R.string.dashboard_stat_goals),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    PyeraCard(
        modifier = modifier,
        containerColor = SurfaceElevated,
        borderColor = ColorBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.dashboard_stat_transactions, label),
                tint = NeonYellow,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.XSmall))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
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
                        contentDescription = if (transaction.isIncome) stringResource(R.string.dashboard_income) else stringResource(R.string.dashboard_expense),
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

            // Amount using ₱ symbol consistently
            val amountColor = if (transaction.isIncome) ColorIncome else TextPrimary
            val sign = if (transaction.isIncome) "+" else "-"
            Text(
                text = "$sign₱${transaction.amount}",
                style = MaterialTheme.typography.bodyLarge,
                color = amountColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
