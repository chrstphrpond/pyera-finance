package com.pyera.app.util.validators

import com.pyera.app.util.ValidationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Locale

/**
 * Parameterized tests for currency amount validation
 */
@RunWith(Parameterized::class)
class CurrencyValidatorAmountParameterizedTest(
    private val amount: String,
    private val currencyCode: String?,
    private val expectedValid: Boolean,
    private val description: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{3}: \"{0}\" {1}")
        fun data(): List<Array<Any?>> = listOf(
            // Valid amounts
            arrayOf("100", null, true, "whole number"),
            arrayOf("100.50", null, true, "with cents"),
            arrayOf("0.01", null, true, "minimum"),
            arrayOf("999999999.99", null, true, "maximum"),
            arrayOf("1", "USD", true, "USD single dollar"),
            arrayOf("1.99", "EUR", true, "EUR with cents"),
            arrayOf("100", "JPY", true, "JPY (no cents)"),
            arrayOf("0.001", "BTC", true, "BTC with high precision"),
            
            // Empty/null
            arrayOf("", null, false, "empty string"),
            arrayOf("   ", null, false, "whitespace"),
            arrayOf(null, null, false, "null amount"),
            
            // Zero
            arrayOf("0", null, false, "zero"),
            arrayOf("0.0", null, false, "zero decimal"),
            arrayOf("0.00", null, false, "zero cents"),
            arrayOf("000", null, false, "multiple zeros"),
            
            // Negative
            arrayOf("-1", null, false, "negative"),
            arrayOf("-0.01", null, false, "negative cent"),
            arrayOf("-100.50", null, false, "negative with cents"),
            
            // Too large
            arrayOf("1000000000", null, false, "1 billion"),
            arrayOf("1000000000.00", null, false, "1 billion formatted"),
            arrayOf("999999999999", null, false, "trillions"),
            
            // Invalid format
            arrayOf("abc", null, false, "letters"),
            arrayOf("10.10.10", null, false, "multiple decimals"),
            arrayOf("10,50", null, false, "comma decimal"),
            arrayOf("$100", null, false, "dollar sign"),
            arrayOf("100$", null, false, "trailing dollar"),
            arrayOf("€50", null, false, "euro symbol"),
            arrayOf("£30", null, false, "pound symbol"),
            arrayOf("ten", null, false, "spelled out"),
            arrayOf("1,000", null, false, "thousand separator"),
            arrayOf("1 000", null, false, "space separated"),
            
            // Boundary formatting
            arrayOf(".", null, false, "decimal only"),
            arrayOf(".5", null, false, "leading decimal"),
            arrayOf("5.", null, false, "trailing decimal"),
            arrayOf("000100", null, true, "leading zeros"),
            
            // Scientific notation
            arrayOf("1e2", null, false, "scientific"),
            arrayOf("1E2", null, false, "scientific uppercase"),
            
            // SQL injection
            arrayOf("1'; DROP TABLE--", null, false, "SQL injection"),
            
            // XSS
            arrayOf("<script>alert(1)</script>", null, false, "XSS"),
            
            // Currency-specific
            arrayOf("100.999", "USD", false, "USD with 3 decimals"),
            arrayOf("100.99", "JPY", false, "JPY with decimals"),
            arrayOf("999999999.99", "XXX", false, "invalid currency code"),
            
            // Special characters
            arrayOf("10%", null, false, "percent"),
            arrayOf("10#", null, false, "hash"),
            arrayOf("10@", null, false, "at symbol"),
            
            // Unicode
            arrayOf("१००", null, false, "Hindi numerals"),
            arrayOf("十", null, false, "Chinese numeral")
        )
    }

    @Test
    fun testAmountValidation() {
        val result = CurrencyValidator.validateAmount(amount ?: "", currencyCode)
        val isSuccess = result is ValidationUtils.ValidationResult.Success
        
        assertEquals(
            "Amount '$amount' ($description) should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isSuccess
        )
    }
}

/**
 * Parameterized tests for currency code validation
 */
