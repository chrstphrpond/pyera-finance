package com.pyera.app.ui.account

import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.data.local.entity.displayName
import com.pyera.app.data.local.entity.formattedBalance
import com.pyera.app.ui.navigation.Screen
import com.pyera.app.ui.theme.*
import com.pyera.app.ui.util.CurrencyFormatter
import com.pyera.app.ui.util.pyeraBackground
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    accountId: Long,
    navController: NavController,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val selectedAccount by viewModel.selectedAccount.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val transactions by remember(accountId) {
        viewModel.transactionsForAccount(accountId)
    }.collectAsStateWithLifecycle(initialValue = emptyList())
    
    // Load account details when screen is shown
    LaunchedEffect(accountId) {
        viewModel.loadAccountDetail(accountId)
    }
    
    selectedAccount?.let { account ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Account Details") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.initEditForm(account)
                            navController.navigate(Screen.EditAccount.createRoute(accountId))
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            floatingActionButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Transfer button
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(Screen.Transfer.createRoute(accountId))
                        },
                        containerColor = ColorTokens.SurfaceLevel2,
                        contentColor = ColorTokens.Primary500
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Transfer")
                    }
                    
                    // Add transaction button
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(Screen.AddTransaction.route)
                        },
                        containerColor = ColorTokens.Primary500,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                    }
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .pyeraBackground()
                    .padding(padding),
                contentPadding = PaddingValues(SpacingTokens.Medium),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.Medium)
            ) {
                // Account Header Card
                item {
                    AccountHeaderCard(account = account)
                }
                
                // Quick Stats
                item {
                    AccountQuickStats(account = account)
                }
                
                // Recent Transactions Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(onClick = { 
                            navController.navigate(Screen.Main.Transactions.route)
                        }) {
                            Text("See All", color = ColorTokens.Primary500)
                        }
                    }
                }
                
                if (transactions.isEmpty()) {
                    item {
                        EmptyTransactionsPlaceholder()
                    }
                } else {
                    items(transactions.take(5)) { transaction ->
                        AccountTransactionItem(transaction = transaction)
                    }
                }
            }
        }
    } ?: run {
        // Loading or error state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = ColorTokens.Primary500)
            } else {
                Text("Account not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AccountHeaderCard(account: AccountEntity) {
    PyeraCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        containerColor = ColorTokens.SurfaceLevel2,
        borderWidth = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.Large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Account Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(account.color)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = account.icon,
                    fontSize = 40.sp
                )
            }
            
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            
            // Account Name
            Text(
                text = account.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            
            // Account Type
            Text(
                text = account.type.displayName(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            
            // Balance
            Text(
                text = account.formattedBalance(),
                style = MaterialTheme.typography.headlineMedium,
                color = if (account.balance >= 0) ColorTokens.Primary500 else ColorTokens.Error500,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Current Balance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Status Badges
            if (account.isDefault || account.isArchived) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (account.isDefault) {
                        Surface(
                            color = ColorTokens.Primary500.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(SpacingTokens.Medium)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = ColorTokens.Primary500,
                                    modifier = Modifier.size(SpacingTokens.Medium)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Default",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ColorTokens.Primary500
                                )
                            }
                        }
                    }
                    
                    if (account.isArchived) {
                        Surface(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(SpacingTokens.Medium)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Archive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(SpacingTokens.Medium)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Archived",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountTransactionItem(transaction: TransactionEntity) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val amountText = if (transaction.type == "INCOME") {
        "+ ${CurrencyFormatter.format(transaction.amount)}"
    } else {
        "- ${CurrencyFormatter.format(transaction.amount)}"
    }

    PyeraCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = SpacingTokens.Medium,
        containerColor = ColorTokens.SurfaceLevel2,
        borderWidth = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.note.ifBlank { "Transaction" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormat.format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = amountText,
                style = MaterialTheme.typography.bodyLarge,
                color = if (transaction.type == "INCOME") ColorTokens.Success500 else ColorTokens.Error500,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AccountQuickStats(account: AccountEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            title = "Currency",
            value = account.currency,
            icon = "💱",
            modifier = Modifier.weight(1f)
        )
        
        QuickStatCard(
            title = "Created",
            value = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                .format(Date(account.createdAt)),
            icon = "📅",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    PyeraCard(
        modifier = modifier,
        cornerRadius = 12.dp,
        containerColor = ColorTokens.SurfaceLevel1,
        borderWidth = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.Medium)
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Suppress("DEPRECATION")
@Composable
private fun EmptyTransactionsPlaceholder() {
    PyeraCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = SpacingTokens.Medium,
        containerColor = ColorTokens.SurfaceLevel1,
        borderWidth = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.ExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            Text(
                text = "No transactions yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Transactions for this account will appear here",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
    }
}



