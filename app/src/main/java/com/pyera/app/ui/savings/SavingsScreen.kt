package com.pyera.app.ui.savings

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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pyera.app.ui.components.EmptySavings
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.AccentGreen
import com.pyera.app.ui.theme.CardBackground
import com.pyera.app.ui.theme.DeepBackground
import com.pyera.app.ui.theme.TextPrimary
import com.pyera.app.ui.theme.TextSecondary
import com.pyera.app.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SavingsScreen(
    viewModel: SavingsViewModel = hiltViewModel()
) {
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var goalToUpdateId by rememberSaveable { mutableStateOf<Long?>(null) }
    val goalToUpdate = goalToUpdateId?.let { id -> savingsGoals.find { it.id == id } }
    var isRefreshing by rememberSaveable { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    if (showAddDialog) {
        AddSavingsGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, target, date ->
                viewModel.addSavingsGoal(name, target, date, 0, AccentGreen.hashCode())
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
                containerColor = AccentGreen,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        },
        containerColor = DeepBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Savings Goals",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Savings Goals List with Pull-to-Refresh
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    // Data comes from StateFlow which auto-refreshes
                    // Just showing the visual feedback
                    isRefreshing = true
                    isRefreshing = false
                },
                indicator = { state, trigger ->
                    SwipeRefreshIndicator(
                        state = state,
                        refreshTriggerDistance = trigger,
                        backgroundColor = CardBackground,
                        contentColor = AccentGreen
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) {
                if (savingsGoals.isEmpty()) {
                    EmptySavings(onAddClick = { showAddDialog = true })
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                .padding(16.dp)
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
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = AccentGreen,
                trackColor = TextTertiary.copy(alpha = 0.3f),
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₱${String.format("%.2f", goal.currentAmount)} / ₱${String.format("%.2f", goal.targetAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "By ${formatDate(goal.deadline)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
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
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "New Savings Goal",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = TextTertiary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = TextTertiary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                            val target = targetText.toDoubleOrNull() ?: 0.0
                            if (name.isNotEmpty() && target > 0) {
                                onConfirm(name, target, defaultDate)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
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
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Update ${goal.name}",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Current: ₱${String.format("%.2f", goal.currentAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("New Total Amount") }, // Simplifying to set total amount
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = TextTertiary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = Color.Red.copy(alpha = 0.7f))
                    }
                    
                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val newAmount = amountText.toDoubleOrNull()
                                if (newAmount != null && newAmount >= 0) {
                                    onConfirm(newAmount)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
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
