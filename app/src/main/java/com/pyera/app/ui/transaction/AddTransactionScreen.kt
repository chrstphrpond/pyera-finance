package com.pyera.app.ui.transaction

import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.data.local.entity.formattedBalance
import com.pyera.app.ui.dashboard.TemplateSelectorRow
import com.pyera.app.ui.navigation.Screen
import com.pyera.app.ui.templates.TemplatesViewModel
import com.pyera.app.ui.util.CurrencyFormatter
import com.pyera.app.ui.util.pyeraBackground
import com.pyera.app.util.ValidationUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val templatesViewModel: TemplatesViewModel = hiltViewModel()
    val templatesState by templatesViewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Form state - using rememberSaveable to survive configuration changes
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf("EXPENSE") } // "INCOME" or "EXPENSE"
    var selectedCategoryId by rememberSaveable { mutableStateOf<Int?>(null) }
    val selectedCategory = selectedCategoryId?.let { id -> state.categories.find { it.id == id } }
    var selectedAccountId by rememberSaveable { mutableStateOf<Long?>(null) }
    val selectedAccount = selectedAccountId?.let { id -> state.accounts.find { it.id == id } } ?: state.defaultAccount
    var selectedDate by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var isScanning by rememberSaveable { mutableStateOf(false) }
    var scannedReceiptUriString by rememberSaveable { mutableStateOf<String?>(null) }
    val scannedReceiptUri = scannedReceiptUriString?.let { android.net.Uri.parse(it) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var isAmountError by rememberSaveable { mutableStateOf(false) }
    var isAccountError by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var saveSuccess by rememberSaveable { mutableStateOf(false) }
    var showSaveAsRule by rememberSaveable { mutableStateOf(false) }
    var ruleCreated by rememberSaveable { mutableStateOf(false) }
    var showSaveAsTemplateDialog by rememberSaveable { mutableStateOf(false) }
    var lastSavedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    
    // Update selected account when default account changes
    LaunchedEffect(state.defaultAccount) {
        if (selectedAccountId == null) {
            state.defaultAccount?.let { defaultAccount ->
                selectedAccountId = defaultAccount.id
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(SpacingTokens.Medium)
            ) {
                NaturalLanguageTransactionInput(
                    onParsed = { parsed ->
                        parsed.amount?.let { amount = String.format(Locale.getDefault(), "%.2f", it) }
                        note = parsed.description
                        selectedType = parsed.type
                        parsed.categoryId?.let { selectedCategoryId = it.toInt() }
                        parsed.date?.let { selectedDate = it }
                        selectedTab = 0
                        scope.launch {
                            snackbarHostState.showSnackbar("Parsed. Review and save the transaction.")
                        }
                    },
                    onError = { error ->
                        scope.launch { snackbarHostState.showSnackbar(error) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Show save as template dialog after successful save
    LaunchedEffect(saveSuccess, lastSavedTransaction) {
        if (saveSuccess && lastSavedTransaction != null) {
            showSaveAsTemplateDialog = true
            saveSuccess = false // Reset to prevent re-triggering
        }
    }

    // Save as Template Dialog
    if (showSaveAsTemplateDialog && lastSavedTransaction != null) {
        SaveAsTemplateDialog(
            onDismiss = {
                showSaveAsTemplateDialog = false
                lastSavedTransaction = null
                navController.popBackStack()
            },
            onSave = { templateName ->
                templatesViewModel.createTemplateFromTransaction(
                    lastSavedTransaction!!,
                    templateName
                )
                showSaveAsTemplateDialog = false
                lastSavedTransaction = null
                navController.popBackStack()
            }
        )
    }

    val scanLauncher = rememberReceiptPicker { uri ->
        isScanning = true
        scannedReceiptUriString = uri.toString()
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
                            tint = ColorTokens.Primary500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pyeraBackground()
                .padding(padding)
        ) {
            val tabs = listOf("Form", "Natural Language")
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (selectedTab == 0) {
                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(SpacingTokens.Medium)
                ) {
                // Scanned Receipt Preview
                AnimatedVisibility(
                    visible = scannedReceiptUri != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    scannedReceiptUri?.let {
                        ReceiptPreviewCard(
                            onClear = { scannedReceiptUriString = null }
                        )
                        Spacer(modifier = Modifier.height(SpacingTokens.Medium))
                    }
                }

                // Type Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = SpacingTokens.Medium),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TypeButton(
                        text = "Expense",
                        isSelected = selectedType == "EXPENSE",
                        onClick = {
                            selectedType = "EXPENSE"
                            selectedCategoryId = null
                            showSaveAsRule = false
                            ruleCreated = false
                        }
                    )
                    TypeButton(
                        text = "Income",
                        isSelected = selectedType == "INCOME",
                        onClick = {
                            selectedType = "INCOME"
                            selectedCategoryId = null
                            showSaveAsRule = false
                            ruleCreated = false
                        }
                    )
                }

                // Template Selector - shows matching templates for quick entry
                TemplateSelectorRow(
                    templates = templatesState.templates,
                    selectedType = selectedType,
                    onTemplateSelected = { template ->
                        // Pre-fill fields from template
                        template.amount?.let { amount = it.toString() }
                        note = template.description
                        selectedType = template.type
                        template.categoryId?.let { catId ->
                            selectedCategoryId = catId
                        }
                        template.accountId?.let { accId ->
                            selectedAccountId = accId
                        }
                        // Mark template as used
                        templatesViewModel.useTemplate(template.id)
                    }
                )
                
                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

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
                
                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                // Date Picker Field
                DatePickerField(
                    date = selectedDate,
                    onClick = { showDatePicker = true }
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                // Note Input with Character Counter
                NoteInputField(
                    note = note,
                    onNoteChange = { note = it }
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Large))

                // Account Section
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium, 
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Account Selector
                AccountSelector(
                    account = selectedAccount,
                    accounts = state.accounts,
                    onAccountSelected = { account ->
                        selectedAccountId = account.id
                        isAccountError = false
                    },
                    isError = isAccountError
                )
                
                Spacer(modifier = Modifier.height(SpacingTokens.Large))
                
                // Category Section
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium, 
                    color = MaterialTheme.colorScheme.onBackground
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
                            onClick = { 
                                selectedCategoryId = category.id
                                showSaveAsRule = note.isNotBlank()
                            }
                        )
                    }
                }

                // Save as Rule option (shown when category is manually selected and note exists)
                AnimatedVisibility(
                    visible = showSaveAsRule && selectedCategory != null && note.isNotBlank() && !ruleCreated,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(SpacingTokens.Medium))
                        
                        PyeraCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 12.dp,
                            containerColor = ColorTokens.SurfaceLevel1,
                            borderWidth = 0.dp,
                            onClick = {
                                scope.launch {
                                    selectedCategory?.let { category ->
                                        val result = viewModel.createRuleFromTransaction(
                                            description = note,
                                            categoryId = category.id
                                        )
                                        result.fold(
                                            onSuccess = {
                                                ruleCreated = true
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Rule saved! Future transactions matching \"$note\" will be categorized as ${category.name}",
                                                    android.widget.Toast.LENGTH_LONG
                                                ).show()
                                            },
                                            onFailure = { e ->
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "Failed to save rule: ${e.message}",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    }
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(SpacingTokens.Medium),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    tint = ColorTokens.Primary500,
                                    modifier = Modifier.size(SpacingTokens.Large)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Save as Rule",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ColorTokens.Primary500,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Auto-categorize future transactions like this",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SpacingTokens.Large))
            }

            // Sticky Bottom Actions
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.Medium)
                ) {
                    // Save Button
                    Button(
                        onClick = {
                            val amountVal = amount.toDoubleOrNull()
                            val accountId = selectedAccount?.id
                            val descriptionResult = ValidationUtils.validateTransactionDescription(note)
                            val categoryResult = ValidationUtils.validateTransactionCategory(selectedCategory?.id?.toLong())
                            val amountResult = ValidationUtils.validateTransactionAmount(amount)
                            
                            when {
                                amountResult is ValidationUtils.ValidationResult.Error -> {
                                    isAmountError = true
                                    android.widget.Toast
                                        .makeText(context, amountResult.message, android.widget.Toast.LENGTH_SHORT)
                                        .show()
                                }
                                accountId == null -> {
                                    isAccountError = true
                                    android.widget.Toast.makeText(context, "Please select an account", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                categoryResult is ValidationUtils.ValidationResult.Error -> {
                                    android.widget.Toast.makeText(context, categoryResult.message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                                descriptionResult is ValidationUtils.ValidationResult.Error -> {
                                    android.widget.Toast.makeText(context, descriptionResult.message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    val transaction = TransactionEntity(
                                        amount = amountVal,
                                        note = note,
                                        date = selectedDate,
                                        type = selectedType,
                                        categoryId = selectedCategory.id,
                                        accountId = accountId,
                                        userId = state.accounts.find { it.id == accountId }?.userId ?: ""
                                    )
                                    viewModel.addTransaction(transaction)
                                    lastSavedTransaction = transaction
                                    saveSuccess = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Primary500),
                        enabled = selectedCategory != null && amount.isNotBlank() && selectedAccount != null
                    ) {
                        Text("Save Transaction", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Save & Add Another Button
                    val isFormValid = selectedCategory != null && amount.isNotBlank() && selectedAccount != null

                    OutlinedButton(
                        onClick = {
                            val amountVal = amount.toDoubleOrNull()
                            val accountId = selectedAccount?.id
                            val descriptionResult = ValidationUtils.validateTransactionDescription(note)
                            val categoryResult = ValidationUtils.validateTransactionCategory(selectedCategory?.id?.toLong())
                            val amountResult = ValidationUtils.validateTransactionAmount(amount)
                            
                            when {
                                amountResult is ValidationUtils.ValidationResult.Error -> {
                                    isAmountError = true
                                    android.widget.Toast
                                        .makeText(context, amountResult.message, android.widget.Toast.LENGTH_SHORT)
                                        .show()
                                }
                                accountId == null -> {
                                    isAccountError = true
                                    android.widget.Toast.makeText(context, "Please select an account", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                categoryResult is ValidationUtils.ValidationResult.Error -> {
                                    android.widget.Toast.makeText(context, categoryResult.message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                                descriptionResult is ValidationUtils.ValidationResult.Error -> {
                                    android.widget.Toast.makeText(context, descriptionResult.message, android.widget.Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    viewModel.addTransaction(
                                        TransactionEntity(
                                            amount = amountVal,
                                            note = note,
                                            date = selectedDate,
                                            type = selectedType,
                                            categoryId = selectedCategory.id,
                                            accountId = accountId,
                                            userId = state.accounts.find { it.id == accountId }?.userId ?: ""
                                        )
                                    )
                                    // Reset form
                                    amount = ""
                                    note = ""
                                    selectedCategoryId = null
                                    selectedDate = System.currentTimeMillis()
                                    scannedReceiptUriString = null
                                    android.widget.Toast.makeText(
                                        context, 
                                        "Transaction saved! Add another.", 
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = isFormValid,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ColorTokens.Primary500
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isFormValid) ColorTokens.Primary500 else MaterialTheme.colorScheme.onSurfaceVariant
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
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorTokens.Primary500)
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
fun DatePickerField(
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
            Text(
                text = dateString,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Change",
                style = MaterialTheme.typography.bodySmall,
                color = ColorTokens.Primary500
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
            onValueChange = { if (it.length <= 500) onNoteChange(it) },
            label = { Text("Note (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorTokens.Primary500,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedLabelColor = ColorTokens.Primary500,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = 3,
            minLines = 2
        )
        
        // Character counter
        Text(
            text = "${note.length}/500",
            style = MaterialTheme.typography.bodySmall,
            color = if (note.length >= 500) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 4.dp, end = 8.dp)
        )
    }
}

@Composable
fun ReceiptPreviewCard(
    onClear: () -> Unit
) {
    Surface(
        color = ColorTokens.SurfaceLevel2,
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
                    .background(ColorTokens.Primary500.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = ColorTokens.Primary500
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Receipt Scanned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Amount auto-filled",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear receipt",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                        year = yesterday.get(Calendar.YEAR)
                        month = yesterday.get(Calendar.MONTH)
                        day = yesterday.get(Calendar.DAY_OF_MONTH)
                    }) {
                        Text("Yesterday", color = ColorTokens.Primary500)
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
                        Text("OK", color = MaterialTheme.colorScheme.onPrimary)
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
            containerColor = if (isSelected) ColorTokens.Primary500 else ColorTokens.SurfaceLevel2,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
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
                color = if (isSelected) ColorTokens.Primary500 else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                if (isSelected) ColorTokens.Primary500.copy(alpha = 0.1f) else ColorTokens.SurfaceLevel2,
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
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) ColorTokens.Primary500 else MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelector(
    account: AccountEntity?,
    accounts: List<AccountEntity>,
    onAccountSelected: (AccountEntity) -> Unit,
    isError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Surface(
            color = if (isError) ColorTokens.Error500.copy(alpha = 0.1f) else ColorTokens.SurfaceLevel2,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingTokens.Medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (account != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(account.color)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = account.icon,
                            fontSize = 20.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = account.formattedBalance(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ColorTokens.SurfaceLevel1),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Select Account",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isError) ColorTokens.Error500 else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select",
                    tint = if (isError) ColorTokens.Error500 else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            accounts.forEach { acc ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(SpacingTokens.ExtraLarge)
                                    .clip(CircleShape)
                                    .background(Color(acc.color)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = acc.icon,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = acc.formattedBalance(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = {
                        onAccountSelected(acc)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SaveAsTemplateDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var templateName by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save as Template", color = MaterialTheme.colorScheme.onBackground) },
        text = {
            Column {
                Text(
                    text = "Create a template from this transaction for quick future entries.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = SpacingTokens.Medium)
                )
                OutlinedTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text("Template Name") },
                    placeholder = { Text("e.g., Morning Coffee") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(templateName) },
                colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Primary500),
                enabled = templateName.isNotBlank()
            ) {
                Text("Save", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = ColorTokens.SurfaceLevel2
    )
}




