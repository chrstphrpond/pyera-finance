package com.pyera.app.ui.analysis

import com.pyera.app.ui.components.PyeraCard
import com.pyera.app.ui.theme.tokens.ColorTokens
import com.pyera.app.ui.theme.tokens.SpacingTokens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pyera.app.ui.theme.CardBackground
import com.pyera.app.ui.theme.GlassOverlay
import com.pyera.app.ui.util.CurrencyFormatter
import com.pyera.app.ui.util.pyeraBackground

@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pyeraBackground()
            .padding(SpacingTokens.Medium)
    ) {
        Text(
            text = "Analysis",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(SpacingTokens.Medium))

        // Basic Summary for now (replacing Charts temporary until I configure Vico properly)
        PyeraCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = SpacingTokens.Medium,
            containerColor = CardBackground,
            borderWidth = 0.dp
        ) {
            Column(modifier = Modifier.padding(SpacingTokens.Medium)) {
                Text("Total Expenses", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Text(
                    text = CurrencyFormatter.format(state.totalExpense),
                    color = ColorTokens.Primary500,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(SpacingTokens.Small))
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(SpacingTokens.Small))
                Text("Predicted Next Month", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f), fontSize = 12.sp)
                Text(
                    text = CurrencyFormatter.format(state.predictedExpense),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(SpacingTokens.Medium))
        
        Button(
            onClick = { viewModel.exportData() },
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorTokens.Primary500,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export Data to CSV")
        }

        state.exportMessage?.let { message ->
            Text(
                text = message,
                color = ColorTokens.Primary500,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(SpacingTokens.Large))
        
        Text(
            text = "Expense by Category",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(SpacingTokens.Medium))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(SpacingTokens.Small)) {
            items(state.expensesByCategory) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GlassOverlay, RoundedCornerShape(8.dp))
                        .padding(SpacingTokens.MediumSmall),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(androidx.compose.ui.graphics.Color(item.color), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item.categoryName, color = MaterialTheme.colorScheme.onBackground)
                    }
                    Text(
                        text = CurrencyFormatter.format(item.amount),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}




