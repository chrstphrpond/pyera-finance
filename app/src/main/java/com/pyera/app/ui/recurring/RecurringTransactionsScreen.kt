package com.pyera.app.ui.recurring

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pyera.app.data.local.entity.RecurringFrequency
import com.pyera.app.data.local.entity.RecurringTransactionEntity
import com.pyera.app.data.local.entity.TransactionType
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.navigation.Screen
import com.pyera.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionsScreen(
    navController: NavController,
    viewModel: RecurringTransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

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
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBackground,
                    titleContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.Recurring.Add.route) },
                containerColor = AccentGreen,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recurring Transaction")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DeepBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentGreen
                )
            } else if (uiState.recurringTransactions.isEmpty()) {
                EmptyRecurringState(
                    onAddClick = { navController.navigate(Screen.Recurring.Add.route) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(
                        items = uiState.recurringTransactions,
                        key = { it.id }
                    ) { recurring ->
                        RecurringTransactionItem(
                            recurring = recurring,
                            viewModel = viewModel,
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
    onEditClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
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
                    colors = ButtonDefaults.buttonColors(containerColor = ColorError)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceElevated,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    PyeraCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                            if (recurring.isActive) AccentGreen.copy(alpha = 0.2f)
                            else TextSecondary.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        tint = if (recurring.isActive) AccentGreen else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Description and frequency
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recurring.description,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (recurring.isActive) TextPrimary else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$frequencyLabel • ${if (recurring.type == TransactionType.INCOME) "Income" else "Expense"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }

                // Amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (recurring.type == TransactionType.INCOME) "+" else "-"}₱${String.format("%.2f", recurring.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (recurring.isActive) amountColor else TextSecondary,
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
                        color = TextTertiary
                    )
                    Text(
                        text = nextDueLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            isOverdue -> ColorError
                            recurring.isActive -> ColorSuccess
                            else -> TextSecondary
                        },
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Action buttons
                Row {
                    // Toggle active/pause button
                    IconButton(
                        onClick = {
                            viewModel.toggleActiveStatus(recurring.id, !recurring.isActive)
                        }
                    ) {
                        Icon(
                            imageVector = if (recurring.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (recurring.isActive) "Pause" else "Resume",
                            tint = if (recurring.isActive) ColorWarning else ColorSuccess
                        )
                    }

                    // Edit button
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = AccentGreen
                        )
                    }

                    // Delete button
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ColorError.copy(alpha = 0.7f)
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
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AccentGreen.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Recurring Transactions",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Set up automatic transactions for regular income or expenses like salary, rent, or subscriptions.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
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
