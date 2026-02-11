package com.pyera.app.ui.savings
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.pyera.app.data.local.entity.SavingsGoalEntity
import androidx.compose.foundation.lazy.items
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.pyera.app.ui.components.EmptySavings
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.CardBackground
import com.pyera.app.ui.util.CurrencyFormatter
import com.pyera.app.ui.util.pyeraBackground
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SavingsScreen(
    viewModel: SavingsViewModel = hiltViewModel()
) {
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var goalToUpdateId by rememberSaveable { mutableStateOf<Int?>(null) }
    val goalToUpdate = goalToUpdateId?.let { id -> savingsGoals.find { it.id == id } }
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            isRefreshing = false
        }
    )

    if (showAddDialog) {
        AddSavingsGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, target, date ->
                viewModel.addSavingsGoal(name, target, date, 0, ColorTokens.Primary500.hashCode())
                showAddDialog = false
            }
        )
    }

    goalToUpdate?.let { goal ->
        UpdateSavingsDialog(
            goal = goal,
            onDismiss = { goalToUpdateId = null },
            onConfirm = { newAmount ->
                viewModel.updateProgress(goal, newAmount)
                goalToUpdateId = null
            },
            onDelete = {
                viewModel.deleteGoal(goal)
                goalToUpdateId = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ColorTokens.Primary500,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pyeraBackground()
                .padding(innerPadding)
                .padding(SpacingTokens.Medium)
        ) {
            Text(
                text = "Savings Goals",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            // Savings Goals List with Pull-to-Refresh
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                if (savingsGoals.isEmpty()) {
                    EmptySavings(onAddClick = { showAddDialog = true })
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.MediumSmall)
                    ) {
                        items(
                            items = savingsGoals,
                            key = { goal: SavingsGoalEntity -> goal.id }
                        ) { goal: SavingsGoalEntity ->
                            SavingsGoalItem(
                                goal = goal,
                                onClick = { goalToUpdateId = goal.id }
                            )
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = CardBackground,
                    contentColor = ColorTokens.Primary500
                )
            }
        }
    }
}

@Composable
fun SavingsGoalItem(
    goal: SavingsGoalEntity,
    onClick: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
    
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorTokens.Primary500,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(SpacingTokens.Small))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SpacingTokens.Small)
                    .clip(RoundedCornerShape(4.dp)),
                color = ColorTokens.Primary500,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f).copy(alpha = 0.3f),
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.Small))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${CurrencyFormatter.format(goal.currentAmount)} / ${CurrencyFormatter.format(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "By ${formatDate(goal.deadline)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Long) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var targetText by rememberSaveable { mutableStateOf("") }
    // Default to 1 month from now
    val defaultDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000 

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
                    text = "New Savings Goal",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = ColorTokens.Primary500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Small))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = ColorTokens.Primary500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
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
                            val target = targetText.toDoubleOrNull() ?: 0.0
                            if (name.isNotEmpty() && target > 0) {
                                onConfirm(name, target, defaultDate)
                            }
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

@Composable
fun UpdateSavingsDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    onDelete: () -> Unit
) {
    var amountText by rememberSaveable { mutableStateOf("") }

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
                    text = "Update ${goal.name}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Current: ${CurrencyFormatter.format(goal.currentAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("New Total Amount") }, // Simplifying to set total amount
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = ColorTokens.Primary500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Large))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = Color.Red.copy(alpha = 0.7f))
                    }
                    
                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val newAmount = amountText.toDoubleOrNull()
                                if (newAmount != null && newAmount >= 0) {
                                    onConfirm(newAmount)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.Primary500)
                        ) {
                            Text("Update", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}



