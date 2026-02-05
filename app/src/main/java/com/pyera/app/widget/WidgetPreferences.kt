package com.pyera.app.widget

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Preferences for widget configuration
 */
class WidgetPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, 
        Context.MODE_PRIVATE
    )
    
    var selectedAccount: String?
        get() = prefs.getString(KEY_SELECTED_ACCOUNT, null)
        set(value) = prefs.edit { putString(KEY_SELECTED_ACCOUNT, value) }
    
    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, true)
        set(value) = prefs.edit { putBoolean(KEY_DARK_THEME, value) }
    
    var updateFrequencyMinutes: Int
        get() = prefs.getInt(KEY_UPDATE_FREQUENCY, 30)
        set(value) = prefs.edit { putInt(KEY_UPDATE_FREQUENCY, value) }
    
    var showBalance: Boolean
        get() = prefs.getBoolean(KEY_SHOW_BALANCE, true)
        set(value) = prefs.edit { putBoolean(KEY_SHOW_BALANCE, value) }
    
    companion object {
        private const val PREFS_NAME = "pyera_widget_prefs"
        private const val KEY_SELECTED_ACCOUNT = "selected_account"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_UPDATE_FREQUENCY = "update_frequency"
        private const val KEY_SHOW_BALANCE = "show_balance"
        
        // Widget update actions
        const val ACTION_WIDGET_UPDATE = "com.pyera.app.ACTION_WIDGET_UPDATE"
        const val ACTION_TRANSACTION_ADDED = "com.pyera.app.ACTION_TRANSACTION_ADDED"
    }
}