@RunWith(Parameterized::class)
class CurrencyValidatorCodeParameterizedTest(
    private val code: String,
    private val allowUnsupported: Boolean,
    private val expectedValid: Boolean,
    private val description: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{3}: \"{0}\" allowUnsupported={1}")
        fun data(): List<Array<Any>> = listOf(
            // Valid supported codes
            arrayOf("USD", false, true, "USD"),
            arrayOf("EUR", false, true, "EUR"),
            arrayOf("GBP", false, true, "GBP"),
            arrayOf("JPY", false, true, "JPY"),
            arrayOf("CNY", false, true, "CNY"),
            arrayOf("INR", false, true, "INR"),
            arrayOf("AUD", false, true, "AUD"),
            arrayOf("CAD", false, true, "CAD"),
            
            // Valid but unsupported (with allowUnsupported=false)
            arrayOf("AFN", false, false, "Afghani unsupported"),
            arrayOf("ALL", false, false, "Lek unsupported"),
            arrayOf("AMD", false, false, "Dram unsupported"),
            
            // Valid but unsupported (with allowUnsupported=true)
            arrayOf("AFN", true, true, "Afghani allowed"),
            arrayOf("ALL", true, true, "Lek allowed"),
            arrayOf("XBT", true, true, "Bitcoin allowed"),
            
            // Invalid codes
            arrayOf("", false, false, "empty"),
            arrayOf("   ", false, false, "whitespace"),
            arrayOf("US", false, false, "two chars"),
            arrayOf("USDD", false, false, "four chars"),
            arrayOf("usD", false, false, "mixed case"),
            arrayOf("123", false, false, "numbers"),
            arrayOf("US1", false, false, "with number"),
            arrayOf("U$D", false, false, "with symbol"),
            arrayOf("U S", false, false, "with space"),
            arrayOf("XXX", false, false, "invalid ISO code"),
            arrayOf("YYY", false, false, "fake code"),
            
            // SQL injection
            arrayOf("US'; DROP TABLE--", false, false, "SQL injection"),
            
            // Case variations
            arrayOf("usd", false, true, "lowercase"),
            arrayOf("Usd", false, true, "mixed case")
        )
    }

    @Test
    fun testCurrencyCodeValidation() {
        val result = CurrencyValidator.validateCurrencyCode(code, allowUnsupported)
        val isSuccess = result is ValidationUtils.ValidationResult.Success
        
        assertEquals(
            "Currency code '$code' ($description) should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isSuccess
        )
    }
}

/**
 * Parameterized tests for amount format validation
 */
@RunWith(Parameterized::class)
class CurrencyValidatorFormatParameterizedTest(
    private val amount: String,
    private val expectedValid: Boolean,
    private val description: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{2}: \"{0}\"")
        fun data(): List<Array<Any>> = listOf(
            // Valid formats
            arrayOf("100", true, "whole number"),
            arrayOf("100.50", true, "with decimal"),
            arrayOf("0.01", true, "small decimal"),
            arrayOf("0.1", true, "single decimal"),
            arrayOf(".5", false, "leading decimal"),
            arrayOf("5.", false, "trailing decimal"),
            
            // Empty/blank
            arrayOf("", false, "empty"),
            arrayOf("   ", false, "whitespace"),
            
            // Multiple decimals
            arrayOf("100.50.25", false, "two decimals"),
            arrayOf("100..50", false, "double dot"),
            
            // Commas
            arrayOf("1,000", false, "comma separator"),
            arrayOf("1,000.50", false, "comma and decimal"),
            
            // Signs
            arrayOf("-100", false, "negative"),
            arrayOf("+100", false, "explicit positive"),
            
            // Characters
            arrayOf("100a", false, "with letter"),
            arrayOf("$100", false, "dollar sign"),
            arrayOf("100$", false, "trailing dollar"),
            
            // Spaces
            arrayOf("100 000", false, "middle space"),
            arrayOf(" 100", true, "leading space"),
            arrayOf("100 ", true, "trailing space")
        )
    }

    @Test
    fun testFormatValidation() {
        val result = CurrencyValidator.validateAmountFormat(amount)
        val isSuccess = result is ValidationUtils.ValidationResult.Success
        
        assertEquals(
            "Format '$amount' ($description) should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isSuccess
        )
    }
}

/**
 * Non-parameterized tests for CurrencyValidator
 */
class CurrencyValidatorNonParameterizedTest {

    @Test
    fun `parseAmount parses valid amounts correctly`() {
        assertEquals(100.0, CurrencyValidator.parseAmount("100") ?: 0.0, 0.001)
        assertEquals(100.5, CurrencyValidator.parseAmount("100.5") ?: 0.0, 0.001)
        assertEquals(100.55, CurrencyValidator.parseAmount("100.55") ?: 0.0, 0.001)
        assertEquals(0.01, CurrencyValidator.parseAmount("0.01") ?: 0.0, 0.001)
    }

    @Test
    fun `parseAmount handles currency symbols`() {
        assertEquals(100.0, CurrencyValidator.parseAmount("$100") ?: 0.0, 0.001)
        assertEquals(100.0, CurrencyValidator.parseAmount("€100") ?: 0.0, 0.001)
        assertEquals(100.5, CurrencyValidator.parseAmount("$100.50") ?: 0.0, 0.001)
    }

