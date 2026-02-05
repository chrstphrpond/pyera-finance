package com.pyera.app.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.pyera.app.ui.theme.PyeraTheme
import kotlinx.coroutines.launch

/**
 * Configuration Activity for Pyera Widgets
 */
class WidgetConfigurationActivity : ComponentActivity() {
    
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get the widget ID from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        // If the widget ID is invalid, finish the activity
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        setContent {
            PyeraTheme {
                WidgetConfigurationScreen(
                    appWidgetId = appWidgetId,
                    onConfigured = { result ->
                        setResult(result)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigurationScreen(
    appWidgetId: Int,
    onConfigured: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { WidgetPreferences(context) }
    
    var selectedTheme by remember { mutableStateOf(if (prefs.isDarkTheme) "dark" else "light") }
    var updateFrequency by remember { mutableIntStateOf(prefs.updateFrequencyMinutes) }
    var showBalance by remember { mutableStateOf(prefs.showBalance) }
    var selectedAccount by remember { mutableStateOf(prefs.selectedAccount ?: "all") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widget Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Selection
            ConfigurationCard(title = "Theme") {
                Column {
                    ThemeOption(
                        label = "Dark Theme",
                        selected = selectedTheme == "dark",
                        onSelect = { selectedTheme = "dark" }
                    )
                    ThemeOption(
                        label = "Light Theme",
                        selected = selectedTheme == "light",
                        onSelect = { selectedTheme = "light" }
                    )
                }
            }
            
            // Account Selection
            ConfigurationCard(title = "Account") {
                Column {
                    AccountOption(
                        label = "All Accounts",
                        selected = selectedAccount == "all",
                        onSelect = { selectedAccount = "all" }
                    )
                    AccountOption(
                        label = "Main Account",
                        selected = selectedAccount == "main",
                        onSelect = { selectedAccount = "main" }
                    )
                }
            }
            
            // Update Frequency
            ConfigurationCard(title = "Update Frequency") {
                Column {
                    Text(
                        text = "${updateFrequency} minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Slider(
                        value = updateFrequency.toFloat(),
                        onValueChange = { updateFrequency = it.toInt() },
                        valueRange = 15f..120f,
                        steps = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Show Balance Toggle
            ConfigurationCard(title = "Display Options") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Show Balance")
                    Switch(
                        checked = showBalance,
                        onCheckedChange = { showBalance = it }
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save Button
            Button(
                onClick = {
                    scope.launch {
                        // Save preferences
                        prefs.isDarkTheme = selectedTheme == "dark"
                        prefs.updateFrequencyMinutes = updateFrequency
                        prefs.showBalance = showBalance
                        prefs.selectedAccount = selectedAccount
                        
                        // Update the widget
                        val manager = GlanceAppWidgetManager(context)
                        val glanceId = manager.getGlanceIdBy(appWidgetId)
                        
                        glanceId?.let { id ->
                            // Determine widget type and update
                            when {
                                manager.getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) < 180 -> {
                                    BalanceWidget().update(context, id)
                                }
                                manager.getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) < 250 -> {
                                    QuickAddWidget().update(context, id)
                                }
                                else -> {
                                    TransactionsWidget().update(context, id)
                                }
                            }
                        }
                        
                        // Schedule updates
                        WidgetUpdateWorker.schedule(context, updateFrequency)
                        
                        onConfigured(Activity.RESULT_OK)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun ConfigurationCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label)
    }
}

@Composable
private fun AccountOption(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label)
    }
}
