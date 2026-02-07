package com.pyera.app.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.pyera.app.data.local.entity.formattedBalance
import com.pyera.app.ui.navigation.Screen
import com.pyera.app.ui.theme.*
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
                        containerColor = DeepBackground,
                        titleContentColor = TextPrimary,
                        navigationIconContentColor = TextPrimary
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
                        containerColor = SurfaceElevated,
                        contentColor = AccentGreen
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Transfer")
                    }
                    
                    // Add transaction button
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(Screen.AddTransaction.route)
                        },
                        containerColor = AccentGreen,
                        contentColor = DeepBackground
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                    }
                }
            },
            containerColor = DeepBackground
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            color = TextPrimary
                        )
                        TextButton(onClick = { 
                            navController.navigate(Screen.Main.Transactions.route)
                        }) {
                            Text("See All", color = AccentGreen)
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
                CircularProgressIndicator(color = AccentGreen)
            } else {
                Text("Account not found", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun AccountHeaderCard(account: AccountEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Account Name
            Text(
                text = account.name,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            
            // Account Type
            Text(
                text = account.type.displayName(),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Balance
            Text(
                text = account.formattedBalance(),
                style = MaterialTheme.typography.headlineMedium,
                color = if (account.balance >= 0) AccentGreen else ColorError,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Current Balance",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            
            // Status Badges
            if (account.isDefault || account.isArchived) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (account.isDefault) {
                        Surface(
                            color = AccentGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Default",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AccentGreen
                                )
                            }
                        }
                    }
                    
                    if (account.isArchived) {
                        Surface(
                            color = TextSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Archive,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Archived",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
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
        "+ ₱${String.format("%,.2f", transaction.amount)}"
    } else {
        "- ₱${String.format("%,.2f", transaction.amount)}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.note.ifBlank { "Transaction" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormat.format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = amountText,
                style = MaterialTheme.typography.bodyLarge,
                color = if (transaction.type == "INCOME") ColorSuccess else ColorError,
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun EmptyTransactionsPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No transactions yet",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Transactions for this account will appear here",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
    }
}
