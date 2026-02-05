package com.pyera.app.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.ui.theme.AccentGreen
import com.pyera.app.ui.theme.ColorError
import com.pyera.app.ui.theme.DarkGreen
import com.pyera.app.ui.theme.NeonYellow
import com.pyera.app.ui.theme.SurfaceElevated
import com.pyera.app.ui.theme.TextPrimary
import androidx.compose.ui.res.stringResource
import com.pyera.app.R
import com.pyera.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTemplateScreen(
    onNavigateBack: () -> Unit,
    onTemplateSaved: () -> Unit,
    categories: List<CategoryEntity> = emptyList(),
    accounts: List<AccountEntity> = emptyList(),
    viewModel: TemplatesViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    var showIconPicker by rememberSaveable { mutableStateOf(false) }
    var showCategoryPicker by rememberSaveable { mutableStateOf(false) }
    var showAccountPicker by rememberSaveable { mutableStateOf(false) }

    // Handle success
    LaunchedEffect(formState.isSuccess) {
        if (formState.isSuccess) {
            onTemplateSaved()
            viewModel.clearForm()
        }
    }

    // Handle error
    LaunchedEffect(formState.error) {
        formState.error?.let {
            // Error is shown in the UI
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_template_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.add_template_back_content_desc),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGreen
                )
            )
        },
        containerColor = DarkGreen
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Template Preview Card
            Text(
                text = stringResource(R.string.add_template_preview_section),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TemplatePreviewCard(formState)

            Spacer(modifier = Modifier.height(24.dp))

            // Template Name
            OutlinedTextField(
                value = formState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.add_template_name_label), color = TextSecondary) },
                placeholder = { Text(stringResource(R.string.add_template_name_placeholder), color = TextSecondary.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceElevated,
                    unfocusedContainerColor = SurfaceElevated,
                    focusedBorderColor = NeonYellow,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Type Selector
            Text(
                text = stringResource(R.string.add_template_transaction_type_label),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TypeChip(
                    label = stringResource(R.string.add_template_type_expense),
                    selected = formState.type == "EXPENSE",
                    color = ColorError,
                    onClick = { viewModel.onTypeChange("EXPENSE") },
                    modifier = Modifier.weight(1f)
                )
                TypeChip(
                    label = stringResource(R.string.add_template_type_income),
                    selected = formState.type == "INCOME",
                    color = AccentGreen,
                    onClick = { viewModel.onTypeChange("INCOME") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Section
            Text(
                text = stringResource(R.string.add_template_amount_label),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = formState.amount,
                    onValueChange = viewModel::onAmountChange,
                    enabled = !formState.hasVariableAmount,
                    label = { Text(stringResource(R.string.add_template_amount_field_label), color = TextSecondary) },
                    placeholder = { Text(stringResource(R.string.add_template_amount_placeholder), color = TextSecondary.copy(alpha = 0.5f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated,
                        focusedBorderColor = NeonYellow,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        disabledContainerColor = SurfaceElevated.copy(alpha = 0.5f),
                        disabledTextColor = TextSecondary
                    )
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.add_template_variable_switch),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Switch(
                        checked = formState.hasVariableAmount,
                        onCheckedChange = viewModel::onVariableAmountToggle,
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = NeonYellow,
                            checkedTrackColor = NeonYellow.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = formState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text(stringResource(R.string.add_template_description_label), color = TextSecondary) },
                placeholder = { Text(stringResource(R.string.add_template_description_placeholder), color = TextSecondary.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceElevated,
                    unfocusedContainerColor = SurfaceElevated,
                    focusedBorderColor = NeonYellow,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selector
            Text(
                text = stringResource(R.string.add_template_category_label),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            CategorySelector(
                selectedCategoryId = formState.categoryId,
                categories = categories.filter { it.type == formState.type },
                onCategorySelected = viewModel::onCategoryChange,
                onClick = { showCategoryPicker = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Account Selector
            Text(
                text = stringResource(R.string.add_template_account_label),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            AccountSelector(
                selectedAccountId = formState.accountId,
                accounts = accounts,
                onAccountSelected = viewModel::onAccountChange,
                onClick = { showAccountPicker = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Icon Picker
            Text(
                text = stringResource(R.string.add_template_icon_label),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            IconSelector(
                selectedIcon = formState.icon,
                onIconClick = { showIconPicker = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Error message
            if (formState.error != null) {
                Text(
                    text = formState.error!!,
                    color = ColorError,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Save Button
            Button(
                onClick = viewModel::saveTemplate,
                enabled = !formState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonYellow,
                    contentColor = DarkGreen
                )
            ) {
                if (formState.isLoading) {
                    Text(stringResource(R.string.add_template_saving))
                } else {
                    Text(stringResource(R.string.add_template_save_button), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Icon Picker Dialog
    if (showIconPicker) {
        IconPickerDialog(
            selectedIcon = formState.icon,
            onIconSelected = {
                viewModel.onIconChange(it)
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }

    // Category Picker Dialog
    if (showCategoryPicker) {
        CategoryPickerDialog(
            categories = categories.filter { it.type == formState.type },
            selectedCategoryId = formState.categoryId,
            onCategorySelected = {
                viewModel.onCategoryChange(it)
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }

    // Account Picker Dialog
    if (showAccountPicker) {
        AccountPickerDialog(
            accounts = accounts,
            selectedAccountId = formState.accountId,
            onAccountSelected = {
                viewModel.onAccountChange(it)
                showAccountPicker = false
            },
            onDismiss = { showAccountPicker = false }
        )
    }
}

@Composable
private fun TemplatePreviewCard(formState: TemplateFormState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = SurfaceElevated
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(NeonYellow.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formState.icon ?: getDefaultIcon(formState.type),
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name
            Text(
                text = formState.name.ifEmpty { stringResource(R.string.add_template_preview_default_name) },
                style = MaterialTheme.typography.titleLarge,
                color = if (formState.name.isEmpty()) TextSecondary else TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Amount
            val amountText = when {
                formState.hasVariableAmount -> stringResource(R.string.add_template_preview_variable_amount)
                formState.amount.isNotEmpty() -> "₱${formState.amount}"
                else -> stringResource(R.string.add_template_preview_no_amount)
            }
            Text(
                text = amountText,
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    formState.type == "INCOME" -> AccentGreen
                    formState.hasVariableAmount || formState.amount.isEmpty() -> TextSecondary
                    else -> TextPrimary
                }
            )
        }
    }
}

private fun getDefaultIcon(type: String): String = when (type) {
    "INCOME" -> "💰"
    else -> "💳"
}

@Composable
private fun TypeChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = color,
            containerColor = SurfaceElevated,
            labelColor = TextSecondary
        ),
        border = if (selected) null else FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = TextSecondary.copy(alpha = 0.3f)
        )
    )
}

@Composable
private fun CategorySelector(
    selectedCategoryId: Int?,
    categories: List<CategoryEntity>,
    onCategorySelected: (Int?) -> Unit,
    onClick: () -> Unit
) {
    val selectedCategory = categories.find { it.id == selectedCategoryId }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (selectedCategory != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(selectedCategory.color)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedCategory.icon ?: "📁",
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedCategory.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    text = stringResource(R.string.add_template_category_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.add_template_select_category_content_desc),
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AccountSelector(
    selectedAccountId: Long?,
    accounts: List<AccountEntity>,
    onAccountSelected: (Long?) -> Unit,
    onClick: () -> Unit
) {
    val selectedAccount = accounts.find { it.id == selectedAccountId }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (selectedAccount != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(selectedAccount.color)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedAccount.icon,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedAccount.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    text = stringResource(R.string.add_template_account_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.add_template_select_category_content_desc),
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun IconSelector(
    selectedIcon: String?,
    onIconClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(SurfaceElevated)
            .border(
                width = 2.dp,
                color = if (selectedIcon != null) NeonYellow else TextSecondary.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable(onClick = onIconClick)
            .semantics { contentDescription = stringResource(R.string.add_template_select_icon_content_desc) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = selectedIcon ?: "+",
            fontSize = if (selectedIcon != null) 28.sp else 24.sp,
            color = if (selectedIcon != null) TextPrimary else TextSecondary
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconPickerDialog(
    selectedIcon: String?,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val icons = listOf(
        "☕", "🍔", "🍕", "🥗", "🍜", "🍱", "🍣",
        "🚗", "🚌", "✈️", "🚕", "⛽", "🅿️",
        "🏠", "💡", "💧", "📱", "🌐", "📺",
        "🛒", "👕", "👟", "💊", "🏥", "💪",
        "🎬", "🎮", "🎵", "📚", "🎨", "🎯",
        "💰", "💵", "💳", "🏦", "📈", "💎",
        "🐕", "🐱", "🎁", "✂️", "🔧", "📝"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = SurfaceElevated
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.add_template_choose_icon_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    icons.forEach { icon ->
                        val isSelected = icon == selectedIcon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) NeonYellow.copy(alpha = 0.3f)
                                    else SurfaceElevated
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) NeonYellow else TextSecondary.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .clickable { onIconSelected(icon) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = icon,
                                fontSize = 24.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.add_template_cancel_button), color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun CategoryPickerDialog(
    categories: List<CategoryEntity>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = SurfaceElevated
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.add_template_select_category_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // None option
                CategoryOption(
                    icon = "❌",
                    name = stringResource(R.string.add_template_no_category),
                    isSelected = selectedCategoryId == null,
                    onClick = { onCategorySelected(null) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category list
                categories.forEach { category ->
                    CategoryOption(
                        icon = category.icon ?: "📁",
                        name = category.name,
                        color = androidx.compose.ui.graphics.Color(category.color),
                        isSelected = category.id == selectedCategoryId,
                        onClick = { onCategorySelected(category.id) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.add_template_cancel_button), color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun CategoryOption(
    icon: String,
    name: String,
    color: Color? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NeonYellow.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color ?: SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.add_template_selected_content_desc),
                tint = NeonYellow
            )
        }
    }
}

@Composable
private fun AccountPickerDialog(
    accounts: List<AccountEntity>,
    selectedAccountId: Long?,
    onAccountSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = SurfaceElevated
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.add_template_select_account_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // None option
                AccountOption(
                    icon = "❌",
                    name = stringResource(R.string.add_template_no_account),
                    isSelected = selectedAccountId == null,
                    onClick = { onAccountSelected(null) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Account list
                accounts.forEach { account ->
                    AccountOption(
                        icon = account.icon,
                        name = account.name,
                        color = androidx.compose.ui.graphics.Color(account.color),
                        isSelected = account.id == selectedAccountId,
                        onClick = { onAccountSelected(account.id) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.add_template_cancel_button), color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun AccountOption(
    icon: String,
    name: String,
    color: Color? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NeonYellow.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color ?: SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.add_template_selected_content_desc),
                tint = NeonYellow
            )
        }
    }
}
