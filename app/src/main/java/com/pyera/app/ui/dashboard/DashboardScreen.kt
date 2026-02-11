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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyera.app.ui.components.*
import com.pyera.app.ui.navigation.Screen
import com.pyera.app.ui.theme.*
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens
import androidx.compose.ui.res.stringResource
import com.pyera.app.R
import com.pyera.app.ui.util.pyeraBackground
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
            .pyeraBackground()
            .verticalScroll(scrollState)
            .padding(horizontal = SpacingTokens.MediumLarge)
            .padding(top = SpacingTokens.MediumLarge, bottom = 100.dp)
    ) {
        // Header with greeting and action icons
        DashboardHeader(
            userName = state.userName,
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick
        )

        Spacer(modifier = Modifier.height(SpacingTokens.Large))

        // Account Selector Pill
        AccountSelectorPill(
            accountName = stringResource(R.string.dashboard_main_account),
            onClick = onAccountSelectorClick
        )

        Spacer(modifier = Modifier.height(SpacingTokens.ExtraLarge))

        // Main Balance Card - Large and prominent
        MainBalanceCard(
            balance = state.totalBalance,
            income = state.totalIncome,
            expenses = state.totalExpense
        )

        Spacer(modifier = Modifier.height(SpacingTokens.Large))

        // Quick Stats Row
        QuickStatsRowModern(
            transactionCount = state.transactionCount,
            activeBudgetsCount = state.activeBudgetsCount,
            savingsGoalsCount = state.savingsGoalsCount
        )

        Spacer(modifier = Modifier.height(SpacingTokens.Large))

        // Quick Actions Grid
        QuickActionsGrid(
            onAddTransaction = onAddTransactionClick,
            onScanReceipt = onScanReceiptClick,
            onViewAnalysis = onAnalysisClick,
            onViewInsights = onInsightsClick
        )

        Spacer(modifier = Modifier.height(SpacingTokens.Large))

        // Quick Templates Section
        QuickTemplatesRow(
            onTemplateClick = { templateId ->
                onAddTransactionClick()
            },
            onSeeAllClick = onTemplatesClick,
            onAddTemplateClick = onTemplatesClick
        )

        Spacer(modifier = Modifier.height(SpacingTokens.Large))

        // Recent Transactions Section
        RecentTransactionsSectionModern(
            transactions = state.recentTransactions,
            onViewAll = onViewAllTransactionsClick,
            onAddTransaction = onAddTransactionClick
        )

        Spacer(modifier = Modifier.height(SpacingTokens.Large))
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(ColorTokens.SurfaceLevel2, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.dashboard_settings_content_desc),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(ColorTokens.SurfaceLevel2, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = stringResource(R.string.dashboard_profile_content_desc),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> stringResource(R.string.dashboard_greeting_morning).trimEnd(',')
        in 12..17 -> stringResource(R.string.dashboard_greeting_afternoon).trimEnd(',')
        else -> stringResource(R.string.dashboard_greeting_evening).trimEnd(',')
    }
}

@Composable
private fun AccountSelectorPill(
    accountName: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(Radius.xl),
            color = ColorTokens.SurfaceLevel2,
            modifier = Modifier.height(40.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = SpacingTokens.Medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = ColorTokens.Primary500,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = accountName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Select account",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MainBalanceCard(
    balance: Double,
    income: Double,
    expenses: Double
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "balance_scale"
    )

    PyeraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Elevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Total Balance Label
            Text(
                text = stringResource(R.string.dashboard_total_balance),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Small))

            // Balance Amount
            AnimatedContent(
                targetState = balance,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith 
                    fadeOut(animationSpec = tween(300))
                },
                label = "balance_animation"
            ) { targetBalance ->
                MoneyDisplay(
                    amount = targetBalance,
                    size = MoneySize.Large,
                    modifier = Modifier.scale(scale)
                )
            }

            Spacer(modifier = Modifier.height(SpacingTokens.Large))

            // Income/Expense Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IncomeExpenseItem(
                    label = stringResource(R.string.dashboard_income),
                    amount = income,
                    isPositive = true,
                    icon = Icons.Outlined.ArrowDownward
                )

                VerticalDivider(
                    modifier = Modifier
                        .height(48.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                IncomeExpenseItem(
                    label = stringResource(R.string.dashboard_expense),
                    amount = expenses,
                    isPositive = false,
                    icon = Icons.Outlined.ArrowUpward
                )
            }
        }
    }
}

