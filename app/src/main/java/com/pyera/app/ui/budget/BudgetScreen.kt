package com.pyera.app.ui.budget
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.pyera.app.data.local.entity.CategoryEntity
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.pyera.app.ui.components.EmptyBudget
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.CardBackground
import com.pyera.app.ui.util.CurrencyFormatter
import com.pyera.app.ui.util.pyeraBackground

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showSetBudgetDialogCategoryId by rememberSaveable { mutableStateOf<Int?>(null) }
    val showSetBudgetDialog = showSetBudgetDialogCategoryId?.let { catId ->
        state.items.find { it.category.id == catId }
    }
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    val refreshing = isRefreshing || state.isLoading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.refreshBudgets()
            isRefreshing = false
        }
    )

    showSetBudgetDialog?.let { budgetItem ->
        SetBudgetDialog(
            item = budgetItem,
            onDismiss = { showSetBudgetDialogCategoryId = null },
            onConfirm = { amount ->
                viewModel.setBudget(budgetItem.category.id, amount)
                showSetBudgetDialogCategoryId = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pyeraBackground()
            .padding(SpacingTokens.Medium)
    ) {
        Text(
            text = "Monthly Budget",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = state.currentPeriod,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(SpacingTokens.Medium))

        // Budget List with Pull-to-Refresh
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ColorTokens.Primary500)
                    }
                }
                state.items.isEmpty() -> {
                    EmptyBudget(
                        onCreateClick = { showSetBudgetDialogCategoryId = -1 }
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.Medium)
                    ) {
                        items(
                            items = state.items,
                            key = { it.category.id }
                        ) { item ->
                            BudgetItemCard(
                                item = item,
                                onClick = { showSetBudgetDialogCategoryId = item.category.id }
                            )
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = CardBackground,
                contentColor = ColorTokens.Primary500
            )
        }
    }
}

@Composable
fun BudgetItemCard(
    item: BudgetItem,
    onClick: () -> Unit
) {
    PyeraCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(SpacingTokens.Medium)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color(item.category.color),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = item.category.name.take(1),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.category.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Budget",
                        tint = ColorTokens.Primary500
                    )
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.MediumSmall))

            // Progress Bar
            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SpacingTokens.Small)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (item.progress >= 1f) ColorTokens.Error500 else ColorTokens.Primary500,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f).copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(SpacingTokens.Small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ${CurrencyFormatter.formatShort(item.spentAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Budget: ${CurrencyFormatter.formatShort(item.budgetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            if (item.remaining < 0) {
                 Text(
                    text = "Over budget by ${CurrencyFormatter.formatShort(kotlin.math.abs(item.remaining))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorTokens.Error500,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun SetBudgetDialog(
    item: BudgetItem,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by rememberSaveable { mutableStateOf(if (item.budgetAmount > 0) item.budgetAmount.toString() else "") }

    Dialog(onDismissRequest = onDismiss) {
        PyeraCard(
            cornerRadius = SpacingTokens.Medium,
            containerColor = CardBackground,
            borderWidth = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(SpacingTokens.Large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Set Budget for ${item.category.name}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(SpacingTokens.Medium))
                
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = ColorTokens.Primary500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        focusedLabelColor = ColorTokens.Primary500,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(SpacingTokens.Large))
                
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
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            onConfirm(amount)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Primary500)
                    ) {
                        Text("Save", color = Color.Black)
                    }
                }
            }
        }
    }
}



