package com.pyera.app.ui.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.domain.repository.AuthRepository
import com.pyera.app.domain.repository.BudgetRepository
import com.pyera.app.domain.repository.SavingsRepository
import com.pyera.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val savingsRepository: SavingsRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _notificationsEnabled = MutableStateFlow(true)

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

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
        viewModelScope.launch {
            try {
                val transactions = transactionRepository.getTransactionsForExport()
                val csv = buildTransactionsCsv(transactions)
                val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
                    .format(System.currentTimeMillis())
                val fileName = "pyera-transactions-$timestamp.csv"
                _events.emit(ProfileEvent.ExportReady(fileName, csv))
            } catch (e: Exception) {
                _events.emit(ProfileEvent.ExportFailed(e.message ?: "Failed to export data"))
            }
        }
    }

    private fun buildTransactionsCsv(transactions: List<com.pyera.app.data.local.entity.TransactionEntity>): String {
        val header = listOf(
            "id",
            "date",
            "type",
            "amount",
            "note",
            "categoryId",
            "accountId",
            "userId",
            "isTransfer",
            "transferAccountId",
            "createdAt",
            "updatedAt"
        ).joinToString(",")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val rows = transactions.joinToString("\n") { tx ->
            listOf(
                tx.id.toString(),
                dateFormat.format(tx.date),
                tx.type,
                tx.amount.toString(),
                escapeCsv(tx.note),
                tx.categoryId?.toString().orEmpty(),
                tx.accountId.toString(),
                escapeCsv(tx.userId),
                tx.isTransfer.toString(),
                tx.transferAccountId?.toString().orEmpty(),
                dateFormat.format(tx.createdAt),
                dateFormat.format(tx.updatedAt)
            ).joinToString(",")
        }

        return if (rows.isBlank()) {
            header
        } else {
            "$header\n$rows"
        }
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n")
        if (!needsQuotes) return value
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}

sealed class ProfileEvent {
    data class ExportReady(val fileName: String, val csvContent: String) : ProfileEvent()
    data class ExportFailed(val message: String) : ProfileEvent()
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