@Composable
private fun IncomeExpenseItem(
    label: String,
    amount: Double,
    isPositive: Boolean,
    icon: ImageVector
) {
    val accentColor = if (isPositive) ColorTokens.Success500 else ColorTokens.Error500
    val signedAmount = if (isPositive) amount else -amount

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(SpacingTokens.Large)
                    .background(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        MoneyDisplay(
            amount = signedAmount,
            isPositive = isPositive,
            size = MoneySize.Small,
            showSign = true
        )
    }
}

@Composable
private fun QuickStatsRowModern(
    transactionCount: Int,
    activeBudgetsCount: Int,
    savingsGoalsCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCardItem(
            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
            value = transactionCount.toString(),
            label = stringResource(R.string.dashboard_stat_transactions),
            accentColor = CardAccentBlue,
            modifier = Modifier.weight(1f)
        )
        StatCardItem(
            icon = Icons.Outlined.AccountBalanceWallet,
            value = activeBudgetsCount.toString(),
            label = stringResource(R.string.dashboard_stat_budgets),
            accentColor = CardAccentPink,
            modifier = Modifier.weight(1f)
        )
        StatCardItem(
            icon = Icons.Outlined.Savings,
            value = savingsGoalsCount.toString(),
            label = stringResource(R.string.dashboard_stat_goals),
            accentColor = CardAccentMint,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCardItem(
    icon: ImageVector,
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    PyeraCard(
        modifier = modifier,
        cornerRadius = Radius.lg,
        containerColor = ColorTokens.SurfaceLevel1,
        borderColor = accentColor.copy(alpha = 0.2f),
        gradientBrush = Brush.verticalGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.12f),
                ColorTokens.SurfaceLevel1
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onAddTransaction: () -> Unit,
    onScanReceipt: () -> Unit,
    onViewAnalysis: () -> Unit,
    onViewInsights: () -> Unit
) {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                icon = Icons.Outlined.CameraAlt,
                label = "Scan",
                accentColor = CardAccentPurple,
                onClick = onScanReceipt,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Outlined.PieChart,
                label = "Analysis",
                accentColor = CardAccentTeal,
                onClick = onViewAnalysis,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Outlined.Lightbulb,
                label = "Insights",
                accentColor = CardAccentOrange,
                onClick = onViewInsights,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PyeraCard(
        modifier = modifier,
        cornerRadius = Radius.lg,
        containerColor = ColorTokens.SurfaceLevel1,
        borderColor = accentColor.copy(alpha = 0.2f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun RecentTransactionsSectionModern(
    transactions: List<TransactionUiModel>,
    onViewAll: () -> Unit,
    onAddTransaction: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dashboard_recent_transactions),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            if (transactions.isNotEmpty()) {
                TextButton(onClick = onViewAll) {
                    Text(
                        text = stringResource(R.string.dashboard_view_all),
                        color = ColorTokens.Primary500,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            PyeraCard(
                modifier = Modifier.fillMaxWidth(),
                variant = CardVariant.Default
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.ExtraLarge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = ColorTokens.Primary500.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = ColorTokens.Primary500,
                            modifier = Modifier.size(SpacingTokens.ExtraLarge)
                        )
                    }
                    Spacer(modifier = Modifier.height(SpacingTokens.Medium))
                    Text(
                        text = stringResource(R.string.transaction_list_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.empty_state_transactions_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(SpacingTokens.Medium))
                    Button(
                        onClick = onAddTransaction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorTokens.Primary500,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(Radius.Button)
                    ) {
                        Text(stringResource(R.string.empty_state_transactions_button))
                    }
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                transactions.take(5).forEach { transaction ->
                    TransactionItemModern(transaction = transaction)
                }
            }
        }
    }
}

@Composable
private fun TransactionItemModern(transaction: TransactionUiModel) {
    val accentColor = if (transaction.isIncome) ColorTokens.Success500 else ColorTokens.Error500
    val amountValue = transaction.amount.replace(",", "").toDoubleOrNull() ?: 0.0
    val signedAmount = if (transaction.isIncome) amountValue else -amountValue

    PyeraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Default
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            accentColor.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.isIncome)
                            Icons.Outlined.ArrowDownward
                        else
                            Icons.Outlined.ArrowUpward,
                        contentDescription = if (transaction.isIncome) "Income" else "Expense",
                        tint = accentColor,
                        modifier = Modifier.size(SpacingTokens.Large)
                    )
                }

                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${transaction.category} • ${transaction.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            MoneyDisplay(
                amount = signedAmount,
                isPositive = transaction.isIncome,
                size = MoneySize.Small,
                showSign = true
            )
        }
    }
}

