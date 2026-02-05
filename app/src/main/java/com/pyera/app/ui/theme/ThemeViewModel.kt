package com.pyera.app.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.preferences.ThemeMode
import com.pyera.app.data.preferences.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {
    
    val themeMode: StateFlow<ThemeMode> = themePreferences.themeModeFlow
    
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.themeMode = mode
        }
    }
    
    fun getThemeModeDisplayName(mode: ThemeMode): String {
        return when (mode) {
            ThemeMode.LIGHT -> "Light"
            ThemeMode.DARK -> "Dark"
            ThemeMode.SYSTEM -> "System Default"
        }
    }
}