    @Test
    fun `parseAmount returns null for invalid input`() {
        assertNull(CurrencyValidator.parseAmount(""))
        assertNull(CurrencyValidator.parseAmount("abc"))
        assertNull(CurrencyValidator.parseAmount("$"))
    }

    @Test
    fun `formatForDisplay formats correctly`() {
        val formatted = CurrencyValidator.formatForDisplay(100.5, "USD", Locale.US)
        assertTrue(formatted.contains("100.50") || formatted.contains("100.5"))
        assertTrue(formatted.contains("$") || formatted.contains("USD"))
    }

    @Test
    fun `getDecimalPlaces returns correct value for currencies`() {
        assertEquals(2, CurrencyValidator.getDecimalPlaces("USD"))
        assertEquals(2, CurrencyValidator.getDecimalPlaces("EUR"))
        assertEquals(0, CurrencyValidator.getDecimalPlaces("JPY"))
        assertEquals(2, CurrencyValidator.getDecimalPlaces("XXX")) // Default
    }

    @Test
    fun `getSupportedCurrencies returns list`() {
        val currencies = CurrencyValidator.getSupportedCurrencies()
        assertTrue(currencies.isNotEmpty())
        assertTrue(currencies.contains("USD"))
        assertTrue(currencies.contains("EUR"))
        assertTrue(currencies.contains("JPY"))
    }

    @Test
    fun `isSupportedCurrency checks correctly`() {
        assertTrue(CurrencyValidator.isSupportedCurrency("USD"))
        assertTrue(CurrencyValidator.isSupportedCurrency("usd")) // Case insensitive
        assertFalse(CurrencyValidator.isSupportedCurrency("XXX"))
        assertFalse(CurrencyValidator.isSupportedCurrency("INVALID"))
    }

    @Test
    fun `validateAmount with double input works correctly`() {
        val result = CurrencyValidator.validateAmount(100.0, "USD")
        assertTrue(result is ValidationUtils.ValidationResult.Success)
        
        val negativeResult = CurrencyValidator.validateAmount(-1.0, "USD")
        assertTrue(negativeResult is ValidationUtils.ValidationResult.Error)
        
        val zeroResult = CurrencyValidator.validateAmount(0.0, "USD")
        assertTrue(zeroResult is ValidationUtils.ValidationResult.Error)
    }

    @Test
    fun `validateAmount handles NaN and Infinity`() {
        val nanResult = CurrencyValidator.validateAmount(Double.NaN)
        assertTrue(nanResult is ValidationUtils.ValidationResult.Error)
        
        val infResult = CurrencyValidator.validateAmount(Double.POSITIVE_INFINITY)
        assertTrue(infResult is ValidationUtils.ValidationResult.Error)
    }

    @Test
    fun `validate returns appropriate error messages`() {
        val emptyResult = CurrencyValidator.validateAmount("")
        assertTrue(emptyResult is ValidationUtils.ValidationResult.Error)
        assertEquals("Amount cannot be empty", (emptyResult as ValidationUtils.ValidationResult.Error).message)
        
        val zeroResult = CurrencyValidator.validateAmount("0")
        assertTrue(zeroResult is ValidationUtils.ValidationResult.Error)
        assertEquals("Amount cannot be zero", (zeroResult as ValidationUtils.ValidationResult.Error).message)
        
        val negativeResult = CurrencyValidator.validateAmount("-1")
        assertTrue(negativeResult is ValidationUtils.ValidationResult.Error)
        assertEquals("Amount cannot be negative", (negativeResult as ValidationUtils.ValidationResult.Error).message)
    }

    @Test
    fun `validateCurrencyCode returns appropriate error messages`() {
        val emptyResult = CurrencyValidator.validateCurrencyCode("")
        assertTrue(emptyResult is ValidationUtils.ValidationResult.Error)
        assertEquals("Currency code cannot be empty", (emptyResult as ValidationUtils.ValidationResult.Error).message)
        
        val shortResult = CurrencyValidator.validateCurrencyCode("US")
        assertTrue(shortResult is ValidationUtils.ValidationResult.Error)
        assertEquals("Currency code must be 3 characters", (shortResult as ValidationUtils.ValidationResult.Error).message)
        
        val invalidResult = CurrencyValidator.validateCurrencyCode("U$D")
        assertTrue(invalidResult is ValidationUtils.ValidationResult.Error)
        assertEquals("Currency code must be letters only", (invalidResult as ValidationUtils.ValidationResult.Error).message)
    }
}
