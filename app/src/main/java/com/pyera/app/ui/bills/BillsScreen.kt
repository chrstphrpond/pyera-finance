package com.pyera.app.ui.bills
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.pyera.app.data.local.entity.BillEntity
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.CardBackground
import com.pyera.app.ui.util.CurrencyFormatter
import com.pyera.app.ui.util.pyeraBackground
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BillsScreen(
    viewModel: BillsViewModel = hiltViewModel()
) {
    val bills by viewModel.bills.collectAsState()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    // Filter to show upcoming (unpaid) bills first
    val sortedBills = bills.sortedBy { it.dueDate }

    if (showAddDialog) {
        AddBillDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amount, date, frequency ->
                viewModel.addBill(name, amount, date, frequency)
                showAddDialog = false
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
                Icon(Icons.Default.Add, contentDescription = "Add Bill")
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
                text = "Upcoming Bills",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            if (sortedBills.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No upcoming bills.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.MediumSmall)
                ) {
                    items(sortedBills) { bill ->
                        BillItem(
                            bill = bill,
                            onMarkPaid = { viewModel.markAsPaid(bill) },
                            onDelete = { viewModel.deleteBill(bill) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BillItem(
    bill: BillEntity,
    onMarkPaid: () -> Unit,
    onDelete: () -> Unit
) {
    PyeraCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(SpacingTokens.Medium)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Due: ${formatDate(bill.dueDate)} (${bill.frequency})",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (bill.dueDate < System.currentTimeMillis()) ColorTokens.Error500 else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.format(bill.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    color = ColorTokens.Primary500,
                    fontWeight = FontWeight.Bold
                )
            }

            Row {
                IconButton(onClick = onMarkPaid) {
                    Icon(Icons.Default.Check, contentDescription = "Mark as Paid", tint = ColorTokens.Success500)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ColorTokens.Error500.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun AddBillDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Long, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var selectedFrequency by rememberSaveable { mutableStateOf("MONTHLY") }
    // Default to 1 week from now
    val defaultDate = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000

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
                    text = "Add Bill Reminder",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bill Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = ColorTokens.Primary500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Small))

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
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(SpacingTokens.Medium))
                
                // Simple Frequency Selection
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    FilterChip(
                        selected = selectedFrequency == "MONTHLY",
                        onClick = { selectedFrequency = "MONTHLY" },
                        label = { Text("Monthly") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ColorTokens.Primary500,
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = selectedFrequency == "YEARLY",
                        onClick = { selectedFrequency = "YEARLY" },
                        label = { Text("Yearly") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ColorTokens.Primary500,
                            selectedLabelColor = Color.Black
                        )
                    )
                }


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
                            if (name.isNotEmpty() && amount > 0) {
                                onConfirm(name, amount, defaultDate, selectedFrequency)
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

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}



