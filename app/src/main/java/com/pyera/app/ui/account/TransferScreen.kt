package com.pyera.app.ui.account

import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.formattedBalance
import com.pyera.app.ui.theme.*
import com.pyera.app.ui.util.CurrencyFormatter
import com.pyera.app.ui.util.pyeraBackground
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    fromAccountId: Long? = null,
    navController: NavController,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val accounts by viewModel.activeAccounts.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    var selectedFromAccountId by rememberSaveable { mutableStateOf<Long?>(fromAccountId) }
    val fromAccount = selectedFromAccountId?.let { id -> accounts.find { it.id == id } }
    var selectedToAccountId by rememberSaveable { mutableStateOf<Long?>(null) }
    val toAccount = selectedToAccountId?.let { id -> accounts.find { it.id == id } }
    var amount by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showFromAccountPicker by rememberSaveable { mutableStateOf(false) }
    var showToAccountPicker by rememberSaveable { mutableStateOf(false) }
    var isAmountError by rememberSaveable { mutableStateOf(false) }
    
    // Set initial from account if provided
    LaunchedEffect(fromAccountId, accounts) {
        if (fromAccount == null && fromAccountId != null) {
            selectedFromAccountId = fromAccountId
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Money") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
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
                .verticalScroll(rememberScrollState())
                .padding(SpacingTokens.Medium)
        ) {
            // Error message
            error?.let { errorMessage ->
                PyeraCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = ErrorContainer,
                    borderWidth = 0.dp
                ) {
                    Text(
                        text = errorMessage,
                        color = ColorTokens.Error500,
                        modifier = Modifier.padding(SpacingTokens.Medium)
                    )
                }
                Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            }
            
            // From Account
            Text(
                text = "From",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            AccountSelector(
                account = fromAccount,
                placeholder = "Select source account",
                onClick = { showFromAccountPicker = true }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Transfer Arrow
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = ColorTokens.Primary500,
                    modifier = Modifier.size(SpacingTokens.ExtraLarge)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // To Account
            Text(
                text = "To",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            AccountSelector(
                account = toAccount,
                placeholder = "Select destination account",
                onClick = { showToAccountPicker = true }
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.Large))
            
            // Amount Input
            Text(
                text = "Amount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
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
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = ColorTokens.Primary500,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    
                    androidx.compose.foundation.text.BasicTextField(
                        value = amount,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() || char == '.' }) {
                                amount = it
                                isAmountError = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Start
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        decorationBox = { innerTextField ->
                            Box {
                                if (amount.isEmpty()) {
                                    Text(
                                        text = "0.00",
                                        style = MaterialTheme.typography.headlineMedium.copy(
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
            
            if (isAmountError) {
                Text(
                    text = "Please enter a valid amount",
                    color = ColorTokens.Error500,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = SpacingTokens.Medium, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            
            // Date Picker
            Text(
                text = "Date",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            Surface(
                color = ColorTokens.SurfaceLevel2,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
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
                        text = dateFormat.format(Date(selectedDate)),
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
            
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("What's this transfer for?") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorTokens.Primary500,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedLabelColor = ColorTokens.Primary500,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.ExtraLarge))
            
            // Transfer Button
            val accountsAreDistinct = fromAccount != null && toAccount != null && fromAccount.id != toAccount.id

            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull()
                    if (amountVal == null || amountVal <= 0) {
                        isAmountError = true
                        return@Button
                    }
                    if (fromAccount == null || toAccount == null) {
                        return@Button
                    }
                    
                    viewModel.transferBetweenAccounts(
                        fromAccountId = fromAccount.id,
                        toAccountId = toAccount.id,
                        amount = amountVal,
                        description = description
                    ) {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Primary500),
                enabled = !isLoading &&
                         accountsAreDistinct &&
                         amount.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(SpacingTokens.Large)
                    )
                } else {
                    Text(
                        text = "Transfer",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Available balance hint
            fromAccount?.let { account ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Available: ${account.formattedBalance()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
    
    // Account Pickers
    if (showFromAccountPicker) {
        AccountPickerDialog(
            title = "Select Source Account",
            accounts = accounts.filter { it.id != toAccount?.id },
            selectedAccount = fromAccount,
            onAccountSelected = { 
                selectedFromAccountId = it.id
                showFromAccountPicker = false
            },
            onDismiss = { showFromAccountPicker = false }
        )
    }
    
    if (showToAccountPicker) {
        AccountPickerDialog(
            title = "Select Destination Account",
            accounts = accounts.filter { it.id != fromAccount?.id },
            selectedAccount = toAccount,
            onAccountSelected = { 
                selectedToAccountId = it.id
                showToAccountPicker = false
            },
            onDismiss = { showToAccountPicker = false }
        )
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        TransferDatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { 
                selectedDate = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun AccountSelector(
    account: AccountEntity?,
    placeholder: String,
    onClick: () -> Unit
) {
    Surface(
        color = ColorTokens.SurfaceLevel2,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(account.color)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = account.icon,
                        fontSize = 24.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(SpacingTokens.Medium))
                
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
                        .size(48.dp)
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
                
                Spacer(modifier = Modifier.width(SpacingTokens.Medium))
                
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountPickerDialog(
    title: String,
    accounts: List<AccountEntity>,
    selectedAccount: AccountEntity?,
    onAccountSelected: (AccountEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(accounts) { account ->
                    val isSelected = account.id == selectedAccount?.id
                    
                    Surface(
                        color = if (isSelected) ColorTokens.Primary500.copy(alpha = 0.2f) else ColorTokens.SurfaceLevel1,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAccountSelected(account) }
                    ) {
                        Row(
                            modifier = Modifier.padding(SpacingTokens.Medium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(account.color)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = account.icon,
                                    fontSize = 24.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(SpacingTokens.Medium))
                            
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
                            
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = ColorTokens.Primary500
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = ColorTokens.SurfaceLevel2
    )
}

@Composable
private fun TransferDatePickerDialog(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = selectedDate
    
    var year by rememberSaveable { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var month by rememberSaveable { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var day by rememberSaveable { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date") },
        text = {
            Column {
                // Simple date picker using dropdowns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Month
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
                    
                    // Day
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
                    
                    // Year
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
                            repeat(5) { index ->
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
                
                // Quick buttons
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
            }
        },
        confirmButton = {
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
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = ColorTokens.SurfaceLevel2
    )
}



