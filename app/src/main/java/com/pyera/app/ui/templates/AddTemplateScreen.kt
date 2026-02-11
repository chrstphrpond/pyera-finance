package com.pyera.app.ui.templates

import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

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
import androidx.compose.ui.graphics.Color
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
import com.pyera.app.ui.util.pyeraBackground
import androidx.compose.ui.res.stringResource
import com.pyera.app.R
import com.pyera.app.ui.util.CurrencyFormatter

@Suppress("DEPRECATION")
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
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.add_template_back_content_desc),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pyeraBackground()
                .padding(paddingValues)
                .padding(horizontal = SpacingTokens.Medium)
                .verticalScroll(rememberScrollState())
        ) {
            // Template Preview Card
            Text(
                text = stringResource(R.string.add_template_preview_section),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TemplatePreviewCard(formState)

            Spacer(modifier = Modifier.height(SpacingTokens.Large))

            // Template Name
            OutlinedTextField(
                value = formState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.add_template_name_label), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                placeholder = { Text(stringResource(R.string.add_template_name_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ColorTokens.SurfaceLevel2,
                    unfocusedContainerColor = ColorTokens.SurfaceLevel2,
                    focusedBorderColor = ColorTokens.Primary500,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            // Type Selector
            Text(
                text = stringResource(R.string.add_template_transaction_type_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TypeChip(
                    label = stringResource(R.string.add_template_type_expense),
                    selected = formState.type == "EXPENSE",
                    color = ColorTokens.Error500,
                    onClick = { viewModel.onTypeChange("EXPENSE") },
                    modifier = Modifier.weight(1f)
                )
                TypeChip(
                    label = stringResource(R.string.add_template_type_income),
                    selected = formState.type == "INCOME",
                    color = ColorTokens.Primary500,
                    onClick = { viewModel.onTypeChange("INCOME") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            // Amount Section
            Text(
                text = stringResource(R.string.add_template_amount_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    label = { Text(stringResource(R.string.add_template_amount_field_label), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    placeholder = { Text(stringResource(R.string.add_template_amount_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ColorTokens.SurfaceLevel2,
                        unfocusedContainerColor = ColorTokens.SurfaceLevel2,
                        focusedBorderColor = ColorTokens.Primary500,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        disabledContainerColor = ColorTokens.SurfaceLevel2.copy(alpha = 0.5f),
                        disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.width(SpacingTokens.Medium))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.add_template_variable_switch),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = formState.hasVariableAmount,
                        onCheckedChange = viewModel::onVariableAmountToggle,
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = ColorTokens.Primary500,
                            checkedTrackColor = ColorTokens.Primary500.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            // Description
            OutlinedTextField(
                value = formState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text(stringResource(R.string.add_template_description_label), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                placeholder = { Text(stringResource(R.string.add_template_description_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ColorTokens.SurfaceLevel2,
                    unfocusedContainerColor = ColorTokens.SurfaceLevel2,
                    focusedBorderColor = ColorTokens.Primary500,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            // Category Selector
            Text(
                text = stringResource(R.string.add_template_category_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            CategorySelector(
                selectedCategoryId = formState.categoryId,
                categories = categories.filter { it.type == formState.type },
                onClick = { showCategoryPicker = true }
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            // Account Selector
            Text(
                text = stringResource(R.string.add_template_account_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            AccountSelector(
                selectedAccountId = formState.accountId,
                accounts = accounts,
                onClick = { showAccountPicker = true }
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            // Icon Picker
            Text(
                text = stringResource(R.string.add_template_icon_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            IconSelector(
                selectedIcon = formState.icon,
                onIconClick = { showIconPicker = true }
            )

            Spacer(modifier = Modifier.height(SpacingTokens.ExtraLarge))

            // Error message
            if (formState.error != null) {
                Text(
                    text = formState.error!!,
                    color = ColorTokens.Error500,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(SpacingTokens.Medium))
            }

            // Save Button
            Button(
                onClick = viewModel::saveTemplate,
                enabled = !formState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorTokens.Primary500,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (formState.isLoading) {
                    Text(stringResource(R.string.add_template_saving))
                } else {
                    Text(stringResource(R.string.add_template_save_button), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.ExtraLarge))
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
    PyeraCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        cornerRadius = SpacingTokens.Medium,
        containerColor = ColorTokens.SurfaceLevel2,
        borderWidth = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ColorTokens.Primary500.copy(alpha = 0.2f)),
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
                color = if (formState.name.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Amount
            val amountText = when {
                formState.hasVariableAmount -> stringResource(R.string.add_template_preview_variable_amount)
                formState.amount.isNotEmpty() -> "${CurrencyFormatter.SYMBOL}${formState.amount}"
                else -> stringResource(R.string.add_template_preview_no_amount)
            }
            Text(
                text = amountText,
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    formState.type == "INCOME" -> ColorTokens.Primary500
                    formState.hasVariableAmount || formState.amount.isEmpty() -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onBackground
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
            containerColor = ColorTokens.SurfaceLevel2,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = if (selected) null else FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
    )
}

@Composable
@Suppress("DEPRECATION")
private fun CategorySelector(
    selectedCategoryId: Int?,
    categories: List<CategoryEntity>,
    onClick: () -> Unit
) {
    val selectedCategory = categories.find { it.id == selectedCategoryId }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorTokens.SurfaceLevel2)
            .clickable(onClick = onClick)
            .padding(SpacingTokens.Medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (selectedCategory != null) {
                Box(
                    modifier = Modifier
                        .size(SpacingTokens.ExtraLarge)
                        .clip(CircleShape)
                        .background(Color(selectedCategory.color)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedCategory.icon,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedCategory.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    text = stringResource(R.string.add_template_category_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.add_template_select_category_content_desc),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
@Suppress("DEPRECATION")
private fun AccountSelector(
    selectedAccountId: Long?,
    accounts: List<AccountEntity>,
    onClick: () -> Unit
) {
    val selectedAccount = accounts.find { it.id == selectedAccountId }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorTokens.SurfaceLevel2)
            .clickable(onClick = onClick)
            .padding(SpacingTokens.Medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (selectedAccount != null) {
                Box(
                    modifier = Modifier
                        .size(SpacingTokens.ExtraLarge)
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
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    text = stringResource(R.string.add_template_account_placeholder),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.add_template_select_category_content_desc),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val contentDesc = stringResource(R.string.add_template_select_icon_content_desc)
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(ColorTokens.SurfaceLevel2)
            .border(
                width = 2.dp,
                color = if (selectedIcon != null) ColorTokens.Primary500 else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable(onClick = onIconClick)
            .semantics { contentDescription = contentDesc },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = selectedIcon ?: "+",
            fontSize = if (selectedIcon != null) 28.sp else 24.sp,
            color = if (selectedIcon != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
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
        PyeraCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Medium),
            cornerRadius = SpacingTokens.Medium,
            containerColor = ColorTokens.SurfaceLevel2,
            borderWidth = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(SpacingTokens.Medium)
            ) {
                Text(
                    text = stringResource(R.string.add_template_choose_icon_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = SpacingTokens.Medium)
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
                                    if (isSelected) ColorTokens.Primary500.copy(alpha = 0.3f)
                                    else ColorTokens.SurfaceLevel2
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ColorTokens.Primary500 else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
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

                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.add_template_cancel_button), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        PyeraCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Medium),
            cornerRadius = SpacingTokens.Medium,
            containerColor = ColorTokens.SurfaceLevel2,
            borderWidth = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(SpacingTokens.Medium)
            ) {
                Text(
                    text = stringResource(R.string.add_template_select_category_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = SpacingTokens.Medium)
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
                        icon = category.icon,
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
                    Text(stringResource(R.string.add_template_cancel_button), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            .background(if (isSelected) ColorTokens.Primary500.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color ?: ColorTokens.SurfaceLevel2),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.add_template_selected_content_desc),
                tint = ColorTokens.Primary500
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
        PyeraCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.Medium),
            cornerRadius = SpacingTokens.Medium,
            containerColor = ColorTokens.SurfaceLevel2,
            borderWidth = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(SpacingTokens.Medium)
            ) {
                Text(
                    text = stringResource(R.string.add_template_select_account_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = SpacingTokens.Medium)
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
                    Text(stringResource(R.string.add_template_cancel_button), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            .background(if (isSelected) ColorTokens.Primary500.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color ?: ColorTokens.SurfaceLevel2),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.add_template_selected_content_desc),
                tint = ColorTokens.Primary500
            )
        }
    }
}




