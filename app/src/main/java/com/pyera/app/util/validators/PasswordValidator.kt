package com.pyera.app.util.validators

import com.pyera.app.util.ValidationUtils

/**
 * Validator for passwords with strength checking
 */
object PasswordValidator {

    private const val MIN_LENGTH = 8
    private const val MAX_LENGTH = 128
    private const val MIN_ENTROPY_BITS = 30

    /**
     * Password strength levels
     */
    enum class Strength {
        VERY_WEAK,
        WEAK,
        MEDIUM,
        STRONG,
        VERY_STRONG
    }

    /**
     * Validates a password and returns detailed result
     * @param password The password to validate
     * @return ValidationResult with specific error message or Success
     */
    fun validate(password: String): ValidationUtils.ValidationResult {
        return when {
            password.isBlank() -> 
                ValidationUtils.ValidationResult.Error("Password cannot be empty")
            
            password.length < MIN_LENGTH -> 
                ValidationUtils.ValidationResult.Error("Password must be at least $MIN_LENGTH characters")
            
            password.length > MAX_LENGTH -> 
                ValidationUtils.ValidationResult.Error("Password is too long (max $MAX_LENGTH characters)")
            
            password.contains(" ") -> 
                ValidationUtils.ValidationResult.Error("Password cannot contain spaces")
            
            !hasUppercase(password) -> 
                ValidationUtils.ValidationResult.Error("Password must contain at least one uppercase letter")
            
            !hasLowercase(password) -> 
                ValidationUtils.ValidationResult.Error("Password must contain at least one lowercase letter")
            
            !hasDigit(password) -> 
                ValidationUtils.ValidationResult.Error("Password must contain at least one digit")
            
            !hasSpecialChar(password) -> 
                ValidationUtils.ValidationResult.Error("Password must contain at least one special character (!@#$%^&*)")
            
            hasSequentialChars(password) -> 
                ValidationUtils.ValidationResult.Error("Password cannot contain sequential characters (123, abc)")
            
            hasRepeatedChars(password) -> 
                ValidationUtils.ValidationResult.Error("Password cannot contain repeated characters (aaa, 111)")
            
            isCommonPassword(password) -> 
                ValidationUtils.ValidationResult.Error("Password is too common. Please choose a more unique password")
            
            else -> ValidationUtils.ValidationResult.Success
        }
    }

    /**
     * Calculates password strength without rejecting
     * @param password The password to evaluate
     * @return Strength level
     */
    fun calculateStrength(password: String): Strength {
        if (password.isBlank()) return Strength.VERY_WEAK
        
        var score = 0
        
        // Length scoring
        when {
            password.length >= 16 -> score += 4
            password.length >= 12 -> score += 3
            password.length >= 10 -> score += 2
            password.length >= MIN_LENGTH -> score += 1
        }
        
        // Character variety scoring
        if (hasUppercase(password)) score++
        if (hasLowercase(password)) score++
        if (hasDigit(password)) score++
        if (hasSpecialChar(password)) score++
        
        // Entropy bonus
        if (calculateEntropy(password) > 50) score++
        
        // Penalties
        if (hasSequentialChars(password)) score -= 2
        if (hasRepeatedChars(password)) score -= 2
        if (isCommonPassword(password)) score -= 3
        
        return when {
            score >= 8 -> Strength.VERY_STRONG
            score >= 6 -> Strength.STRONG
            score >= 4 -> Strength.MEDIUM
            score >= 2 -> Strength.WEAK
            else -> Strength.VERY_WEAK
        }
    }

    /**
     * Quick validation for minimum requirements
     */
    fun meetsMinimumRequirements(password: String): Boolean {
        return password.length >= MIN_LENGTH &&
               hasUppercase(password) &&
               hasLowercase(password) &&
               hasDigit(password) &&
               hasSpecialChar(password)
    }

    private fun hasUppercase(password: String): Boolean = password.any { it.isUpperCase() }
    private fun hasLowercase(password: String): Boolean = password.any { it.isLowerCase() }
    private fun hasDigit(password: String): Boolean = password.any { it.isDigit() }
    private fun hasSpecialChar(password: String): Boolean = 
        password.any { it in "!@#$%^&*()_+-=[]{}|;':\",./<>?" }

    private fun hasSequentialChars(password: String): Boolean {
        val lower = password.lowercase()
        val sequences = listOf(
            "abcdefghijklmnopqrstuvwxyz",
            "0123456789",
            "qwertyuiop",
            "asdfghjkl",
            "zxcvbnm"
        )
        
        for (seq in sequences) {
            for (i in 0 until seq.length - 2) {
                if (lower.contains(seq.substring(i, i + 3))) return true
            }
        }
        return false
    }

    private fun hasRepeatedChars(password: String): Boolean {
        val regex = Regex("(.)\\1{2,}") // 3 or more same characters
        return regex.containsMatchIn(password)
    }

    private fun isCommonPassword(password: String): Boolean {
        val commonPasswords = setOf(
            "password123", "password1", "12345678", "qwerty123",
            "letmein1", "welcome1", "admin123", "login123",
            "pass1234", "abc12345", "iloveyou1", "monkey123",
            "football1", "baseball1", "dragon123", "master123",
            "sunshine1", "princess1", "baseball1", "football1"
        )
        return password.lowercase() in commonPasswords
    }

    private fun calculateEntropy(password: String): Double {
        var poolSize = 0
        if (password.any { it.isLowerCase() }) poolSize += 26
        if (password.any { it.isUpperCase() }) poolSize += 26
        if (password.any { it.isDigit() }) poolSize += 10
        if (password.any { !it.isLetterOrDigit() }) poolSize += 32
        
        return password.length * kotlin.math.log2(poolSize.toDouble())
    }

    /**
     * Validates that confirmation password matches
     */
    fun validateConfirmation(password: String, confirmation: String): ValidationUtils.ValidationResult {
        return when {
            confirmation.isBlank() -> 
                ValidationUtils.ValidationResult.Error("Please confirm your password")
            password != confirmation -> 
                ValidationUtils.ValidationResult.Error("Passwords do not match")
            else -> ValidationUtils.ValidationResult.Success
        }
    }
}
