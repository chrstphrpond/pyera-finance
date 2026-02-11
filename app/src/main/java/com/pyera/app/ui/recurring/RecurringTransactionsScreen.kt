package com.pyera.app.ui.recurring

import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pyera.app.data.local.entity.RecurringTransactionEntity
import com.pyera.app.data.local.entity.TransactionType
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.navigation.Screen
import com.pyera.app.ui.theme.*
import com.pyera.app.ui.util.CurrencyFormatter
import com.pyera.app.ui.util.pyeraBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionsScreen(
    navController: NavController,
    viewModel: RecurringTransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Transactions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.Recurring.Add.route) },
                containerColor = ColorTokens.Primary500,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recurring Transaction")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pyeraBackground()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = ColorTokens.Primary500
                )
            } else if (uiState.recurringTransactions.isEmpty()) {
                EmptyRecurringState(
                    onAddClick = { navController.navigate(Screen.Recurring.Add.route) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = SpacingTokens.Medium),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = SpacingTokens.Medium)
                ) {
                    val accountsById = accounts.associateBy { it.id }
                    items(
                        items = uiState.recurringTransactions,
                        key = { it.id }
                    ) { recurring ->
                        val account = accountsById[recurring.accountId]
                        RecurringTransactionItem(
                            recurring = recurring,
                            viewModel = viewModel,
                            accountName = account?.name,
                            isMissingAccount = recurring.accountId == null,
                            onEditClick = {
                                navController.navigate(Screen.Recurring.Edit.createRoute(recurring.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecurringTransactionItem(
    recurring: RecurringTransactionEntity,
    viewModel: RecurringTransactionsViewModel,
    accountName: String?,
    isMissingAccount: Boolean,
    onEditClick: () -> Unit
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val frequencyLabel = viewModel.getFrequencyLabel(recurring.frequency)
    val nextDueLabel = viewModel.getNextDueLabel(recurring.nextDueDate)

    val amountColor = if (recurring.type == TransactionType.INCOME) ColorIncome else ColorExpense
    val isOverdue = recurring.nextDueDate < System.currentTimeMillis() && recurring.isActive

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recurring Transaction") },
            text = { Text("Are you sure you want to delete \"${recurring.description}\"? This will not affect past transactions.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecurring(recurring)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Error500)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = ColorTokens.SurfaceLevel2,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    PyeraCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Medium)
        ) {
            // Header row with icon, description, and amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (recurring.isActive) ColorTokens.Primary500.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        tint = if (recurring.isActive) ColorTokens.Primary500 else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(SpacingTokens.Large)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Description and frequency
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recurring.description,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (recurring.isActive) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$frequencyLabel • ${if (recurring.type == TransactionType.INCOME) "Income" else "Expense"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                    if (isMissingAccount) {
                        Text(
                            text = "Account required",
                            style = MaterialTheme.typography.bodySmall,
                            color = ColorTokens.Error500,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (!accountName.isNullOrBlank()) {
                        Text(
                            text = "Account: $accountName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }

                // Amount
                Column(horizontalAlignment = Alignment.End) {
                    val signedAmount = if (recurring.type == TransactionType.INCOME) {
                        recurring.amount
                    } else {
                        -recurring.amount
                    }
                    Text(
                        text = CurrencyFormatter.formatWithSign(signedAmount),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (recurring.isActive) amountColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Divider
            HorizontalDivider(color = ColorBorder.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(12.dp))

            // Footer row with next due date and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Next due date
                Column {
                    Text(
                        text = "Next due",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                    Text(
                        text = nextDueLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            isOverdue -> ColorTokens.Error500
                            recurring.isActive -> ColorTokens.Success500
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Action buttons
                Row {
                    if (isMissingAccount) {
                        TextButton(onClick = onEditClick) {
                            Text("Assign")
                        }
                    }
                    // Toggle active/pause button
                    IconButton(
                        onClick = {
                            viewModel.toggleActiveStatus(recurring.id, !recurring.isActive)
                        }
                    ) {
                        Icon(
                            imageVector = if (recurring.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (recurring.isActive) "Pause" else "Resume",
                            tint = if (recurring.isActive) ColorTokens.Warning500 else ColorTokens.Success500
                        )
                    }

                    // Edit button
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = ColorTokens.Primary500
                        )
                    }

                    // Delete button
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ColorTokens.Error500.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyRecurringState(
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.ExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(ColorTokens.Primary500.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = null,
                tint = ColorTokens.Primary500,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(SpacingTokens.Large))

        Text(
            text = "No Recurring Transactions",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Set up automatic transactions for regular income or expenses like salary, rent, or subscriptions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(SpacingTokens.Large))

        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Primary500)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Recurring Transaction", color = Color.Black)
        }
    }
}

@Composable
private fun HorizontalDivider(
    modifier: Modifier = Modifier,
    color: Color = ColorBorder
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}



