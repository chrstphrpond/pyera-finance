package com.pyera.app.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pyera.app.data.local.entity.AccountType
import com.pyera.app.data.local.entity.defaultIcon
import com.pyera.app.data.local.entity.displayName
import com.pyera.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    navController: NavController,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    var showIconPicker by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (formState.isEditing) "Edit Account" else "Add Account") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Error message
            error?.let { errorMessage ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ErrorContainer)
                ) {
                    Text(
                        text = errorMessage,
                        color = ColorError,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Account Icon Preview
            AccountIconPreview(
                icon = formState.icon,
                color = formState.color,
                onClick = { showIconPicker = true }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Account Name
            OutlinedTextField(
                value = formState.name,
                onValueChange = { viewModel.updateFormState(name = it) },
                label = { Text("Account Name") },
                placeholder = { Text("e.g., BPI Savings, GCash") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = AccentGreen,
                    unfocusedLabelColor = TextSecondary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Account Type Selector
            Text(
                text = "Account Type",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            AccountTypeSelector(
                selectedType = formState.type,
                onTypeSelected = { 
                    viewModel.updateFormState(
                        type = it,
                        icon = it.defaultIcon()
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Initial Balance
            OutlinedTextField(
                value = formState.initialBalance,
                onValueChange = { viewModel.updateFormState(initialBalance = it) },
                label = { Text("Initial Balance") },
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                prefix = { Text("₱") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = AccentGreen,
                    unfocusedLabelColor = TextSecondary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Color Picker
            Text(
                text = "Account Color",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            ColorPicker(
                selectedColor = formState.color,
                onColorSelected = { viewModel.updateFormState(color = it) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Default Account Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Set as Default",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Use this account for new transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = formState.isDefault,
                    onCheckedChange = { viewModel.updateFormState(isDefault = it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentGreen,
                        checkedTrackColor = AccentGreen.copy(alpha = 0.5f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Save Button
            Button(
                onClick = {
                    if (formState.isEditing) {
                        viewModel.updateAccount { navController.popBackStack() }
                    } else {
                        viewModel.createAccount { navController.popBackStack() }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                enabled = !isLoading && formState.name.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = DeepBackground,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = if (formState.isEditing) "Update Account" else "Create Account",
                        color = DeepBackground,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
    
    // Icon Picker Dialog
    if (showIconPicker) {
        IconPickerDialog(
            selectedIcon = formState.icon,
            onIconSelected = {
                viewModel.updateFormState(icon = it)
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}

@Composable
private fun AccountIconPreview(
    icon: String,
    color: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(color))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 40.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap to change icon",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun AccountTypeSelector(
    selectedType: AccountType,
    onTypeSelected: (AccountType) -> Unit
) {
    val accountTypes = AccountType.values()
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(accountTypes) { type ->
            val isSelected = type == selectedType
            
            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(type) },
                label = { Text(type.displayName()) },
                leadingIcon = {
                    Text(type.defaultIcon())
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentGreen.copy(alpha = 0.2f),
                    selectedLabelColor = AccentGreen
                )
            )
        }
    }
}

@Composable
private fun ColorPicker(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(AccountColors) { color ->
            val isSelected = color == selectedColor
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(color))
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = if (isSelected) TextPrimary else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun IconPickerDialog(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val icons = listOf(
        "🏦", "💳", "💵", "💰", "💎",
        "📱", "🏧", "💸", "💲", "💱",
        "🏛️", "🪙", "💴", "💶", "💷",
        "🧧", "🎁", "🏪", "🛒", "🛍️"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Icon") },
        text = {
            Column {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icons.forEach { icon ->
                        val isSelected = icon == selectedIcon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) AccentGreen.copy(alpha = 0.2f)
                                    else SurfaceElevated
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) AccentGreen else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onIconSelected(icon) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = icon, fontSize = 24.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceElevated
    )
}

// Simple FlowRow implementation
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val hGapPx = 8.dp.roundToPx()
        val vGapPx = 8.dp.roundToPx()
        
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()
        
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0
        
        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)
            
            if (currentRow.isNotEmpty() && currentRowWidth + hGapPx + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                rowWidths.add(currentRowWidth)
                rowHeights.add(currentRowHeight)
                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }
            
            currentRow.add(placeable)
            currentRowWidth += if (currentRow.size == 1) placeable.width else hGapPx + placeable.width
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }
        
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentRowWidth)
            rowHeights.add(currentRowHeight)
        }
        
        val height = rowHeights.sum() + (rows.size - 1).coerceAtLeast(0) * vGapPx
        
        layout(constraints.maxWidth, height) {
            var y = 0
            rows.forEachIndexed { index, row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + hGapPx
                }
                y += rowHeights[index] + vGapPx
            }
        }
    }
}
