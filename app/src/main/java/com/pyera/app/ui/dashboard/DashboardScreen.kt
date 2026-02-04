package com.pyera.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pyera.app.ui.components.AccountSelectorPill
import com.pyera.app.ui.components.AssetListItem
import com.pyera.app.ui.components.BalanceDisplayLarge
import com.pyera.app.ui.components.CircularActionButton
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.AccentGreen
import com.pyera.app.ui.theme.ColorSuccess
import com.pyera.app.ui.theme.DeepBackground
import com.pyera.app.ui.theme.GreenGlowSubtle
import com.pyera.app.ui.theme.NegativeChange
import com.pyera.app.ui.theme.Orange
import com.pyera.app.ui.theme.PaleViolet
import com.pyera.app.ui.theme.PositiveChange
import com.pyera.app.ui.theme.TextSecondary
import com.pyera.app.ui.theme.TextTertiary
import com.pyera.app.ui.util.bounceClick
import com.pyera.app.ui.util.pulseGlow
import com.pyera.app.ui.util.staggeredSlideIn
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset

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
    onAccountSelectorClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
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

        Spacer(modifier = Modifier.height(32.dp))

        // Large Balance Display with ambient glow
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Radial glow background
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GreenGlowSubtle,
                                Color.Transparent
                            ),
                            center = Offset.Unspecified,
                            radius = 400f
                        )
                    )
            )
            BalanceDisplayLarge(
                balance = state.totalBalance,
                label = "Current balance",
                percentageChange = 0.25f,
                changeTimeframe = "1d",
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CircularActionButton(
                icon = Icons.Default.Add,
                label = "Add",
                isHighlighted = true,
                onClick = onAddTransactionClick,
                modifier = Modifier.pulseGlow(enabled = true)
            )
            CircularActionButton(
                icon = Icons.Default.Analytics,
                label = "Analysis",
                onClick = onAnalysisClick
            )
            CircularActionButton(
                icon = Icons.Default.Receipt,
                label = "Bills",
                onClick = onBillsClick
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // My Accounts Section
        SectionHeader(
            title = "My accounts",
            onSeeAllClick = onViewAllTransactionsClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssetListItem(
                icon = Icons.Outlined.Savings,
                iconColor = AccentGreen,
                title = "Savings",
                subtitle = "Primary savings",
                amount = "₱${String.format("%,.2f", state.totalIncome)}",
                changePercent = 2.45f,
                onClick = {},
                modifier = Modifier.staggeredSlideIn(index = 0)
            )
            AssetListItem(
                icon = Icons.Outlined.CreditCard,
                iconColor = Orange,
                title = "Expenses",
                subtitle = "Monthly spending",
                amount = "₱${String.format("%,.2f", state.totalExpense)}",
                changePercent = -1.23f,
                onClick = {},
                modifier = Modifier.staggeredSlideIn(index = 1)
            )
            AssetListItem(
                icon = Icons.Outlined.AccountBalanceWallet,
                iconColor = PaleViolet,
                title = "Budget",
                subtitle = "Remaining budget",
                amount = "₱${String.format("%,.2f", state.totalBalance)}",
                onClick = onBudgetClick,
                modifier = Modifier.staggeredSlideIn(index = 2)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Transactions Section
        SectionHeader(
            title = "Recent transactions",
            onSeeAllClick = onViewAllTransactionsClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.recentTransactions.isEmpty()) {
            EmptyTransactionsState(onAddTransactionClick = onAddTransactionClick)
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.recentTransactions.take(3).forEachIndexed { index, transaction ->
                    TransactionItem(
                        transaction = transaction,
                        modifier = Modifier.staggeredSlideIn(index = index + 3) // Offset by asset list items
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )

        TextButton(onClick = onSeeAllClick) {
            Text(
                text = "see all",
                style = MaterialTheme.typography.labelMedium,
                color = AccentGreen
            )
        }
    }
}

@Composable
internal fun EmptyTransactionsState(onAddTransactionClick: () -> Unit) {
    PyeraCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AccentGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    tint = AccentGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No transactions yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap + to add your first transaction",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddTransactionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    contentColor = DeepBackground
                ),
                modifier = Modifier.bounceClick(onClick = onAddTransactionClick)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Transaction")
            }
        }
    }
}

@Composable
internal fun TransactionItem(
    transaction: TransactionUiModel,
    modifier: Modifier = Modifier
) {
    val amountColor = if (transaction.isIncome) PositiveChange else MaterialTheme.colorScheme.onBackground
    val sign = if (transaction.isIncome) "+" else "-"
    val iconColor = if (transaction.isIncome) PositiveChange else NegativeChange

    PyeraCard(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.isIncome)
                            Icons.Default.ArrowDownward
                        else
                            Icons.Default.ArrowUpward,
                        contentDescription = if (transaction.isIncome) "Income" else "Expense",
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${transaction.category} • ${transaction.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = "$sign ₱${transaction.amount}",
                style = MaterialTheme.typography.bodyLarge,
                color = amountColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
