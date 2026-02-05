package com.pyera.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _themeModeFlow = MutableStateFlow(themeMode)
    val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()
    
    var themeMode: ThemeMode
        get() {
            val modeName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
            return try {
                ThemeMode.valueOf(modeName ?: ThemeMode.SYSTEM.name)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }
        set(value) {
            prefs.edit().putString(KEY_THEME_MODE, value.name).apply()
            _themeModeFlow.value = value
        }
    
    fun followSystem(): Boolean = themeMode == ThemeMode.SYSTEM
    
    fun isDarkTheme(systemIsDark: Boolean): Boolean {
        return when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> systemIsDark
        }
    }
    
    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}
