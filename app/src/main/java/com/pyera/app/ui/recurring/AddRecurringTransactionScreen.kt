package com.pyera.app.ui.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.RecurringFrequency
import com.pyera.app.data.local.entity.TransactionType
import com.pyera.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringTransactionScreen(
    navController: NavController,
    viewModel: RecurringTransactionsViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    var showStartDatePicker by rememberSaveable { mutableStateOf(false) }
    var showEndDatePicker by rememberSaveable { mutableStateOf(false) }
    var isAmountError by rememberSaveable { mutableStateOf(false) }

    // Reset form state when screen is first shown
    LaunchedEffect(Unit) {
        viewModel.resetFormState()
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            selectedDate = formState.startDate,
            onDateSelected = { date ->
                viewModel.updateFormState { it.copy(startDate = date) }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker && formState.hasEndDate) {
        DatePickerDialog(
            selectedDate = formState.endDate ?: System.currentTimeMillis(),
            onDateSelected = { date ->
                viewModel.updateFormState { it.copy(endDate = date) }
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Recurring Transaction") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
        containerColor = DeepBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Type Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TypeButton(
                        text = "Expense",
                        isSelected = formState.type == TransactionType.EXPENSE,
                        onClick = { 
                            viewModel.updateFormState { it.copy(type = TransactionType.EXPENSE, categoryId = null) }
                        }
                    )
                    TypeButton(
                        text = "Income",
                        isSelected = formState.type == TransactionType.INCOME,
                        onClick = { 
                            viewModel.updateFormState { it.copy(type = TransactionType.INCOME, categoryId = null) }
                        }
                    )
                }

                // Amount Input
                RecurringAmountInputField(
                    amount = formState.amount,
                    onAmountChange = { value ->
                        if (value.all { char -> char.isDigit() || char == '.' }) {
                            viewModel.updateFormState { it.copy(amount = value) }
                            isAmountError = false
                        }
                    },
                    isError = isAmountError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description Input
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = { newValue -> viewModel.updateFormState { it.copy(description = newValue) } },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentGreen,
                        unfocusedLabelColor = TextSecondary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Frequency Selector
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FrequencySelector(
                    selectedFrequency = formState.frequency,
                    onFrequencySelected = { frequency ->
                        viewModel.updateFormState { it.copy(frequency = frequency) }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Start Date Picker
                DatePickerField(
                    label = "Start Date",
                    date = formState.startDate,
                    onClick = { showStartDatePicker = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // End Date Option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = formState.hasEndDate,
                        onCheckedChange = { checked ->
                            viewModel.updateFormState { 
                                it.copy(hasEndDate = checked, endDate = if (checked) System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000 else null) 
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AccentGreen,
                            uncheckedColor = TextSecondary
                        )
                    )
                    Text(
                        text = "Set end date",
                        color = TextPrimary,
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
                    DatePickerField(
                        label = "End Date",
                        date = formState.endDate ?: System.currentTimeMillis(),
                        onClick = { showEndDatePicker = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Category Selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                val filteredCategories = categories.filter { category ->
                    category.type == if (formState.type == TransactionType.INCOME) "INCOME" else "EXPENSE"
                }

                if (filteredCategories.isEmpty()) {
                    Text(
                        text = "No ${if (formState.type == TransactionType.INCOME) "income" else "expense"} categories yet. Add one to continue.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(
                            items = filteredCategories,
                            key = { category: CategoryEntity -> category.id }
                        ) { category: CategoryEntity ->
                            RecurringCategoryChip(
                                category = category,
                                isSelected = formState.categoryId == category.id.toLong(),
                                onClick = {
                                    viewModel.updateFormState { it.copy(categoryId = category.id.toLong()) }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Save Button
            Surface(
                color = DeepBackground,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val amountVal = formState.amount.toDoubleOrNull()
                        if (amountVal != null && amountVal > 0 && formState.description.isNotBlank()) {
                            viewModel.addRecurring(formState)
                            navController.popBackStack()
                        } else {
                            if (amountVal == null || amountVal <= 0) {
                                isAmountError = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    enabled = formState.amount.isNotBlank() &&
                        formState.description.isNotBlank() &&
                        formState.categoryId != null
                ) {
                    Text("Save Recurring Transaction", color = Color.Black, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun RecurringCategoryChip(
    category: CategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val background = if (isSelected) AccentGreen.copy(alpha = 0.2f) else SurfaceElevated
    val borderColor = if (isSelected) AccentGreen else ColorBorder
    val textColor = if (isSelected) AccentGreen else TextPrimary

    Column(
        modifier = Modifier
            .widthIn(min = 80.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(category.color).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.icon,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
fun RecurringAmountInputField(
    amount: String,
    onAmountChange: (String) -> Unit,
    isError: Boolean
) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "₱",
                style = TextStyle(
                    fontSize = 32.sp,
                    color = AccentGreen
                ),
                modifier = Modifier.padding(end = 12.dp)
            )

            androidx.compose.foundation.text.BasicTextField(
                value = amount,
                onValueChange = onAmountChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 32.sp,
                    color = TextPrimary,
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
                                    color = TextSecondary
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
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

@Composable
fun FrequencySelector(
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
                    selectedContainerColor = AccentGreen,
                    selectedLabelColor = Color.Black,
                    containerColor = SurfaceElevated,
                    labelColor = TextPrimary
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
fun DatePickerField(
    label: String,
    date: Long,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateString = dateFormat.format(Date(date))

    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = AccentGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
            Text(
                text = "Change",
                style = MaterialTheme.typography.bodySmall,
                color = AccentGreen
            )
        }
    }
}

@Composable
fun TypeButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) AccentGreen else SurfaceElevated,
            contentColor = if (isSelected) Color.Black else TextPrimary
        ),
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text)
    }
}

@Composable
fun DatePickerDialog(
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
            shape = RoundedCornerShape(16.dp),
            color = SurfaceElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Date",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
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

                Spacer(modifier = Modifier.height(16.dp))

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
                        Text("Today", color = AccentGreen)
                    }
                    TextButton(onClick = {
                        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                        year = tomorrow.get(Calendar.YEAR)
                        month = tomorrow.get(Calendar.MONTH)
                        day = tomorrow.get(Calendar.DAY_OF_MONTH)
                    }) {
                        Text("Tomorrow", color = AccentGreen)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newCalendar = Calendar.getInstance()
                            newCalendar.set(year, month, day, 0, 0, 0)
                            newCalendar.set(Calendar.MILLISECOND, 0)
                            onDateSelected(newCalendar.timeInMillis)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                    ) {
                        Text("OK", color = Color.Black)
                    }
                }
            }
        }
    }
}
