package com.pyera.app.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.RecurringFrequency
import com.pyera.app.data.local.entity.RecurringTransactionEntity
import com.pyera.app.data.local.entity.TransactionType
import com.pyera.app.data.repository.RecurringTransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Data class representing the UI state for recurring transactions.
 */
data class RecurringTransactionsUiState(
    val recurringTransactions: List<RecurringTransactionEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Data class representing the form state for adding/editing recurring transactions.
 */
data class RecurringFormState(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long? = null,
    val description: String = "",
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val startDate: Long = System.currentTimeMillis(),
    val hasEndDate: Boolean = false,
    val endDate: Long? = null,
    val isActive: Boolean = true
) {
    fun isValid(): Boolean {
        val amountValue = amount.toDoubleOrNull()
        return amountValue != null && 
               amountValue > 0 && 
               description.isNotBlank() &&
               categoryId != null
    }

    fun toEntity(id: Long = 0): RecurringTransactionEntity {
        return RecurringTransactionEntity(
            id = id,
            amount = amount.toDoubleOrNull() ?: 0.0,
            type = type,
            categoryId = categoryId,
            description = description,
            frequency = frequency,
            startDate = startDate,
            endDate = if (hasEndDate) endDate else null,
            nextDueDate = startDate,
            isActive = isActive
        )
    }

    companion object {
        fun fromEntity(entity: RecurringTransactionEntity): RecurringFormState {
            return RecurringFormState(
                amount = entity.amount.toString(),
                type = entity.type,
                categoryId = entity.categoryId,
                description = entity.description,
                frequency = entity.frequency,
                startDate = entity.startDate,
                hasEndDate = entity.endDate != null,
                endDate = entity.endDate,
                isActive = entity.isActive
            )
        }
    }
}

@HiltViewModel
class RecurringTransactionsViewModel @Inject constructor(
    private val repository: RecurringTransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringTransactionsUiState())
    val uiState: StateFlow<RecurringTransactionsUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(RecurringFormState())
    val formState: StateFlow<RecurringFormState> = _formState.asStateFlow()

    init {
        loadRecurringTransactions()
    }

    /**
     * Load all recurring transactions from the repository.
     */
    private fun loadRecurringTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.getAllRecurring().collect { transactions ->
                    _uiState.update {
                        it.copy(
                            recurringTransactions = transactions,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load recurring transactions: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Add a new recurring transaction.
     */
    fun addRecurring(state: RecurringFormState) {
        if (!state.isValid()) {
            _uiState.update { it.copy(errorMessage = "Please fill in all required fields") }
            return
        }

        viewModelScope.launch {
            try {
                repository.addRecurring(state.toEntity())
                _uiState.update { it.copy(errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to add recurring transaction: ${e.message}")
                }
            }
        }
    }

    /**
     * Update an existing recurring transaction.
     */
    fun updateRecurring(id: Long, state: RecurringFormState) {
        if (!state.isValid()) {
            _uiState.update { it.copy(errorMessage = "Please fill in all required fields") }
            return
        }

        viewModelScope.launch {
            try {
                repository.updateRecurring(state.toEntity(id))
                _uiState.update { it.copy(errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to update recurring transaction: ${e.message}")
                }
            }
        }
    }

    /**
     * Delete a recurring transaction.
     */
    fun deleteRecurring(entity: RecurringTransactionEntity) {
        viewModelScope.launch {
            try {
                repository.deleteRecurring(entity)
                _uiState.update { it.copy(errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to delete recurring transaction: ${e.message}")
                }
            }
        }
    }

    /**
     * Delete a recurring transaction by ID.
     */
    fun deleteRecurringById(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteRecurringById(id)
                _uiState.update { it.copy(errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to delete recurring transaction: ${e.message}")
                }
            }
        }
    }

    /**
     * Toggle the active status of a recurring transaction.
     */
    fun toggleActiveStatus(id: Long, isActive: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleActiveStatus(id, isActive)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to update status: ${e.message}")
                }
            }
        }
    }

    /**
     * Load a recurring transaction for editing.
     */
    fun loadRecurringForEdit(id: Long) {
        viewModelScope.launch {
            try {
                val entity = repository.getRecurringById(id)
                entity?.let {
                    _formState.value = RecurringFormState.fromEntity(it)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to load recurring transaction: ${e.message}")
                }
            }
        }
    }

    /**
     * Reset the form state to default values.
     */
    fun resetFormState() {
        _formState.value = RecurringFormState()
    }

    /**
     * Update the form state.
     */
    fun updateFormState(update: (RecurringFormState) -> RecurringFormState) {
        _formState.update(update)
    }

    /**
     * Clear any error messages.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Get a human-readable string for the frequency.
     */
    fun getFrequencyLabel(frequency: RecurringFrequency): String {
        return when (frequency) {
            RecurringFrequency.DAILY -> "Daily"
            RecurringFrequency.WEEKLY -> "Weekly"
            RecurringFrequency.BIWEEKLY -> "Bi-weekly"
            RecurringFrequency.MONTHLY -> "Monthly"
            RecurringFrequency.QUARTERLY -> "Quarterly"
            RecurringFrequency.YEARLY -> "Yearly"
        }
    }

    /**
     * Get a human-readable string for the next due date.
     */
    fun getNextDueLabel(nextDueDate: Long): String {
        val now = System.currentTimeMillis()
        val diff = nextDueDate - now
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            days < 0 -> "Overdue"
            days == 0L -> "Today"
            days == 1L -> "Tomorrow"
            days < 7 -> "In $days days"
            days < 30 -> "In ${days / 7} weeks"
            else -> {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = nextDueDate
                val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, java.util.Locale.getDefault())
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                "$month $day"
            }
        }
    }
}
