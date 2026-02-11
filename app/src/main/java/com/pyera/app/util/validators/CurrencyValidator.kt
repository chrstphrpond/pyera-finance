package com.pyera.app.util.validators

import com.pyera.app.util.ValidationUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import java.util.Currency
import java.util.Locale
import kotlin.math.pow

/**
 * Validator for currency amounts and currency codes
 */
object CurrencyValidator {

    private const val MAX_AMOUNT = 999_999_999.99
    private const val MIN_AMOUNT = -999_999_999.99
    private val SUPPORTED_CURRENCIES = setOf(
        "USD", "EUR", "GBP", "JPY", "CNY", "INR", "AUD", "CAD",
        "CHF", "HKD", "SGD", "SEK", "KRW", "BRL", "RUB", "ZAR"
    )

    /**
     * Validates a currency amount
     * @param amount The amount to validate (as String for input validation)
     * @param currencyCode Optional currency code for currency-specific validation
     * @return ValidationResult indicating success or specific error
     */
    fun validateAmount(
        amount: String, 
        currencyCode: String? = null
    ): ValidationUtils.ValidationResult {
        return when {
            amount.isBlank() -> 
                ValidationUtils.ValidationResult.Error("Amount cannot be empty")
            
            !isValidNumberFormat(amount) -> 
                ValidationUtils.ValidationResult.Error("Invalid number format")
            
            else -> {
                val parsedAmount = parseAmount(amount)
                    ?: return ValidationUtils.ValidationResult.Error("Invalid amount format")
                
                validateParsedAmount(parsedAmount, currencyCode)
            }
        }
    }

    /**
     * Validates a parsed amount value
     */
    fun validateAmount(
        amount: Double,
        currencyCode: String? = null
    ): ValidationUtils.ValidationResult {
        return validateParsedAmount(amount, currencyCode)
    }

    private fun validateParsedAmount(
        amount: Double,
        currencyCode: String?
    ): ValidationUtils.ValidationResult {
        return when {
            amount.isNaN() || amount.isInfinite() -> 
                ValidationUtils.ValidationResult.Error("Invalid amount")
            
            amount == 0.0 -> 
                ValidationUtils.ValidationResult.Error("Amount cannot be zero")
            
            amount < 0 -> 
                ValidationUtils.ValidationResult.Error("Amount cannot be negative")
            
            amount > MAX_AMOUNT -> 
                ValidationUtils.ValidationResult.Error("Amount exceeds maximum limit")
            
            amount < MIN_AMOUNT -> 
                ValidationUtils.ValidationResult.Error("Amount is below minimum limit")
            
            !hasValidDecimalPlaces(amount, currencyCode) -> 
                ValidationUtils.ValidationResult.Error("Too many decimal places for currency")
            
            else -> ValidationUtils.ValidationResult.Success
        }
    }

    /**
     * Validates a currency code (ISO 4217)
     * @param currencyCode The 3-letter currency code
     * @param allowUnsupported Whether to allow currencies not in supported list
     * @return ValidationResult indicating success or specific error
     */
    fun validateCurrencyCode(
        currencyCode: String,
        allowUnsupported: Boolean = false
    ): ValidationUtils.ValidationResult {
        val normalizedCode = currencyCode.uppercase().trim()
        
        return when {
            normalizedCode.isBlank() -> 
                ValidationUtils.ValidationResult.Error("Currency code cannot be empty")
            
            normalizedCode.length != 3 -> 
                ValidationUtils.ValidationResult.Error("Currency code must be 3 characters")
            
            !normalizedCode.matches(Regex("^[A-Z]{3}$")) -> 
                ValidationUtils.ValidationResult.Error("Currency code must be letters only")
            
            !isValidIsoCode(normalizedCode) -> 
                ValidationUtils.ValidationResult.Error("Invalid ISO 4217 currency code")
            
            !allowUnsupported && normalizedCode !in SUPPORTED_CURRENCIES -> 
                ValidationUtils.ValidationResult.Error("Currency not supported")
            
            else -> ValidationUtils.ValidationResult.Success
        }
    }

