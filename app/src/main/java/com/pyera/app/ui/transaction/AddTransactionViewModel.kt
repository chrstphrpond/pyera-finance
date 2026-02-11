package com.pyera.app.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.domain.repository.TransactionRepository
import com.pyera.app.util.ValidationUtils
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
        return when (val result = ValidationUtils.validateTransactionAmount(amount)) {
            is ValidationUtils.ValidationResult.Error -> {
                _validationState.update { it.copy(amountError = result.message) }
                false
            }
            ValidationUtils.ValidationResult.Success -> {
                _validationState.update { it.copy(amountError = null) }
                true
            }
        }
    }
    
    fun validateDescription(description: String): Boolean {
        return when (val result = ValidationUtils.validateTransactionDescription(description)) {
            is ValidationUtils.ValidationResult.Error -> {
                _validationState.update { it.copy(descriptionError = result.message) }
                false
            }
            ValidationUtils.ValidationResult.Success -> {
                _validationState.update { it.copy(descriptionError = null) }
                true
            }
        }
    }
    
    fun validateCategory(categoryId: Long): Boolean {
        return when (val result = ValidationUtils.validateTransactionCategory(categoryId)) {
            is ValidationUtils.ValidationResult.Error -> {
                _validationState.update { it.copy(categoryError = result.message) }
                false
            }
            ValidationUtils.ValidationResult.Success -> {
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
        accountId: Long,
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
                        date = date,
                        accountId = accountId,
                        userId = userId
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
