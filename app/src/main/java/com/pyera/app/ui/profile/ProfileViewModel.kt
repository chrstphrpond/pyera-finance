package com.pyera.app.ui.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.repository.AuthRepository
import com.pyera.app.data.repository.BudgetRepository
import com.pyera.app.data.repository.SavingsRepository
import com.pyera.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val savingsRepository: SavingsRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _notificationsEnabled = MutableStateFlow(true)

    val state: StateFlow<ProfileState> = combine(
        transactionRepository.getAllTransactions()
            .distinctUntilChanged(),
        savingsRepository.getAllSavingsGoals()
            .distinctUntilChanged(),
        budgetRepository.getActiveBudgetsForUser(authRepository.currentUser?.uid ?: "")
            .distinctUntilChanged(),
        _notificationsEnabled
    ) { transactions, savingsGoals, activeBudgets, notifications ->
        ProfileState(
            userName = authRepository.currentUser?.displayName ?: "",
            email = authRepository.currentUser?.email ?: "",
            avatarUrl = authRepository.currentUser?.photoUrl?.toString(),
            transactionCount = transactions.size,
            savingsGoalsCount = savingsGoals.size,
            activeBudgetsCount = activeBudgets.size,
            notificationsEnabled = notifications,
            appVersion = "1.0.0" // Replace with actual version
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileState()
    )

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun exportData() {
        // TODO: Implement data export
    }
}

@Immutable
data class ProfileState(
    val userName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val transactionCount: Int = 0,
    val savingsGoalsCount: Int = 0,
    val activeBudgetsCount: Int = 0,
    val notificationsEnabled: Boolean = true,
    val appVersion: String = "1.0.0",
    val isLoading: Boolean = false
)