    /**
     * Validates amount string format for input
     */
    fun validateAmountFormat(amount: String): ValidationUtils.ValidationResult {
        return when {
            amount.isBlank() -> 
                ValidationUtils.ValidationResult.Error("Amount cannot be empty")
            
            amount.startsWith(".") || amount.endsWith(".") -> 
                ValidationUtils.ValidationResult.Error("Invalid decimal format")
            
            amount.count { it == '.' } > 1 -> 
                ValidationUtils.ValidationResult.Error("Multiple decimal points not allowed")
            
            amount.contains(",") -> 
                ValidationUtils.ValidationResult.Error("Commas not allowed. Use decimal point only")
            
            amount.startsWith("-") -> 
                ValidationUtils.ValidationResult.Error("Negative amounts not allowed")
            
            !amount.matches(Regex("^\\d*\\.?\\d*$")) -> 
                ValidationUtils.ValidationResult.Error("Only numbers and decimal point allowed")
            
            else -> ValidationUtils.ValidationResult.Success
        }
    }

    /**
     * Formats an amount for display with proper currency symbol
     */
    fun formatForDisplay(
        amount: Double, 
        currencyCode: String,
        locale: Locale = Locale.getDefault()
    ): String {
        return try {
            val currency = Currency.getInstance(currencyCode)
            val symbols = DecimalFormatSymbols(locale)
            val formatter = DecimalFormat("#,##0.00", symbols)
            "${currency.symbol}${formatter.format(amount)}"
        } catch (e: IllegalArgumentException) {
            "%.2f %s".format(amount, currencyCode)
        }
    }

    /**
     * Parses a currency string to double
     */
    fun parseAmount(amount: String): Double? {
        return try {
            // Remove currency symbols and whitespace
            val cleanAmount = amount
                .replace(Regex("[^\\d.-]"), "")
                .trim()
            
            if (cleanAmount.isEmpty()) return null
            
            BigDecimal(cleanAmount).setScale(2, RoundingMode.HALF_UP).toDouble()
        } catch (e: NumberFormatException) {
            null
        } catch (e: ParseException) {
            null
        }
    }

    /**
     * Gets the number of decimal places for a currency
     */
    fun getDecimalPlaces(currencyCode: String): Int {
        return try {
            Currency.getInstance(currencyCode).defaultFractionDigits
        } catch (e: IllegalArgumentException) {
            2 // Default to 2 decimal places
        }
    }

    /**
     * Checks if the amount has valid decimal places for the currency
     */
    private fun hasValidDecimalPlaces(amount: Double, currencyCode: String?): Boolean {
        val decimalPlaces = currencyCode?.let { getDecimalPlaces(it) } ?: 2
        
        val multiplier = 10.0.pow(decimalPlaces.toDouble())
        val scaled = (amount * multiplier).toLong()
        val reconstructed = scaled / multiplier
        
        return kotlin.math.abs(amount - reconstructed) < 0.0001
    }

    private fun isValidNumberFormat(amount: String): Boolean {
        return amount.matches(Regex("^[+-]?(\\d{1,3}(,\\d{3})*|\\d+)(\\.\\d+)?$")) ||
               amount.matches(Regex("^[+-]?(\\d{1,3}(\\s\\d{3})*|\\d+)(,\\d+)?$")) || // European format
               amount.matches(Regex("^[+-]?\\d*\\.?\\d*$"))
    }

    private fun isValidIsoCode(code: String): Boolean {
        return try {
            Currency.getInstance(code)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    /**
     * Gets list of supported currency codes
     */
    fun getSupportedCurrencies(): List<String> = SUPPORTED_CURRENCIES.toList()

    /**
     * Checks if a currency is supported
     */
    fun isSupportedCurrency(currencyCode: String): Boolean {
        return currencyCode.uppercase() in SUPPORTED_CURRENCIES
    }
}
