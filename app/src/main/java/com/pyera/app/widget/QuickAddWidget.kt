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
 * Quick Add Widget (3x1)
 * Quick add income/expense buttons with current balance
 */
class QuickAddWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = WidgetPreferences(context)
        val balanceData = WidgetDataProvider.getBalanceData(context, prefs.selectedAccount)

        provideContent {
            GlanceTheme {
                QuickAddWidgetContent(
                    balance = balanceData.totalBalance,
                    isDarkTheme = prefs.isDarkTheme
                )
            }
        }
    }
}

@Composable
private fun QuickAddWidgetContent(
    balance: Double,
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

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(16.dp)
            .padding(12.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            // Balance Section
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.Horizontal.Start
            ) {
                Text(
                    text = "Balance",
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
            
            Spacer(modifier = GlanceModifier.width(8.dp))
            
            // Add Income Button
            Box(
                modifier = GlanceModifier
                    .background(androidx.glance.ColorProvider(R.color.income_green))
                    .cornerRadius(8.dp)
                    .clickable(actionRunCallback<AddIncomeActionCallback>())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ Income",
                    style = TextStyle(
                        color = androidx.glance.ColorProvider(R.color.white),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            
            Spacer(modifier = GlanceModifier.width(8.dp))
            
            // Add Expense Button
            Box(
                modifier = GlanceModifier
                    .background(androidx.glance.ColorProvider(R.color.expense_red))
                    .cornerRadius(8.dp)
                    .clickable(actionRunCallback<AddExpenseActionCallback>())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ Expense",
                    style = TextStyle(
                        color = androidx.glance.ColorProvider(R.color.white),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

/**
 * Action callback to open add income screen
 */
class AddIncomeActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        WidgetDataProvider.openAddTransaction(context, "INCOME")
    }
}

/**
 * Action callback to open add expense screen
 */
class AddExpenseActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        WidgetDataProvider.openAddTransaction(context, "EXPENSE")
    }
}
