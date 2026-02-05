package com.pyera.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.pyera.app.R

/**
 * Transactions Widget (4x2)
 * Shows recent 5 transactions with balance summary
 */
class TransactionsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = WidgetPreferences(context)
        val balanceData = WidgetDataProvider.getBalanceData(context, prefs.selectedAccount)
        val transactions = WidgetDataProvider.getRecentTransactions(context, prefs.selectedAccount, 5)

        provideContent {
            GlanceTheme {
                TransactionsWidgetContent(
                    balance = balanceData.totalBalance,
                    income = balanceData.monthlyIncome,
                    expense = balanceData.monthlyExpense,
                    transactions = transactions,
                    isDarkTheme = prefs.isDarkTheme
                )
            }
        }
    }
}

@Composable
private fun TransactionsWidgetContent(
    balance: Double,
    income: Double,
    expense: Double,
    transactions: List<WidgetTransaction>,
    isDarkTheme: Boolean
) {
    val backgroundColor = if (isDarkTheme) {
        androidx.glance.ColorProvider(R.color.widget_background_dark)
    } else {
        androidx.glance.ColorProvider(R.color.widget_background_light)
    }
    
    val textColor = if (isDarkTheme) {
        androidx.glance.ColorProvider(R.color.widget_text_primary_dark)
    } else {
        androidx.glance.ColorProvider(R.color.widget_text_primary_light)
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(16.dp)
            .padding(12.dp)
    ) {
        // Header with Balance
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            Column(
                modifier = GlanceModifier.defaultWeight()
            ) {
                Text(
                    text = "Total Balance",
                    style = TextStyle(
                        color = androidx.glance.ColorProvider(R.color.widget_text_secondary_dark),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = WidgetDataProvider.formatCurrency(balance),
                    style = TextStyle(
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            // Income/Expense Mini Summary
            Column(
                horizontalAlignment = Alignment.Horizontal.End
            ) {
                Text(
                    text = "+${WidgetDataProvider.formatCurrency(income)}",
                    style = TextStyle(
                        color = androidx.glance.ColorProvider(R.color.income_green),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = "-${WidgetDataProvider.formatCurrency(expense)}",
                    style = TextStyle(
                        color = androidx.glance.ColorProvider(R.color.expense_red),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Divider
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(androidx.glance.ColorProvider(R.color.widget_divider))
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Transactions List
        if (transactions.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recent transactions",
                    style = TextStyle(
                        color = androidx.glance.ColorProvider(R.color.widget_text_secondary_dark),
                        fontSize = 12.sp
                    )
                )
            }
        } else {
            LazyColumn {
                items(transactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: WidgetTransaction,
    isDarkTheme: Boolean
) {
    val textColor = if (isDarkTheme) {
        androidx.glance.ColorProvider(R.color.widget_text_primary_dark)
    } else {
        androidx.glance.ColorProvider(R.color.widget_text_primary_light)
    }
    
    val secondaryTextColor = if (isDarkTheme) {
        androidx.glance.ColorProvider(R.color.widget_text_secondary_dark)
    } else {
        androidx.glance.ColorProvider(R.color.widget_text_secondary_light)
    }
    
    val amountColor = if (transaction.type == "INCOME") {
        androidx.glance.ColorProvider(R.color.income_green)
    } else {
        androidx.glance.ColorProvider(R.color.expense_red)
    }
    
    val amountPrefix = if (transaction.type == "INCOME") "+" else "-"

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .clickable(actionRunCallback<OpenTransactionsActionCallback>())
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        // Transaction Icon/Category
        Box(
            modifier = GlanceModifier
                .width(32.dp)
                .height(32.dp)
                .background(androidx.glance.ColorProvider(R.color.widget_surface))
                .cornerRadius(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = transaction.categoryIcon?.take(1) ?: "💰",
                style = TextStyle(fontSize = 14.sp)
            )
        }
        
        Spacer(modifier = GlanceModifier.width(10.dp))
        
        // Transaction Details
        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = transaction.note.takeIf { it.isNotBlank() } ?: transaction.categoryName,
                style = TextStyle(
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.height(1.dp))
            Text(
                text = transaction.date,
                style = TextStyle(
                    color = secondaryTextColor,
                    fontSize = 10.sp
                )
            )
        }
        
        // Amount
        Text(
            text = "$amountPrefix${WidgetDataProvider.formatCurrency(transaction.amount)}",
            style = TextStyle(
                color = amountColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

/**
 * Action callback to open transactions list
 */
class OpenTransactionsActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        WidgetDataProvider.openTransactionsList(context)
    }
}
