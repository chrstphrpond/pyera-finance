package com.pyera.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.biometric.BiometricAuthManager
import com.pyera.app.data.repository.AuthRepository
import com.pyera.app.data.repository.TransactionRepository
import com.pyera.app.data.repository.BudgetRepository
import com.pyera.app.data.repository.SavingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import javax.inject.Inject

data class ProfileState(
    val userName: String = "Pyera User",
    val email: String = "user@example.com",
    val avatarInitials: String = "PU",
    val totalTransactions: Int = 0,
    val totalSavings: Double = 0.0,
    val budgetStatus: String = "Active",
    val notificationsEnabled: Boolean = true,
    val currency: String = "PHP ₱",
    val appearance: String = "Dark",
    val appVersion: String = "1.0.0",
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnabled: Boolean = false
)

sealed class ProfileEvent {
    data object Logout : ProfileEvent()
    data object ExportData : ProfileEvent()
    data class ToggleNotifications(val enabled: Boolean) : ProfileEvent()
    data class ToggleBiometric(val enabled: Boolean) : ProfileEvent()
    data object NavigateToAccountSettings : ProfileEvent()
    data object NavigateToDataPrivacy : ProfileEvent()
    data object NavigateToHelpSupport : ProfileEvent()
    data object NavigateToAbout : ProfileEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    savingsRepository: SavingsRepository,
    budgetRepository: BudgetRepository,
    private val authRepository: AuthRepository,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {

    private val _notificationsEnabled = MutableStateFlow(true)

    val state: StateFlow<ProfileState> = combine(
        transactionRepository.getAllTransactions(),
        savingsRepository.getAllSavingsGoals(),
        budgetRepository.getActiveBudgetsForUser(authRepository.currentUser?.uid ?: ""),
        _notificationsEnabled
    ) { transactions, savingsGoals, budgets, notifications ->
        val totalSavings = savingsGoals.sumOf { it.currentAmount }
        val budgetStatus = when {
            budgets.isEmpty() -> "No Budget"
            else -> "Active"
        }
        
        ProfileState(
            userName = authRepository.currentUser?.displayName ?: "Pyera User",
            email = authRepository.currentUser?.email ?: "user@example.com",
            avatarInitials = getInitials(authRepository.currentUser?.displayName ?: "Pyera User"),
            totalTransactions = transactions.size,
            totalSavings = totalSavings,
            budgetStatus = budgetStatus,
            notificationsEnabled = notifications,
            currency = "PHP ₱",
            appearance = "Dark",
            appVersion = "1.0.0",
            isBiometricAvailable = biometricAuthManager.isBiometricAvailable(),
            isBiometricEnabled = authRepository.isBiometricEnabled()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileState(
            isBiometricAvailable = biometricAuthManager.isBiometricAvailable(),
            isBiometricEnabled = authRepository.isBiometricEnabled()
        )
    )

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.Logout -> {
                viewModelScope.launch {
                    // Handle logout logic
                    authRepository.logout()
                }
            }
            is ProfileEvent.ExportData -> {
                viewModelScope.launch {
                    // Handle data export logic
                    exportUserData()
                }
            }
            is ProfileEvent.ToggleNotifications -> {
                _notificationsEnabled.value = event.enabled
                // Save notification preference
            }
            is ProfileEvent.ToggleBiometric -> {
                toggleBiometric(event.enabled)
            }
            else -> {
                // Handle navigation events in the UI layer
            }
        }
    }

    private fun toggleBiometric(enabled: Boolean) {
        if (enabled) {
            // Enabling biometric - credentials should already be stored from login
            authRepository.setBiometricEnabled(true)
        } else {
            // Disabling biometric - clear stored credentials
            authRepository.setBiometricEnabled(false)
            authRepository.clearStoredCredentials()
        }
    }

    private suspend fun exportUserData(): String {
        // Implementation for exporting user data
        // Returns a JSON string or file path
        return ""
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    private fun getInitials(name: String): String {
        return name.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .ifEmpty { "PU" }
    }
}
