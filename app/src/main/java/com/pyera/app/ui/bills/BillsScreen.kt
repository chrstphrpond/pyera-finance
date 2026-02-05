package com.pyera.app.ui.bills

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
import com.pyera.app.ui.theme.AccentGreen
import com.pyera.app.ui.theme.CardBackground
import com.pyera.app.ui.theme.ColorError
import com.pyera.app.ui.theme.ColorSuccess
import com.pyera.app.ui.theme.DeepBackground
import com.pyera.app.ui.theme.TextPrimary
import com.pyera.app.ui.theme.TextSecondary
import com.pyera.app.ui.theme.TextTertiary
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
                containerColor = AccentGreen,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Bill")
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
                text = "Upcoming Bills",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (sortedBills.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No upcoming bills.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Due: ${formatDate(bill.dueDate)} (${bill.frequency})",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (bill.dueDate < System.currentTimeMillis()) ColorError else TextSecondary
                )
                Text(
                    text = "₱${String.format("%.2f", bill.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AccentGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Row {
                IconButton(onClick = onMarkPaid) {
                    Icon(Icons.Default.Check, contentDescription = "Mark as Paid", tint = ColorSuccess)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ColorError.copy(alpha = 0.7f))
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
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Bill Reminder",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bill Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                // Simple Frequency Selection
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    FilterChip(
                        selected = selectedFrequency == "MONTHLY",
                        onClick = { selectedFrequency = "MONTHLY" },
                        label = { Text("Monthly") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentGreen,
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = selectedFrequency == "YEARLY",
                        onClick = { selectedFrequency = "YEARLY" },
                        label = { Text("Yearly") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentGreen,
                            selectedLabelColor = Color.Black
                        )
                    )
                }


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
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (name.isNotEmpty() && amount > 0) {
                                onConfirm(name, amount, defaultDate, selectedFrequency)
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

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
