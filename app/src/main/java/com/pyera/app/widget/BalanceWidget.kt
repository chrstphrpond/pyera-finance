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
 * Balance Widget (2x1)
 * Shows total balance with income/expense summary
 */
class BalanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = WidgetPreferences(context)
        val balanceData = WidgetDataProvider.getBalanceData(context, prefs.selectedAccount)

        provideContent {
            GlanceTheme {
                BalanceWidgetContent(
                    balance = balanceData.totalBalance,
                    income = balanceData.monthlyIncome,
                    expense = balanceData.monthlyExpense,
                    isDarkTheme = prefs.isDarkTheme
                )
            }
        }
    }
}

@Composable
private fun BalanceWidgetContent(
    balance: Double,
    income: Double,
    expense: Double,
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
    
    val secondaryTextColor = if (isDarkTheme) {
        androidx.glance.ColorProvider(R.color.widget_text_secondary_dark)
    } else {
        androidx.glance.ColorProvider(R.color.widget_text_secondary_light)
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(16.dp)
            .clickable(actionRunCallback<OpenAppActionCallback>())
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // Balance Title
            Text(
                text = "Total Balance",
                style = TextStyle(
                    color = secondaryTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            // Balance Amount
            Text(
                text = WidgetDataProvider.formatCurrency(balance),
                style = TextStyle(
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            // Income/Expense Row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.Start
            ) {
                // Income
                Column {
                    Text(
                        text = "↑ ${WidgetDataProvider.formatCurrency(income)}",
                        style = TextStyle(
                            color = androidx.glance.ColorProvider(R.color.income_green),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                
                Spacer(modifier = GlanceModifier.width(12.dp))
                
                // Expense
                Column {
                    Text(
                        text = "↓ ${WidgetDataProvider.formatCurrency(expense)}",
                        style = TextStyle(
                            color = androidx.glance.ColorProvider(R.color.expense_red),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

/**
 * Action callback to open the main app
 */
class OpenAppActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        WidgetDataProvider.openApp(context)
    }
}
