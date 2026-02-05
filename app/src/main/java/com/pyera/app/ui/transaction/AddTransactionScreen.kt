package com.pyera.app.ui.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.ui.theme.AccentGreen
import com.pyera.app.ui.theme.DeepBackground
import com.pyera.app.ui.theme.SurfaceElevated
import com.pyera.app.ui.theme.TextPrimary
import com.pyera.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Form state
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("EXPENSE") } // "INCOME" or "EXPENSE"
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var isScanning by remember { mutableStateOf(false) }
    var scannedReceiptUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isAmountError by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }

    // Navigate back when transaction is successfully saved
    if (saveSuccess) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }

    val scanLauncher = rememberReceiptPicker { uri ->
        isScanning = true
        scannedReceiptUri = uri
        scope.launch {
            val data = viewModel.processReceipt(uri)
            if (data.totalAmount != null) {
                amount = data.totalAmount.toString()
            }
            if (!data.merchant.isNullOrEmpty()) {
                note = "Receipt from ${data.merchant}"
            }
            isScanning = false
            android.widget.Toast.makeText(context, "Receipt Scanned!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    val onScanClick = {
        scanLauncher()
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onScanClick) {
                        Icon(
                            Icons.Default.PhotoCamera, 
                            contentDescription = "Scan Receipt", 
                            tint = AccentGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBackground,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
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
                // Scanned Receipt Preview
                AnimatedVisibility(
                    visible = scannedReceiptUri != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    scannedReceiptUri?.let { uri ->
                        ReceiptPreviewCard(
                            uri = uri,
                            onClear = { scannedReceiptUri = null }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Type Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TypeButton(
                        text = "Expense",
                        isSelected = selectedType == "EXPENSE",
                        onClick = { selectedType = "EXPENSE"; selectedCategory = null }
                    )
                    TypeButton(
                        text = "Income",
                        isSelected = selectedType == "INCOME",
                        onClick = { selectedType = "INCOME"; selectedCategory = null }
                    )
                }

                // Amount Input with Currency Symbol
                AmountInputField(
                    amount = amount,
                    onAmountChange = { 
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            amount = it
                            isAmountError = false
                        }
                    },
                    isError = isAmountError
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker Field
                DatePickerField(
                    date = selectedDate,
                    onClick = { showDatePicker = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Note Input with Character Counter
                NoteInputField(
                    note = note,
                    onNoteChange = { note = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Category Section
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium, 
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Category Grid (horizontal scrolling for larger touch targets)
                val filteredCategories = state.categories.filter { it.type == selectedType }
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(
                    items = filteredCategories,
                    key = { category: CategoryEntity -> category.id }
                ) { category: CategoryEntity ->
                        CategoryItem(
                            category = category,
                            isSelected = selectedCategory?.id == category.id,
                            onClick = { selectedCategory = category }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Sticky Bottom Actions
            Surface(
                color = DeepBackground,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Save Button
                    Button(
                        onClick = {
                            val amountVal = amount.toDoubleOrNull()
                            if (amountVal != null && amountVal > 0 && selectedCategory != null) {
                                viewModel.addTransaction(
                                    TransactionEntity(
                                        amount = amountVal,
                                        note = note,
                                        date = selectedDate,
                                        type = selectedType,
                                        categoryId = selectedCategory?.id
                                    )
                                )
                                saveSuccess = true
                            } else {
                                if (amountVal == null || amountVal <= 0) {
                                    isAmountError = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        enabled = selectedCategory != null && amount.isNotBlank()
                    ) {
                        Text("Save Transaction", color = DeepBackground, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Save & Add Another Button
                    OutlinedButton(
                        onClick = {
                            val amountVal = amount.toDoubleOrNull()
                            if (amountVal != null && amountVal > 0 && selectedCategory != null) {
                                viewModel.addTransaction(
                                    TransactionEntity(
                                        amount = amountVal,
                                        note = note,
                                        date = selectedDate,
                                        type = selectedType,
                                        categoryId = selectedCategory?.id
                                    )
                                )
                                // Reset form
                                amount = ""
                                note = ""
                                selectedCategory = null
                                selectedDate = System.currentTimeMillis()
                                scannedReceiptUri = null
                                android.widget.Toast.makeText(
                                    context, 
                                    "Transaction saved! Add another.", 
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                if (amountVal == null || amountVal <= 0) {
                                    isAmountError = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = selectedCategory != null && amount.isNotBlank(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AccentGreen
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (selectedCategory != null && amount.isNotBlank()) AccentGreen else TextSecondary
                            )
                        )
                    ) {
                        Text("Save & Add Another", fontSize = 16.sp)
                    }
                }
            }
        }

        // Scanning indicator
        if (isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentGreen)
            }
        }
    }
}

@Composable
fun AmountInputField(
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
fun DatePickerField(
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
            Text(
                text = dateString,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Change",
                style = MaterialTheme.typography.bodySmall,
                color = AccentGreen
            )
        }
    }
}

@Composable
fun NoteInputField(
    note: String,
    onNoteChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = note,
            onValueChange = { if (it.length <= 200) onNoteChange(it) },
            label = { Text("Note (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentGreen,
                unfocusedBorderColor = TextSecondary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedLabelColor = AccentGreen,
                unfocusedLabelColor = TextSecondary
            ),
            maxLines = 3,
            minLines = 2
        )
        
        // Character counter
        Text(
            text = "${note.length}/200",
            style = MaterialTheme.typography.bodySmall,
            color = if (note.length >= 200) MaterialTheme.colorScheme.error else TextSecondary,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 4.dp, end = 8.dp)
        )
    }
}

@Composable
fun ReceiptPreviewCard(
    uri: android.net.Uri,
    onClear: () -> Unit
) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Receipt thumbnail placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = AccentGreen
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Receipt Scanned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Amount auto-filled",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear receipt",
                    tint = TextSecondary
                )
            }
        }
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
    
    var year by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var day by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

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
                    var monthExpanded by remember { mutableStateOf(false) }
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
                    var dayExpanded by remember { mutableStateOf(false) }
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
                    var yearExpanded by remember { mutableStateOf(false) }
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
                                val y = currentYear - index
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
                        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                        year = yesterday.get(Calendar.YEAR)
                        month = yesterday.get(Calendar.MONTH)
                        day = yesterday.get(Calendar.DAY_OF_MONTH)
                    }) {
                        Text("Yesterday", color = AccentGreen)
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
                        Text("OK", color = DeepBackground)
                    }
                }
            }
        }
    }
}

@Composable
fun TypeButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) AccentGreen else SurfaceElevated,
            contentColor = if (isSelected) DeepBackground else TextPrimary
        ),
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text)
    }
}

@Composable
fun CategoryItem(category: CategoryEntity, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) AccentGreen else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                if (isSelected) AccentGreen.copy(alpha = 0.1f) else SurfaceElevated,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(category.color))
        ) {
            Text(
                category.name.take(1).uppercase(), 
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) AccentGreen else TextPrimary,
            maxLines = 1,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
