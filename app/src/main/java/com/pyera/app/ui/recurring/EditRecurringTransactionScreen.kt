package com.pyera.app.ui.recurring

import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pyera.app.data.local.entity.RecurringFrequency
import com.pyera.app.data.local.entity.TransactionType
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.ui.transaction.AccountSelector
import com.pyera.app.ui.theme.*
import com.pyera.app.ui.util.CurrencyFormatter
import com.pyera.app.ui.util.pyeraBackground
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecurringTransactionScreen(
    navController: NavController,
    recurringId: Long,
    viewModel: RecurringTransactionsViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    var showStartDatePicker by rememberSaveable { mutableStateOf(false) }
    var showEndDatePicker by rememberSaveable { mutableStateOf(false) }
    var isAmountError by rememberSaveable { mutableStateOf(false) }
    var isAccountError by rememberSaveable { mutableStateOf(false) }
    var showApplyOptions by rememberSaveable { mutableStateOf(false) }

    // Load the recurring transaction when the screen is shown
    LaunchedEffect(recurringId) {
        viewModel.loadRecurringForEdit(recurringId)
    }

    LaunchedEffect(accounts, formState.accountId) {
        if (formState.accountId == null) {
            val defaultAccount = accounts.firstOrNull { it.isDefault } ?: accounts.firstOrNull()
            if (defaultAccount != null) {
                viewModel.updateFormState { it.copy(accountId = defaultAccount.id) }
            }
        }
    }

    if (showStartDatePicker) {
        DatePickerDialogEdit(
            selectedDate = formState.startDate,
            onDateSelected = { date ->
                viewModel.updateFormState { it.copy(startDate = date) }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker && formState.hasEndDate) {
        DatePickerDialogEdit(
            selectedDate = formState.endDate ?: System.currentTimeMillis(),
            onDateSelected = { date ->
                viewModel.updateFormState { it.copy(endDate = date) }
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    if (showApplyOptions) {
        ApplyChangesDialog(
            onApplyToFuture = {
                viewModel.updateRecurring(recurringId, formState)
                showApplyOptions = false
                navController.popBackStack()
            },
            onApplyToAll = {
                // In a full implementation, this would also update past generated transactions
                viewModel.updateRecurring(recurringId, formState)
                showApplyOptions = false
                navController.popBackStack()
            },
            onDismiss = { showApplyOptions = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Recurring Transaction") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pyeraBackground()
                .padding(padding)
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(SpacingTokens.Medium)
            ) {
                // Type Selector (read-only in edit mode - changing type would affect past transactions)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = SpacingTokens.Medium),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TypeButtonEdit(
                        text = "Expense",
                        isSelected = formState.type == TransactionType.EXPENSE,
                        onClick = { } // Read-only
                    )
                    TypeButtonEdit(
                        text = "Income",
                        isSelected = formState.type == TransactionType.INCOME,
                        onClick = { } // Read-only
                    )
                }

                // Info text about type being read-only
                Text(
                    text = "Transaction type cannot be changed after creation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier.padding(bottom = SpacingTokens.Medium)
                )

                // Amount Input
                EditAmountInputField(
                    amount = formState.amount,
                    onAmountChange = { value ->
                        if (value.all { char -> char.isDigit() || char == '.' }) {
                            viewModel.updateFormState { it.copy(amount = value) }
                            isAmountError = false
                        }
                    },
                    isError = isAmountError
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                // Description Input
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = { newValue -> viewModel.updateFormState { it.copy(description = newValue) } },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorTokens.Primary500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedLabelColor = ColorTokens.Primary500,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                // Account Selection
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (accounts.isEmpty()) {
                    Text(
                        text = "No accounts available. Add an account to continue.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val selectedAccount = accounts.firstOrNull { it.id == formState.accountId }
                    AccountSelector(
                        account = selectedAccount,
                        accounts = accounts,
                        onAccountSelected = { account: AccountEntity ->
                            viewModel.updateFormState { it.copy(accountId = account.id) }
                            isAccountError = false
                        },
                        isError = isAccountError && formState.accountId == null
                    )
                }

                if (isAccountError && formState.accountId == null) {
                    Text(
                        text = "Please select an account",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                // Frequency Selector
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FrequencySelectorEdit(
                    selectedFrequency = formState.frequency,
                    onFrequencySelected = { frequency ->
                        viewModel.updateFormState { it.copy(frequency = frequency) }
                    }
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                // Start Date Picker
                DatePickerFieldEdit(
                    label = "Start Date",
                    date = formState.startDate,
                    onClick = { showStartDatePicker = true }
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                // End Date Option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = formState.hasEndDate,
                        onCheckedChange = { checked ->
                            viewModel.updateFormState {
                                it.copy(
                                    hasEndDate = checked,
                                    endDate = if (checked) System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000 else null
                                )
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ColorTokens.Primary500,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "Set end date",
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.clickable {
                            viewModel.updateFormState {
                                it.copy(
                                    hasEndDate = !it.hasEndDate,
                                    endDate = if (!it.hasEndDate) System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000 else null
                                )
                            }
                        }
                    )
                }

                if (formState.hasEndDate) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DatePickerFieldEdit(
                        label = "End Date",
                        date = formState.endDate ?: System.currentTimeMillis(),
                        onClick = { showEndDatePicker = true }
                    )
                }

                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                // Active Status Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Active Status",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (formState.isActive) "Currently active" else "Currently paused",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = formState.isActive,
                        onCheckedChange = { checked ->
                            viewModel.updateFormState { it.copy(isActive = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ColorTokens.Primary500,
                            checkedTrackColor = ColorTokens.Primary500.copy(alpha = 0.5f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(SpacingTokens.Large))
            }

            // Save Button
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val amountVal = formState.amount.toDoubleOrNull()
                        if (formState.accountId == null) {
                            isAccountError = true
                            return@Button
                        }
                        if (amountVal != null && amountVal > 0 && formState.description.isNotBlank()) {
                            // Show apply options dialog
                            showApplyOptions = true
                        } else {
                            if (amountVal == null || amountVal <= 0) {
                                isAmountError = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.Medium)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Primary500),
                    enabled = formState.amount.isNotBlank() &&
                        formState.description.isNotBlank() &&
                        formState.accountId != null
                ) {
                    Text("Save Changes", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ApplyChangesDialog(
    onApplyToFuture: () -> Unit,
    onApplyToAll: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(SpacingTokens.Medium),
            color = ColorTokens.SurfaceLevel2,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(SpacingTokens.Large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Apply Changes",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                Text(
                    text = "How would you like to apply these changes?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Large))

                // Apply to future only
                Button(
                    onClick = onApplyToFuture,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Primary500)
                ) {
                    Text("Apply to Future Only", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Apply to all
                OutlinedButton(
                    onClick = onApplyToAll,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Text("Apply to All (Including Past)")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cancel
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun EditAmountInputField(
    amount: String,
    onAmountChange: (String) -> Unit,
    isError: Boolean
) {
    Surface(
        color = ColorTokens.SurfaceLevel2,
        shape = RoundedCornerShape(SpacingTokens.Medium),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = SpacingTokens.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = CurrencyFormatter.SYMBOL,
                style = TextStyle(
                    fontSize = 32.sp,
                    color = ColorTokens.Primary500
                ),
                modifier = Modifier.padding(end = 12.dp)
            )

            androidx.compose.foundation.text.BasicTextField(
                value = amount,
                onValueChange = onAmountChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                decorationBox = { innerTextField ->
                    Box {
                        if (amount.isEmpty()) {
                            Text(
                                text = "0.00",
                                style = TextStyle(
                                    fontSize = 32.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }

    if (isError) {
        Text(
            text = "Please enter a valid amount greater than 0",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = SpacingTokens.Medium, top = 4.dp)
        )
    }
}

@Composable
fun FrequencySelectorEdit(
    selectedFrequency: RecurringFrequency,
    onFrequencySelected: (RecurringFrequency) -> Unit
) {
    val frequencies = RecurringFrequency.values()

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(frequencies) { frequency ->
            val isSelected = frequency == selectedFrequency
            val label = when (frequency) {
                RecurringFrequency.DAILY -> "Daily"
                RecurringFrequency.WEEKLY -> "Weekly"
                RecurringFrequency.BIWEEKLY -> "Bi-weekly"
                RecurringFrequency.MONTHLY -> "Monthly"
                RecurringFrequency.QUARTERLY -> "Quarterly"
                RecurringFrequency.YEARLY -> "Yearly"
            }

            FilterChip(
                selected = isSelected,
                onClick = { onFrequencySelected(frequency) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ColorTokens.Primary500,
                    selectedLabelColor = Color.Black,
                    containerColor = ColorTokens.SurfaceLevel2,
                    labelColor = MaterialTheme.colorScheme.onBackground
                ),
                border = if (isSelected) null else FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = false,
                    borderColor = ColorBorder
                )
            )
        }
    }
}

@Composable
fun DatePickerFieldEdit(
    label: String,
    date: Long,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateString = dateFormat.format(Date(date))

    Surface(
        color = ColorTokens.SurfaceLevel2,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.Medium, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = ColorTokens.Primary500,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Change",
                style = MaterialTheme.typography.bodySmall,
                color = ColorTokens.Primary500
            )
        }
    }
}

@Composable
fun TypeButtonEdit(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = false, // Read-only in edit mode
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = if (isSelected) ColorTokens.Primary500 else ColorTokens.SurfaceLevel2,
            disabledContentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text)
    }
}

@Composable
fun DatePickerDialogEdit(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = selectedDate

    var year by rememberSaveable { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var month by rememberSaveable { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var day by rememberSaveable { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(SpacingTokens.Medium),
            color = ColorTokens.SurfaceLevel2,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(SpacingTokens.Medium)) {
                Text(
                    text = "Select Date",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = SpacingTokens.Medium)
                )

                // Simple date picker using dropdowns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Month picker
                    var monthExpanded by rememberSaveable { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { monthExpanded = true }) {
                            Text(Calendar.getInstance().apply { set(Calendar.MONTH, month) }
                                .getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: "")
                        }
                        DropdownMenu(
                            expanded = monthExpanded,
                            onDismissRequest = { monthExpanded = false }
                        ) {
                            repeat(12) { index ->
                                DropdownMenuItem(
                                    text = {
                                        Text(Calendar.getInstance().apply { set(Calendar.MONTH, index) }
                                            .getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: "")
                                    },
                                    onClick = {
                                        month = index
                                        monthExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Day picker
                    var dayExpanded by rememberSaveable { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { dayExpanded = true }) {
                            Text(day.toString())
                        }
                        DropdownMenu(
                            expanded = dayExpanded,
                            onDismissRequest = { dayExpanded = false }
                        ) {
                            val maxDay = Calendar.getInstance().apply {
                                set(year, month, 1)
                            }.getActualMaximum(Calendar.DAY_OF_MONTH)

                            repeat(maxDay) { index ->
                                DropdownMenuItem(
                                    text = { Text((index + 1).toString()) },
                                    onClick = {
                                        day = index + 1
                                        dayExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Year picker
                    var yearExpanded by rememberSaveable { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { yearExpanded = true }) {
                            Text(year.toString())
                        }
                        DropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false }
                        ) {
                            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                            repeat(10) { index ->
                                val y = currentYear - 1 + index
                                DropdownMenuItem(
                                    text = { Text(y.toString()) },
                                    onClick = {
                                        year = y
                                        yearExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                // Quick date buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = {
                        val today = Calendar.getInstance()
                        year = today.get(Calendar.YEAR)
                        month = today.get(Calendar.MONTH)
                        day = today.get(Calendar.DAY_OF_MONTH)
                    }) {
                        Text("Today", color = ColorTokens.Primary500)
                    }
                    TextButton(onClick = {
                        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                        year = tomorrow.get(Calendar.YEAR)
                        month = tomorrow.get(Calendar.MONTH)
                        day = tomorrow.get(Calendar.DAY_OF_MONTH)
                    }) {
                        Text("Tomorrow", color = ColorTokens.Primary500)
                    }
                }

                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newCalendar = Calendar.getInstance()
                            newCalendar.set(year, month, day, 0, 0, 0)
                            newCalendar.set(Calendar.MILLISECOND, 0)
                            onDateSelected(newCalendar.timeInMillis)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Primary500)
                    ) {
                        Text("OK", color = Color.Black)
                    }
                }
            }
        }
    }
}



