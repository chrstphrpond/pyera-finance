package com.pyera.app.util.validators

import android.util.Patterns
import com.pyera.app.util.ValidationUtils

/**
 * Validator for email addresses with comprehensive validation rules
 */
object EmailValidator {

    private const val MAX_EMAIL_LENGTH = 254
    private const val MAX_LOCAL_PART_LENGTH = 64

    /**
     * Validates an email address
     * @param email The email address to validate
     * @return ValidationResult indicating success or specific error
     */
    fun validate(email: String): ValidationUtils.ValidationResult {
        return when {
            email.isBlank() -> 
                ValidationUtils.ValidationResult.Error("Email cannot be empty")
            
            email.length > MAX_EMAIL_LENGTH -> 
                ValidationUtils.ValidationResult.Error("Email is too long (max $MAX_EMAIL_LENGTH characters)")
            
            email.contains("..") -> 
                ValidationUtils.ValidationResult.Error("Email cannot contain consecutive dots")
            
            email.startsWith(".") || email.endsWith(".") -> 
                ValidationUtils.ValidationResult.Error("Email cannot start or end with a dot")
            
            !isValidLocalPart(email) -> 
                ValidationUtils.ValidationResult.Error("Invalid email format")
            
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                ValidationUtils.ValidationResult.Error("Invalid email format")
            
            isDisposableEmail(email) -> 
                ValidationUtils.ValidationResult.Error("Disposable email addresses are not allowed")
            
            else -> ValidationUtils.ValidationResult.Success
        }
    }

    /**
     * Validates the local part (before @) of the email
     */
    private fun isValidLocalPart(email: String): Boolean {
        val atIndex = email.indexOf('@')
        if (atIndex == -1) return false
        
        val localPart = email.substring(0, atIndex)
        if (localPart.length > MAX_LOCAL_PART_LENGTH) return false
        
        // Check for valid characters in local part
        val validLocalPartRegex = Regex("^[a-zA-Z0-9!#\$%&'*+/=?^_`{|}~.-]+$")
        return validLocalPartRegex.matches(localPart)
    }

    /**
     * Checks if the email is from a disposable email provider
     */
    private fun isDisposableEmail(email: String): Boolean {
        val domain = email.substringAfter("@", "").lowercase()
        val disposableDomains = setOf(
            "tempmail.com", "throwaway.com", "mailinator.com",
            "guerrillamail.com", "sharklasers.com", "spam4.me",
            "trashmail.com", "yopmail.com", "temp.inbox.com",
            "mailnesia.com", "tempmailaddress.com", "burnermail.io"
        )
        return domain in disposableDomains
    }

    /**
     * Checks if email format is valid without strict validation
     * Useful for quick client-side validation
     */
    fun isValidFormat(email: String): Boolean {
        return email.isNotBlank() && 
               Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
