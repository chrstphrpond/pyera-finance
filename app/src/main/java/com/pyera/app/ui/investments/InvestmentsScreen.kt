package com.pyera.app.ui.investments
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.pyera.app.data.local.entity.InvestmentEntity
import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.CardBackground
import com.pyera.app.ui.theme.NegativeChange
import com.pyera.app.ui.theme.PositiveChange
import com.pyera.app.ui.util.CurrencyFormatter
import com.pyera.app.ui.util.pyeraBackground
import java.util.*

@Composable
fun InvestmentsScreen(
    viewModel: InvestmentsViewModel = hiltViewModel()
) {
    val investments by viewModel.investments.collectAsStateWithLifecycle()
    val totalValue by viewModel.totalPortfolioValue.collectAsStateWithLifecycle()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var investmentToUpdateId by rememberSaveable { mutableStateOf<Int?>(null) }
    val investmentToUpdate = investmentToUpdateId?.let { id -> investments.find { it.id == id } }

    if (showAddDialog) {
        AddInvestmentDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, invested, current ->
                viewModel.addInvestment(name, type, invested, current)
                showAddDialog = false
            }
        )
    }

    investmentToUpdate?.let { investment ->
        UpdateInvestmentDialog(
            investment = investment,
            onDismiss = { investmentToUpdateId = null },
            onConfirm = { newValue ->
                viewModel.updateValue(investment, newValue)
                investmentToUpdateId = null
            },
            onDelete = {
                viewModel.deleteInvestment(investment)
                investmentToUpdateId = null
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
                Icon(Icons.Default.Add, contentDescription = "Add Asset")
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
            // Portfolio Summary
            PyeraCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(SpacingTokens.Large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Portfolio Value",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(SpacingTokens.Small))
                    Text(
                        text = CurrencyFormatter.format(totalValue),
                        style = MaterialTheme.typography.displayMedium,
                        color = ColorTokens.Primary500,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.Large))
            
            Text(
                text = "My Assets",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(SpacingTokens.Medium))

            if (investments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No investments yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.MediumSmall)
                ) {
                    items(
                        items = investments,
                        key = { investment: InvestmentEntity -> investment.id }
                    ) { investment: InvestmentEntity ->
                        InvestmentItem(investment, onClick = { investmentToUpdateId = investment.id })
                    }
                }
            }
        }
    }
}

@Composable
fun InvestmentItem(investment: InvestmentEntity, onClick: () -> Unit) {
    val profit = investment.currentValue - investment.amountInvested
    val profitPercent = if (investment.amountInvested > 0) (profit / investment.amountInvested) * 100 else 0.0

    PyeraCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(
            modifier = Modifier.padding(SpacingTokens.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = investment.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = investment.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(investment.currentValue),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (profit >= 0) "+" else ""}${String.format("%.2f", profitPercent)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (profit >= 0) PositiveChange else NegativeChange
                )
            }
        }
    }
}

@Composable
fun AddInvestmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("STOCK") }
    var investedText by rememberSaveable { mutableStateOf("") }
    // Initial value same as invested
    
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
                    text = "Add Asset",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Asset Name (e.g. AAPL)") },
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
                    value = investedText,
                    onValueChange = { investedText = it },
                    label = { Text("Amount Invested") },
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
                            val invested = investedText.toDoubleOrNull() ?: 0.0
                            if (name.isNotEmpty() && invested > 0) {
                                onConfirm(name, type, invested, invested) // Current value starts as invested
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
fun UpdateInvestmentDialog(
    investment: InvestmentEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    onDelete: () -> Unit
) {
    var valueText by rememberSaveable { mutableStateOf("") }

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
                    text = "Update ${investment.name}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                 Text(
                    text = "Initial Investment: ${CurrencyFormatter.format(investment.amountInvested)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(SpacingTokens.Medium))

                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = { Text("Current Market Value") },
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

                Spacer(modifier = Modifier.height(SpacingTokens.Large))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = ColorTokens.Error500.copy(alpha = 0.7f))
                    }
                    
                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val newValue = valueText.toDoubleOrNull()
                                if (newValue != null && newValue >= 0) {
                                    onConfirm(newValue)
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



