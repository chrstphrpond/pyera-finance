package com.pyera.app.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.TransactionEntity

import com.pyera.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _validationState = MutableStateFlow(ValidationState())
    val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()
    
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun validateAmount(amount: String): Boolean {
        return when {
            amount.isBlank() -> {
                _validationState.update { it.copy(amountError = "Amount is required") }
                false
            }
            amount.toDoubleOrNull() == null -> {
                _validationState.update { it.copy(amountError = "Invalid amount") }
                false
            }
            amount.toDouble() <= 0 -> {
                _validationState.update { it.copy(amountError = "Amount must be greater than 0") }
                false
            }
            amount.toDouble() > 999999999.99 -> {
                _validationState.update { it.copy(amountError = "Amount is too large") }
                false
            }
            else -> {
                _validationState.update { it.copy(amountError = null) }
                true
            }
        }
    }
    
    fun validateDescription(description: String): Boolean {
        return when {
            description.isBlank() -> {
                _validationState.update { it.copy(descriptionError = "Description is required") }
                false
            }
            description.length > 200 -> {
                _validationState.update { it.copy(descriptionError = "Description is too long") }
                false
            }
            else -> {
                _validationState.update { it.copy(descriptionError = null) }
                true
            }
        }
    }
    
    fun validateCategory(categoryId: Long): Boolean {
        return when {
            categoryId <= 0 -> {
                _validationState.update { it.copy(categoryError = "Please select a category") }
                false
            }
            else -> {
                _validationState.update { it.copy(categoryError = null) }
                true
            }
        }
    }
    
    fun saveTransaction(
        amount: String,
        description: String,
        categoryId: Long,
        type: String,
        date: Long,
        userId: String = "current_user"
    ) {
        val isAmountValid = validateAmount(amount)
        val isDescriptionValid = validateDescription(description)
        val isCategoryValid = validateCategory(categoryId)
        
        if (!isAmountValid || !isDescriptionValid || !isCategoryValid) {
            return
        }
        
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            try {
                transactionRepository.insertTransaction(
                    TransactionEntity(
                        amount = amount.toDouble(),
                        note = description.trim(),
                        categoryId = categoryId.toInt(),
                        type = type,
                        date = date
                    )
                )
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Failed to save transaction")
            }
        }
    }
    
    fun resetValidation() {
        _validationState.value = ValidationState()
    }
    
    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}

data class ValidationState(
    val amountError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null
)

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}
