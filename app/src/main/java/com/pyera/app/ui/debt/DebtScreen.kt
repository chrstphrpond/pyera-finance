package com.pyera.app.ui.debt

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.MoneyOff
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyera.app.data.local.entity.DebtEntity
import com.pyera.app.ui.components.ConfirmationDialog
import com.pyera.app.ui.components.EmptyDebt
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.ColorError
import com.pyera.app.ui.theme.ColorSuccess
import com.pyera.app.ui.theme.ColorWarning
import com.pyera.app.ui.theme.DeepBackground
import com.pyera.app.ui.theme.ErrorContainer
import com.pyera.app.ui.theme.AccentGreen
import com.pyera.app.ui.theme.SuccessContainer
import com.pyera.app.ui.theme.SurfaceElevated
import com.pyera.app.ui.theme.TextPrimary
import com.pyera.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtScreen(
    viewModel: DebtViewModel = hiltViewModel()
) {
    val debts by viewModel.debts.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(0) } // 0: I Owe, 1: Owed to Me
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var debtToEditId by rememberSaveable { mutableStateOf<Long?>(null) }
    val debtToEdit = debtToEditId?.let { id -> debts.find { it.id == id } }
    var debtToDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val debtToDelete = debtToDeleteId?.let { id -> debts.find { it.id == id } }
    var debtToMarkPaidId by rememberSaveable { mutableStateOf<Long?>(null) }
    val debtToMarkPaid = debtToMarkPaidId?.let { id -> debts.find { it.id == id } }
    var showCelebration by rememberSaveable { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    // Calculate summary values
    val totalYouOwe = debts.filter { it.type == "PAYABLE" && !it.isPaid }.sumOf { it.amount }
    val totalOwedToYou = debts.filter { it.type == "RECEIVABLE" && !it.isPaid }.sumOf { it.amount }
    val netPosition = totalOwedToYou - totalYouOwe

    val filteredDebts = debts.filter {
        if (selectedTab == 0) it.type == "PAYABLE" else it.type == "RECEIVABLE"
    }.filter { !it.isPaid }

    // Celebration effect
    if (showCelebration) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            showCelebration = false
        }
    }

    // Delete confirmation dialog
    debtToDelete?.let { debt ->
        ConfirmationDialog(
            title = "Delete Debt?",
            message = "Are you sure you want to delete the debt with ${debt.name} for ${formatCurrency(debt.amount)}? This action cannot be undone.",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteDebt(debt)
                debtToDeleteId = null
            },
            onDismiss = { debtToDeleteId = null }
        )
    }

    // Mark as paid confirmation dialog
    debtToMarkPaid?.let { debt ->
        ConfirmationDialog(
            title = "Mark as Paid?",
            message = "Mark the debt with ${debt.name} for ${formatCurrency(debt.amount)} as paid?",
            confirmText = "Mark Paid",
            dismissText = "Cancel",
            isDestructive = false,
            onConfirm = {
                viewModel.markAsPaid(debt)
                showCelebration = true
                debtToMarkPaidId = null
            },
            onDismiss = { debtToMarkPaidId = null }
        )
    }

    // Add/Edit dialog
    if (showAddDialog || debtToEdit != null) {
        AddEditDebtDialog(
            debt = debtToEdit,
            type = if (selectedTab == 0) "PAYABLE" else "RECEIVABLE",
            onDismiss = {
                showAddDialog = false
                debtToEditId = null
            },
            onConfirm = { name, amount, date, type ->
                debtToEdit?.let { existingDebt ->
                    viewModel.updateDebt(existingDebt.copy(name = name, amount = amount, dueDate = date))
                } ?: run {
                    viewModel.addDebt(name, amount, date, type)
                }
                showAddDialog = false
                debtToEditId = null
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalance,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Debt Manager",
                            color = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = DeepBackground,
                    scrolledContainerColor = DeepBackground
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = listState.isScrollingUp(),
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = AccentGreen,
                    contentColor = DeepBackground,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Debt")
                }
            }
        },
        containerColor = DeepBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Summary Card
            DebtSummaryCard(
                totalYouOwe = totalYouOwe,
                totalOwedToYou = totalOwedToYou,
                netPosition = netPosition,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Tab Row with badges
            DebtTabRow(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                iOweCount = debts.count { it.type == "PAYABLE" && !it.isPaid },
                owedToMeCount = debts.count { it.type == "RECEIVABLE" && !it.isPaid },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Debt List with Pull-to-Refresh
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    viewModel.refresh()
                },
                indicator = { state, trigger ->
                    SwipeRefreshIndicator(
                        state = state,
                        refreshTriggerDistance = trigger,
                        backgroundColor = SurfaceElevated,
                        contentColor = AccentGreen
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (filteredDebts.isEmpty()) {
                        EmptyDebt(
                            isIOwe = selectedTab == 0,
                            onAddClick = { showAddDialog = true }
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = filteredDebts,
                                key = { it.id }
                            ) { debt ->
                                DebtItem(
                                    debt = debt,
                                    onMarkPaid = { debtToMarkPaid = debt },
                                    onDelete = { debtToDelete = debt },
                                    onEdit = { debtToEdit = debt }
                                )
                            }
                            // Bottom spacer for FAB
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }

                    // Celebration overlay
                    if (showCelebration) {
                        CelebrationAnimation(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DebtSummaryCard(
    totalYouOwe: Double,
    totalOwedToYou: Double,
    netPosition: Double,
    modifier: Modifier = Modifier
) {
    PyeraCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = SurfaceElevated
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total You Owe
                SummaryItem(
                    title = "You Owe",
                    amount = totalYouOwe,
                    icon = Icons.Outlined.MoneyOff,
                    color = ColorError,
                    modifier = Modifier.weight(1f)
                )

                // Total Owed to You
                SummaryItem(
                    title = "Owed to You",
                    amount = totalOwedToYou,
                    icon = Icons.Outlined.AttachMoney,
                    color = ColorSuccess,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Net Position
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Net Position",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isPositive = netPosition >= 0
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isPositive) ColorSuccess else ColorError)
                    )
                    Text(
                        text = formatCurrency(netPosition),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isPositive) ColorSuccess else ColorError,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    title: String,
    amount: Double,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DebtTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    iOweCount: Int,
    owedToMeCount: Int,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        Triple("I Owe", ColorError, iOweCount),
        Triple("Owed to Me", ColorSuccess, owedToMeCount)
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            tabs.forEachIndexed { index, (title, color, count) ->
                val isSelected = selectedTab == index
                val backgroundColor = if (isSelected) {
                    color.copy(alpha = 0.15f)
                } else {
                    Color.Transparent
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor)
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Indicator dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) color else TextSecondary.copy(alpha = 0.5f))
                        )

                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) color else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )

                        // Count badge
                        if (count > 0) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) color else TextSecondary.copy(alpha = 0.3f),
                                modifier = Modifier.defaultMinSize(minWidth = 20.dp)
                            ) {
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) DeepBackground else TextPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
private fun DebtItem(
    debt: DebtEntity,
    onMarkPaid: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val isPayable = debt.type == "PAYABLE"
    val tintColor = if (isPayable) ColorError else ColorSuccess
    val containerColor = if (isPayable) ErrorContainer else SuccessContainer

    val daysRemaining = calculateDaysRemaining(debt.dueDate)
    val isOverdue = daysRemaining < 0
    val isDueSoon = daysRemaining in 0..3

    val statusColor = when {
        isOverdue -> ColorError
        isDueSoon -> ColorWarning
        else -> tintColor
    }

    val statusText = when {
        isOverdue -> "Overdue by ${-daysRemaining} day${if (-daysRemaining > 1) "s" else ""}"
        daysRemaining == 0 -> "Due today"
        daysRemaining == 1 -> "Due tomorrow"
        else -> "Due in $daysRemaining days"
    }

    PyeraCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = containerColor.copy(alpha = 0.5f),
        borderColor = tintColor.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Person icon
                Surface(
                    shape = CircleShape,
                    color = tintColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Debt info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = debt.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Due date row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formatDate(debt.dueDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Status indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isOverdue || isDueSoon) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Amount
                Text(
                    text = formatCurrency(debt.amount),
                    style = MaterialTheme.typography.headlineSmall,
                    color = tintColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = tintColor.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Edit button
                TextButton(
                    onClick = onEdit,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = TextSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", style = MaterialTheme.typography.labelMedium)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Delete button
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = ColorError.copy(alpha = 0.8f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", style = MaterialTheme.typography.labelMedium)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Mark as paid button
                Button(
                    onClick = onMarkPaid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tintColor.copy(alpha = 0.2f),
                        contentColor = tintColor
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Paid", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditDebtDialog(
    debt: DebtEntity?,
    type: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Long, String) -> Unit
) {
    val isEdit = debt != null
    var name by rememberSaveable { mutableStateOf(debt?.name ?: "") }
    var amountText by rememberSaveable { mutableStateOf(debt?.amount?.toString() ?: "") }
    var selectedDate by rememberSaveable { mutableStateOf(debt?.dueDate ?: (System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var amountError by rememberSaveable { mutableStateOf<String?>(null) }

    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dialog title
                Text(
                    text = when {
                        isEdit -> "Edit Debt"
                        type == "PAYABLE" -> "Add Debt (I Owe)"
                        else -> "Add Receivable (Owed to Me)"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (type == "PAYABLE") "Record money you owe to someone" else "Record money someone owes you",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Person name field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Person / Entity Name") },
                    placeholder = { Text("e.g., John Doe") },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AccentGreen
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        errorBorderColor = ColorError,
                        focusedLabelColor = AccentGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = null
                    },
                    label = { Text("Amount") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it) } },
                    leadingIcon = {
                        Text(
                            text = "₱",
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentGreen,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        errorBorderColor = ColorError,
                        focusedLabelColor = AccentGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Due date field
                OutlinedTextField(
                    value = dateFormatter.format(Date(selectedDate)),
                    onValueChange = { },
                    label = { Text("Due Date") },
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = AccentGreen
                        )
                    },
                    trailingIcon = {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("Change")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = AccentGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = TextSecondary
                        )
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // Validate
                            var isValid = true
                            if (name.isBlank()) {
                                nameError = "Name is required"
                                isValid = false
                            }
                            val amount = amountText.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                amountError = "Enter a valid amount"
                                isValid = false
                            }
                            if (isValid && amount != null) {
                                onConfirm(name, amount, selectedDate, type)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen,
                            contentColor = DeepBackground
                        )
                    ) {
                        Text(if (isEdit) "Update" else "Save")
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = SurfaceElevated
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = AccentGreen,
                    selectedDayContentColor = DeepBackground,
                    todayDateBorderColor = AccentGreen,
                    todayContentColor = AccentGreen,
                    titleContentColor = TextPrimary,
                    headlineContentColor = TextPrimary,
                    weekdayContentColor = TextSecondary
                )
            )
        }
    }
}

@Composable
private fun CelebrationAnimation(modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "celebration"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .size(120.dp),
        shape = RoundedCornerShape(20.dp),
        color = SuccessContainer,
        shadowElevation = 8.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = ColorSuccess,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Paid!",
                style = MaterialTheme.typography.titleMedium,
                color = ColorSuccess,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Helper extension to track scroll direction
@Composable
private fun androidx.compose.foundation.lazy.LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

// Helper functions
private fun calculateDaysRemaining(dueDate: Long): Int {
    val currentTime = System.currentTimeMillis()
    val diff = dueDate - currentTime
    return TimeUnit.MILLISECONDS.toDays(diff).toInt()
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatCurrency(amount: Double): String {
    return "₱${String.format("%,.2f", amount)}"
}
