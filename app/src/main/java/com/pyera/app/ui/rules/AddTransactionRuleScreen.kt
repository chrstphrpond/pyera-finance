package com.pyera.app.ui.rules

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.pyera.app.R
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.MatchType
import com.pyera.app.ui.util.pyeraBackground

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionRuleScreen(
    navController: NavController,
    ruleId: Long? = null,
    viewModel: RulesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showHelpDialog by rememberSaveable { mutableStateOf(false) }

    // Load rule for editing if ruleId provided
    LaunchedEffect(ruleId) {
        ruleId?.let {
            if (!state.isEditMode) {
                viewModel.loadRuleForEdit(it)
            }
        }
    }

    // Show error messages
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Navigate back on success
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            navController.popBackStack()
        }
    }

    if (showHelpDialog) {
        MatchTypeHelpDialog(onDismiss = { showHelpDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (state.isEditMode) stringResource(R.string.add_rule_title_edit) else stringResource(R.string.add_rule_title_create)) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.rules_back_content_desc),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            Icons.Default.Help,
                            contentDescription = stringResource(R.string.add_rule_help_content_desc),
                            tint = ColorTokens.Primary500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pyeraBackground()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(SpacingTokens.Medium)
        ) {
            // Pattern Input
            Text(
                text = stringResource(R.string.add_rule_pattern_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = state.pattern,
                onValueChange = { viewModel.updatePattern(it) },
                placeholder = { Text(stringResource(R.string.add_rule_pattern_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ColorTokens.Primary500,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Large))

            // Match Type Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.add_rule_match_type_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = { showHelpDialog = true }) {
                    Icon(
                        Icons.Default.Help,
                        contentDescription = stringResource(R.string.add_rule_match_type_help_content_desc),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MatchType.entries.forEach { matchType ->
                    val isSelected = state.selectedMatchType == matchType
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateMatchType(matchType) },
                        label = { 
                            Text(
                                text = matchType.name.replace("_", " "),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ColorTokens.Primary500,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = ColorTokens.SurfaceLevel1,
                            labelColor = MaterialTheme.colorScheme.onBackground
                        ),
                        border = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.Large))

            // Category Selector
            Text(
                text = stringResource(R.string.add_rule_category_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (state.categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorTokens.SurfaceLevel1),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.add_rule_category_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val selectedCategory = state.categories.find { it.id == state.selectedCategoryId }
                
                // Expense categories
                Text(
                    text = stringResource(R.string.add_rule_expense_section),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.categories.filter { it.type == "EXPENSE" }.forEach { category ->
                        CategoryChip(
                            category = category,
                            isSelected = state.selectedCategoryId == category.id,
                            onClick = { viewModel.updateCategory(category.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                // Income categories
                Text(
                    text = stringResource(R.string.add_rule_income_section),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.categories.filter { it.type == "INCOME" }.forEach { category ->
                        CategoryChip(
                            category = category,
                            isSelected = state.selectedCategoryId == category.id,
                            onClick = { viewModel.updateCategory(category.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.Large))

            // Priority Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.add_rule_priority_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                state.priority >= 8 -> ColorTokens.Error500
                                state.priority >= 5 -> ColorTokens.Warning500
                                else -> ColorTokens.Success500
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = state.priority.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Text(
                text = stringResource(R.string.add_rule_priority_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Slider(
                value = state.priority.toFloat(),
                onValueChange = { viewModel.updatePriority(it.toInt()) },
                valueRange = 0f..10f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = ColorTokens.Primary500,
                    activeTrackColor = ColorTokens.Primary500,
                    inactiveTrackColor = ColorTokens.SurfaceLevel1
                )
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Large))

            // Test Section
            PyeraCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 12.dp,
                containerColor = ColorTokens.SurfaceLevel2,
                borderWidth = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.Medium)
                ) {
                    Text(
                        text = stringResource(R.string.add_rule_test_section_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = state.testDescription,
                        onValueChange = { viewModel.updateTestDescription(it) },
                        placeholder = { Text(stringResource(R.string.add_rule_test_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ColorTokens.Primary500,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            state.testResult?.let { result ->
                                Icon(
                                    imageVector = if (result) 
                                        Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = if (result) stringResource(R.string.add_rule_test_matches_content_desc) else stringResource(R.string.add_rule_test_no_match_content_desc),
                                    tint = if (result) ColorTokens.Success500 else ColorTokens.Error500
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.testRule() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorTokens.SurfaceLevel1,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        enabled = state.pattern.isNotBlank() && state.testDescription.isNotBlank()
                    ) {
                        Text(stringResource(R.string.add_rule_test_button))
                    }

                    // Test result
                    state.testResult?.let { result ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (result) ColorTokens.Success500.copy(alpha = 0.15f) 
                                    else ColorTokens.Error500.copy(alpha = 0.15f)
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (result) stringResource(R.string.add_rule_test_matches) else stringResource(R.string.add_rule_test_no_match),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (result) ColorTokens.Success500 else ColorTokens.Error500,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.ExtraLarge))

            // Save Button
            Button(
                onClick = { viewModel.saveRule() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorTokens.Primary500,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = state.pattern.isNotBlank() && state.selectedCategoryId != null && !state.isLoading
            ) {
                Text(
                    text = if (state.isEditMode) stringResource(R.string.add_rule_update_button) else stringResource(R.string.add_rule_save_button),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (state.isEditMode) {
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(
                    onClick = { 
                        viewModel.resetForm()
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.rules_cancel_button),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.Large))
        }
    }
}

@Composable
private fun CategoryChip(
    category: CategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(category.color) else ColorTokens.SurfaceLevel1
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground
    val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.onBackground else androidx.compose.ui.graphics.Color(category.color))
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.add_rule_category_selected_content_desc),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun MatchTypeHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ColorTokens.SurfaceLevel2,
        title = {
            Text(
                text = stringResource(R.string.add_rule_help_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                HelpItem(
                    title = stringResource(R.string.match_type_contains_title),
                    description = stringResource(R.string.match_type_contains_desc)
                )
                Spacer(modifier = Modifier.height(12.dp))
                HelpItem(
                    title = stringResource(R.string.match_type_starts_with_title),
                    description = stringResource(R.string.match_type_starts_with_desc)
                )
                Spacer(modifier = Modifier.height(12.dp))
                HelpItem(
                    title = stringResource(R.string.match_type_ends_with_title),
                    description = stringResource(R.string.match_type_ends_with_desc)
                )
                Spacer(modifier = Modifier.height(12.dp))
                HelpItem(
                    title = stringResource(R.string.match_type_exact_title),
                    description = stringResource(R.string.match_type_exact_desc)
                )
                Spacer(modifier = Modifier.height(12.dp))
                HelpItem(
                    title = stringResource(R.string.match_type_regex_title),
                    description = stringResource(R.string.match_type_regex_desc)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.add_rule_help_close), color = ColorTokens.Primary500)
            }
        }
    )
}

@Composable
private fun HelpItem(title: String, description: String) {
    Column {
        Text(
            text = title,
            color = ColorTokens.Primary500,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}




