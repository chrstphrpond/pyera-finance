package com.pyera.app.ui.debt

import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

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
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.pyera.app.data.local.entity.DebtEntity
import com.pyera.app.ui.components.ConfirmationDialog
import com.pyera.app.ui.components.EmptyDebt
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.ErrorContainer
import com.pyera.app.ui.theme.SuccessContainer
import com.pyera.app.ui.util.CurrencyFormatter
import com.pyera.app.ui.util.pyeraBackground
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DebtScreen(
    viewModel: DebtViewModel = hiltViewModel()
) {
    val debts by viewModel.debts.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(0) } // 0: I Owe, 1: Owed to Me
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var debtToEditId by rememberSaveable { mutableStateOf<Int?>(null) }
    val debtToEdit = debtToEditId?.let { id -> debts.find { it.id == id } }
    var debtToDeleteId by rememberSaveable { mutableStateOf<Int?>(null) }
    val debtToDelete = debtToDeleteId?.let { id -> debts.find { it.id == id } }
    var debtToMarkPaidId by rememberSaveable { mutableStateOf<Int?>(null) }
    val debtToMarkPaid = debtToMarkPaidId?.let { id -> debts.find { it.id == id } }
    var showCelebration by rememberSaveable { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    )

    // Calculate summary values
    val totals by remember(debts) {
        derivedStateOf {
            val totalYouOwe = debts.filter { it.type == "PAYABLE" && !it.isPaid }.sumOf { it.amount }
            val totalOwedToYou = debts.filter { it.type == "RECEIVABLE" && !it.isPaid }.sumOf { it.amount }
            DebtTotals(
                totalYouOwe = totalYouOwe,
                totalOwedToYou = totalOwedToYou,
                netPosition = totalOwedToYou - totalYouOwe
            )
        }
    }

    val filteredDebts by remember(debts, selectedTab) {
        derivedStateOf {
            debts.filter {
                if (selectedTab == 0) it.type == "PAYABLE" else it.type == "RECEIVABLE"
            }.filter { !it.isPaid }
        }
    }

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
                            tint = ColorTokens.Primary500,
                            modifier = Modifier.size(SpacingTokens.ExtraLarge)
                        )
                        Text(
                            text = "Debt Manager",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
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
                    containerColor = ColorTokens.Primary500,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Debt")
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pyeraBackground()
                .padding(innerPadding)
        ) {
            // Summary Card
            DebtSummaryCard(
                totalYouOwe = totals.totalYouOwe,
                totalOwedToYou = totals.totalOwedToYou,
                netPosition = totals.netPosition,
                modifier = Modifier.padding(horizontal = SpacingTokens.Medium, vertical = 8.dp)
            )

            // Tab Row with badges
            DebtTabRow(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                iOweCount = debts.count { it.type == "PAYABLE" && !it.isPaid },
                owedToMeCount = debts.count { it.type == "RECEIVABLE" && !it.isPaid },
                modifier = Modifier.padding(horizontal = SpacingTokens.Medium)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Debt List with Pull-to-Refresh
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                if (filteredDebts.isEmpty()) {
                    EmptyDebt(
                        isIOwe = selectedTab == 0,
                        onAddClick = { showAddDialog = true }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = SpacingTokens.Medium, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = filteredDebts,
                            key = { it.id }
                        ) { debt ->
                            DebtItem(
                                debt = debt,
                                onMarkPaid = { debtToMarkPaidId = debt.id },
                                onDelete = { debtToDeleteId = debt.id },
                                onEdit = { debtToEditId = debt.id }
                            )
                        }
                        // Bottom spacer for FAB
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = ColorTokens.SurfaceLevel2,
                    contentColor = ColorTokens.Primary500
                )

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

@Composable
private fun DebtSummaryCard(
    totalYouOwe: Double,
    totalOwedToYou: Double,
    netPosition: Double,
    modifier: Modifier = Modifier
) {
    PyeraCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = ColorTokens.SurfaceLevel2
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.Medium)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    color = ColorTokens.Error500,
                    modifier = Modifier.weight(1f)
                )

                // Total Owed to You
                SummaryItem(
                    title = "Owed to You",
                    amount = totalOwedToYou,
                    icon = Icons.Outlined.AttachMoney,
                    color = ColorTokens.Success500,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            .background(if (isPositive) ColorTokens.Success500 else ColorTokens.Error500)
                    )
                    Text(
                        text = formatCurrency(netPosition),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isPositive) ColorTokens.Success500 else ColorTokens.Error500,
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
                modifier = Modifier.size(SpacingTokens.Medium)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        Triple("I Owe", ColorTokens.Error500, iOweCount),
        Triple("Owed to Me", ColorTokens.Success500, owedToMeCount)
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ColorTokens.SurfaceLevel2
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
                                .background(if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        )

                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )

                        // Count badge
                        if (count > 0) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.defaultMinSize(minWidth = 20.dp)
                            ) {
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
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
    val tintColor = if (isPayable) ColorTokens.Error500 else ColorTokens.Success500
    val containerColor = if (isPayable) ErrorContainer else SuccessContainer

    val daysRemaining = calculateDaysRemaining(debt.dueDate)
    val isOverdue = daysRemaining < 0
    val isDueSoon = daysRemaining in 0..3

    val statusColor = when {
        isOverdue -> ColorTokens.Error500
        isDueSoon -> ColorTokens.Warning500
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
            modifier = Modifier.padding(SpacingTokens.Medium)
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
                        color = MaterialTheme.colorScheme.onBackground,
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formatDate(debt.dueDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                        contentColor = ColorTokens.Error500.copy(alpha = 0.8f)
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
                // Dialog title
                Text(
                    text = when {
                        isEdit -> "Edit Debt"
                        type == "PAYABLE" -> "Add Debt (I Owe)"
                        else -> "Add Receivable (Owed to Me)"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (type == "PAYABLE") "Record money you owe to someone" else "Record money someone owes you",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Large))

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
                            tint = ColorTokens.Primary500
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = ColorTokens.Primary500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        errorBorderColor = ColorTokens.Error500,
                        focusedLabelColor = ColorTokens.Primary500
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

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
                            text = CurrencyFormatter.SYMBOL,
                            style = MaterialTheme.typography.titleMedium,
                            color = ColorTokens.Primary500,
                            modifier = Modifier.padding(start = SpacingTokens.Medium)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = ColorTokens.Primary500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        errorBorderColor = ColorTokens.Error500,
                        focusedLabelColor = ColorTokens.Primary500
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

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
                            tint = ColorTokens.Primary500
                        )
                    },
                    trailingIcon = {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("Change")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = ColorTokens.Primary500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = ColorTokens.Primary500
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Large))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                            containerColor = ColorTokens.Primary500,
                            contentColor = MaterialTheme.colorScheme.onPrimary
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
                containerColor = ColorTokens.SurfaceLevel2
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = ColorTokens.Primary500,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    todayDateBorderColor = ColorTokens.Primary500,
                    todayContentColor = ColorTokens.Primary500,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    headlineContentColor = MaterialTheme.colorScheme.onBackground,
                    weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                tint = ColorTokens.Success500,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Paid!",
                style = MaterialTheme.typography.titleMedium,
                color = ColorTokens.Success500,
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
    return CurrencyFormatter.format(amount)
}

private data class DebtTotals(
    val totalYouOwe: Double,
    val totalOwedToYou: Double,
    val netPosition: Double
)




