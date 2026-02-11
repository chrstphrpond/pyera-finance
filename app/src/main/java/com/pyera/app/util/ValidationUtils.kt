package com.pyera.app.util

object ValidationUtils {
    
    private const val MAX_NOTE_LENGTH = 500
    private const val MAX_CATEGORY_NAME_LENGTH = 50
    private const val MAX_AMOUNT = 999_999_999.0
    private val INVALID_DESCRIPTION_CHARS = Regex("[<>\"']")
    
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

    fun validateTransactionAmount(amount: String): ValidationResult {
        val parsed = amount.toDoubleOrNull()
        return when {
            amount.isBlank() -> ValidationResult.Error("Amount is required")
            parsed == null -> ValidationResult.Error("Invalid amount format")
            parsed <= 0 -> ValidationResult.Error("Amount must be greater than 0")
            parsed > MAX_AMOUNT -> ValidationResult.Error("Amount exceeds maximum")
            else -> ValidationResult.Success
        }
    }

    fun validateTransactionDescription(description: String): ValidationResult {
        return when {
            description.isBlank() -> ValidationResult.Error("Description is required")
            description.length > MAX_NOTE_LENGTH -> ValidationResult.Error("Description too long (max $MAX_NOTE_LENGTH chars)")
            description.contains(INVALID_DESCRIPTION_CHARS) -> ValidationResult.Error("Invalid characters in description")
            else -> ValidationResult.Success
        }
    }

    fun validateTransactionCategory(categoryId: Long?): ValidationResult {
        return when {
            categoryId == null || categoryId <= 0 -> ValidationResult.Error("Category is required")
            else -> ValidationResult.Success
        }
    }

    fun validateTransaction(
        amount: String,
        description: String,
        categoryId: Long?
    ): ValidationResult {
        val errors = mutableListOf<String>()

        val amountResult = validateTransactionAmount(amount)
        if (amountResult is ValidationResult.Error) errors.add(amountResult.message)

        val descriptionResult = validateTransactionDescription(description)
        if (descriptionResult is ValidationResult.Error) errors.add(descriptionResult.message)

        val categoryResult = validateTransactionCategory(categoryId)
        if (categoryResult is ValidationResult.Error) errors.add(categoryResult.message)

        return if (errors.isEmpty()) ValidationResult.Success
        else ValidationResult.Error(errors.joinToString(", "))
    }

    fun validateBudgetAmount(amount: String): ValidationResult {
        val parsed = amount.toDoubleOrNull()
        return when {
            amount.isBlank() -> ValidationResult.Error("Amount is required")
            parsed == null -> ValidationResult.Error("Invalid amount format")
            parsed <= 0 -> ValidationResult.Error("Budget amount must be greater than 0")
            parsed > MAX_AMOUNT -> ValidationResult.Error("Budget amount too large")
            else -> ValidationResult.Success
        }
    }

    fun validateBudgetCategory(categoryId: Int?): ValidationResult {
        return when {
            categoryId == null || categoryId <= 0 -> ValidationResult.Error("Category is required")
            else -> ValidationResult.Success
        }
    }

    fun validateAlertThreshold(alertThreshold: Int): ValidationResult {
        return when {
            alertThreshold !in 50..95 -> ValidationResult.Error("Alert threshold must be between 50% and 95%")
            else -> ValidationResult.Success
        }
    }

    fun validateBudget(
        amount: Double,
        categoryId: Int?,
        alertThreshold: Int
    ): ValidationResult {
        val errors = mutableListOf<String>()

        when {
            amount <= 0 -> errors.add("Budget amount must be greater than 0")
            amount > MAX_AMOUNT -> errors.add("Budget amount too large")
        }

        if (categoryId == null || categoryId <= 0) errors.add("Category is required")
        if (alertThreshold !in 50..95) errors.add("Alert threshold must be between 50% and 95%")

        return if (errors.isEmpty()) ValidationResult.Success
        else ValidationResult.Error(errors.joinToString(", "))
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
