package com.pyera.app.util

object ValidationUtils {
    
    private const val MAX_NOTE_LENGTH = 500
    private const val MAX_CATEGORY_NAME_LENGTH = 50
    private const val MAX_AMOUNT = 999_999_999.0
    
    fun validateTransactionNote(note: String): ValidationResult {
        return when {
            note.length > MAX_NOTE_LENGTH -> 
                ValidationResult.Error("Note too long (max $MAX_NOTE_LENGTH characters)")
            else -> ValidationResult.Success
        }
    }
    
    fun validateAmount(amount: Double): ValidationResult {
        return when {
            amount < 0 -> ValidationResult.Error("Amount cannot be negative")
            amount > MAX_AMOUNT -> ValidationResult.Error("Amount exceeds maximum limit")
            else -> ValidationResult.Success
        }
    }
    
    fun validateCategoryName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Name cannot be empty")
            name.length > MAX_CATEGORY_NAME_LENGTH -> 
                ValidationResult.Error("Name too long (max $MAX_CATEGORY_NAME_LENGTH characters)")
            !name.matches(Regex("^[a-zA-Z0-9\\s\\-_]+$")) -> 
                ValidationResult.Error("Name contains invalid characters")
            else -> ValidationResult.Success
        }
    }
    
    fun validateBudgetName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Budget name cannot be empty")
            name.length > MAX_CATEGORY_NAME_LENGTH -> 
                ValidationResult.Error("Name too long (max $MAX_CATEGORY_NAME_LENGTH characters)")
            else -> ValidationResult.Success
        }
    }
    
    fun validatePercentage(percentage: Int): ValidationResult {
        return when {
            percentage < 0 -> ValidationResult.Error("Percentage cannot be negative")
            percentage > 100 -> ValidationResult.Error("Percentage cannot exceed 100")
            else -> ValidationResult.Success
        }
    }
    
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}
