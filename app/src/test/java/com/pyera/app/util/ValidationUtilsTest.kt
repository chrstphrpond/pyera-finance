package com.pyera.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Comprehensive tests for ValidationUtils with edge case coverage
 */
@RunWith(Parameterized::class)
class ValidationUtilsAmountParameterizedTest(
    private val input: Double,
    private val expectedValid: Boolean,
    private val description: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{2}: {0} -> {1}")
        fun data(): List<Array<Any>> = listOf(
            // Negative and zero amounts
            arrayOf(-1.0, false, "negative amount"),
            arrayOf(-0.01, false, "small negative"),
            arrayOf(-999999999.99, false, "large negative"),
            arrayOf(0.0, false, "zero amount"),
            arrayOf(-0.0, false, "negative zero"),
            
            // Very small amounts (edge of valid range)
            arrayOf(0.001, true, "very small decimal"),
            arrayOf(0.01, true, "minimum valid cent"),
            arrayOf(0.0001, true, "tiny decimal"),
            
            // Normal valid amounts
            arrayOf(1.0, true, "single dollar"),
            arrayOf(100.0, true, "hundred dollars"),
            arrayOf(999999.99, true, "large valid amount"),
            arrayOf(999999999.0, true, "near maximum"),
            arrayOf(999999999.99, true, "maximum valid amount"),
            
            // Boundary and excessive amounts
            arrayOf(1000000000.0, false, "exactly 1 billion"),
            arrayOf(1000000000.01, false, "just over 1 billion"),
            arrayOf(Double.MAX_VALUE, false, "Double.MAX_VALUE"),
            
            // Special double values
            arrayOf(Double.NaN, false, "NaN"),
            arrayOf(Double.POSITIVE_INFINITY, false, "positive infinity"),
            arrayOf(Double.NEGATIVE_INFINITY, false, "negative infinity"),
            arrayOf(Double.MIN_VALUE, true, "Double.MIN_VALUE"),
            
            // Decimal precision edge cases
            arrayOf(99.999, true, "three decimal places"),
            arrayOf(99.9999, true, "four decimal places"),
            arrayOf(0.999999, true, "many decimals near 1")
        )
    }

    @Test
    fun testAmountValidation() {
        val result = ValidationUtils.validateAmount(input)
        val isSuccess = result is ValidationUtils.ValidationResult.Success
        assertEquals(
            "Amount $input should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isSuccess
        )
    }
}

/**
 * Tests for transaction note validation with edge cases
 */
@RunWith(Parameterized::class)
class ValidationUtilsNoteParameterizedTest(
    private val input: String,
    private val expectedValid: Boolean,
    private val description: String
) {
    companion object {
        private const val MAX_NOTE_LENGTH = 500
        
        @JvmStatic
        @Parameterized.Parameters(name = "{2}")
        fun data(): List<Array<Any>> = listOf(
            // Empty and whitespace
            arrayOf("", true, "empty string (allowed)"),
            arrayOf("   ", true, "whitespace only (allowed)"),
            arrayOf("  trimmed  ", true, "padded string"),
            
            // Normal strings
            arrayOf("Grocery shopping", true, "normal text"),
            arrayOf("Coffee at Starbucks", true, "with spaces"),
            
            // Maximum length boundaries
            arrayOf("a".repeat(MAX_NOTE_LENGTH), true, "exactly max length"),
            arrayOf("a".repeat(MAX_NOTE_LENGTH - 1), true, "one under max"),
            arrayOf("a".repeat(MAX_NOTE_LENGTH + 1), false, "one over max"),
            arrayOf("a".repeat(1000), false, "way over max"),
            
            // Special characters
            arrayOf("Food & Drinks", true, "ampersand"),
            arrayOf("Lunch @ Cafe", true, "at symbol"),
            arrayOf("Item #123", true, "hash"),
            arrayOf("Price: $50.00", true, "dollar sign"),
            arrayOf("Savings (20%)", true, "parentheses and percent"),
            arrayOf("A+B=C", true, "math symbols"),
            arrayOf("Item - Sub-item", true, "hyphen"),
            arrayOf("Item_Underscore", true, "underscore"),
            
            // Unicode characters
            arrayOf("日本語テキスト", true, "Japanese characters"),
            arrayOf("中文测试", true, "Chinese characters"),
            arrayOf("한국어", true, "Korean characters"),
            arrayOf("العربية", true, "Arabic characters"),
            arrayOf("🍕 Pizza", true, "emoji"),
            arrayOf("Café", true, "accented character"),
            arrayOf("€50", true, "euro symbol"),
            arrayOf("£30", true, "pound symbol"),
            arrayOf("¥1000", true, "yen symbol"),
            
            // Newlines and special formatting
            arrayOf("Line 1\nLine 2", true, "newline character"),
            arrayOf("Tab\there", true, "tab character"),
            arrayOf("Item\r\nDetails", true, "carriage return"),
            
            // SQL injection attempts (should be allowed at validation level, handled at DB level)
            arrayOf("'; DROP TABLE transactions; --", true, "SQL injection pattern"),
            arrayOf("<script>alert('xss')</script>", true, "XSS attempt"),
            
            // HTML/XML entities
            arrayOf("&lt;tag&gt;", true, "HTML entities"),
            arrayOf("<b>Bold</b>", true, "HTML tags"),
            
            // URL/Email-like strings
            arrayOf("https://example.com/payment", true, "URL in note"),
            arrayOf("user@example.com", true, "email in note"),
            
            // Numbers as strings
            arrayOf("12345678901234567890", true, "long number string"),
            arrayOf("3.14159265359", true, "pi digits")
        )
    }

    @Test
    fun testNoteValidation() {
        val result = ValidationUtils.validateTransactionNote(input)
        val isSuccess = result is ValidationUtils.ValidationResult.Success
        assertEquals(
            "Note '$description' should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isSuccess
        )
    }
}

/**
 * Tests for budget name validation with edge cases
 */
@RunWith(Parameterized::class)
class ValidationUtilsBudgetNameParameterizedTest(
    private val input: String,
    private val expectedValid: Boolean,
    private val description: String
) {
    companion object {
        private const val MAX_BUDGET_NAME_LENGTH = 50
        
        @JvmStatic
        @Parameterized.Parameters(name = "{2}")
        fun data(): List<Array<Any>> = listOf(
            // Empty and whitespace
            arrayOf("", false, "empty string"),
            arrayOf("   ", false, "whitespace only"),
            arrayOf("\t\n", false, "tab and newline"),
            
            // Normal valid names
            arrayOf("Food", true, "simple name"),
            arrayOf("Monthly Food Budget", true, "with spaces"),
            arrayOf("Food-2024", true, "with hyphen and numbers"),
            arrayOf("My_Budget", true, "with underscore"),
            arrayOf("Q1 Budget", true, "quarter notation"),
            
            // Length boundaries
            arrayOf("a", true, "single character"),
            arrayOf("ab", true, "two characters"),
            arrayOf("Budget".repeat(8).take(MAX_BUDGET_NAME_LENGTH), true, "exactly max length"),
            arrayOf("Budget".repeat(8).take(MAX_BUDGET_NAME_LENGTH + 1), false, "one over max"),
            arrayOf("a".repeat(100), false, "way over max"),
            
            // Unicode characters
            arrayOf("食費", true, "Japanese budget"),
            arrayOf("予算2024", true, "Japanese with numbers"),
            arrayOf("Presupuesto", true, "Spanish"),
            arrayOf("Budget 🎯", true, "with emoji"),
            arrayOf("Groceries 🛒", true, "with shopping emoji"),
            
            // Numbers only
            arrayOf("12345", true, "numbers only"),
            arrayOf("2024 Budget", true, "year prefix"),
            
            // Special characters that should be valid
            arrayOf("Food & Entertainment", true, "ampersand"),
            arrayOf("Food/Drinks", true, "forward slash"),
            arrayOf("Food + Drinks", true, "plus sign"),
            arrayOf("(Emergency)", true, "parentheses"),
            arrayOf("Budget [2024]", true, "square brackets"),
            
            // SQL injection patterns (handled at DB level)
            arrayOf("Budget'; DROP TABLE--", true, "SQL injection attempt"),
            arrayOf("<script>", true, "HTML tag"),
            
            // Path-like strings
            arrayOf("Personal/Food/Groceries", true, "path-like name"),
            
            // URL-like strings
            arrayOf("https://budget.com", true, "URL in name")
        )
    }

    @Test
    fun testBudgetNameValidation() {
        val result = ValidationUtils.validateBudgetName(input)
        val isSuccess = result is ValidationUtils.ValidationResult.Success
        assertEquals(
            "Budget name '$description' should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isSuccess
        )
    }
}

/**
 * Tests for category name validation with edge cases
 */
@RunWith(Parameterized::class)
class ValidationUtilsCategoryNameParameterizedTest(
    private val input: String,
    private val expectedValid: Boolean,
    private val description: String
) {
    companion object {
        private const val MAX_CATEGORY_NAME_LENGTH = 50
        
        @JvmStatic
        @Parameterized.Parameters(name = "{2}")
        fun data(): List<Array<Any>> = listOf(
            // Empty and whitespace
            arrayOf("", false, "empty string"),
            arrayOf("   ", false, "whitespace only"),
            arrayOf("\t", false, "tab character"),
            
            // Valid simple names
            arrayOf("Food", true, "simple name"),
            arrayOf("Transport", true, "transport category"),
            arrayOf("Entertainment", true, "entertainment"),
            arrayOf("a", true, "single letter"),
            arrayOf("AB", true, "two letters"),
            
            // Valid with numbers and allowed chars
            arrayOf("Food123", true, "with numbers"),
            arrayOf("Food 123", true, "with space and numbers"),
            arrayOf("Food-1", true, "with hyphen"),
            arrayOf("Food_1", true, "with underscore"),
            arrayOf("Q1-2024", true, "quarter-year format"),
            arrayOf("Category A", true, "with space"),
            
            // Length boundaries
            arrayOf("a".repeat(MAX_CATEGORY_NAME_LENGTH), true, "exactly max length"),
            arrayOf("a".repeat(MAX_CATEGORY_NAME_LENGTH + 1), false, "one over max"),
            arrayOf("Category".repeat(10), false, "way over max"),
            
            // Invalid characters (based on current regex: ^[a-zA-Z0-9\s\-_]+$)
            arrayOf("Food!", false, "exclamation mark"),
            arrayOf("Food@", false, "at symbol"),
            arrayOf("Food#", false, "hash"),
            arrayOf("Food$", false, "dollar sign"),
            arrayOf("Food%", false, "percent"),
            arrayOf("Food&", false, "ampersand"),
            arrayOf("Food*", false, "asterisk"),
            arrayOf("Food(", false, "open parenthesis"),
            arrayOf("Food)", false, "close parenthesis"),
            arrayOf("Food=", false, "equals"),
            arrayOf("Food+", false, "plus"),
            arrayOf("Food/", false, "forward slash"),
            arrayOf("Food\\", false, "backslash"),
            arrayOf("Food|", false, "pipe"),
            arrayOf("Food:", false, "colon"),
            arrayOf("Food;", false, "semicolon"),
            arrayOf("Food'", false, "single quote"),
            arrayOf("Food\"", false, "double quote"),
            arrayOf("Food<", false, "less than"),
            arrayOf("Food>", false, "greater than"),
            arrayOf("Food?", false, "question mark"),
            arrayOf("Food,", false, "comma"),
            arrayOf("Food.", false, "period"),
            
            // Unicode characters (may fail with current regex)
            arrayOf("食費", false, "Japanese characters (regex limitation)"),
            arrayOf("Comida", true, "Spanish (basic Latin)"),
            arrayOf("Café", false, "accented e (regex limitation)"),
            arrayOf("Food 🍕", false, "emoji (regex limitation)"),
            arrayOf("€", false, "euro symbol"),
            
            // Multiple invalid characters
            arrayOf("Food!!!", false, "multiple exclamation"),
            arrayOf("@#$%", false, "all symbols"),
            
            // Leading/trailing special cases
            arrayOf(" Food", true, "leading space"),
            arrayOf("Food ", true, "trailing space"),
            arrayOf("-Food", true, "leading hyphen"),
            arrayOf("Food-", true, "trailing hyphen"),
            
            // Numbers only
            arrayOf("123", true, "numbers only"),
            arrayOf("0", true, "zero only"),
            
            // SQL injection (will fail due to special chars)
            arrayOf("'; DROP TABLE--", false, "SQL injection"),
        )
    }

    @Test
    fun testCategoryNameValidation() {
        val result = ValidationUtils.validateCategoryName(input)
        val isSuccess = result is ValidationUtils.ValidationResult.Success
        assertEquals(
            "Category name '$description' should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isSuccess
        )
    }
}

/**
 * Tests for percentage validation with edge cases
 */
@RunWith(Parameterized::class)
class ValidationUtilsPercentageParameterizedTest(
    private val input: Int,
    private val expectedValid: Boolean,
    private val description: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{2}: {0}%")
        fun data(): List<Array<Any>> = listOf(
            // Negative percentages
            arrayOf(-100, false, "negative hundred"),
            arrayOf(-1, false, "negative one"),
            arrayOf(-50, false, "negative fifty"),
            arrayOf(Int.MIN_VALUE, false, "Integer.MIN_VALUE"),
            
            // Zero
            arrayOf(0, true, "zero percent"),
            
            // Valid percentages
            arrayOf(1, true, "one percent"),
            arrayOf(50, true, "fifty percent"),
            arrayOf(99, true, "ninety-nine percent"),
            arrayOf(100, true, "one hundred percent"),
            
            // Over 100
            arrayOf(101, false, "one hundred one percent"),
            arrayOf(150, false, "one hundred fifty percent"),
            arrayOf(999, false, "nine hundred ninety-nine percent"),
            arrayOf(Int.MAX_VALUE, false, "Integer.MAX_VALUE"),
            
            // Edge values
            arrayOf(25, true, "quarter"),
            arrayOf(33, true, "third approximation"),
            arrayOf(66, true, "two thirds approximation"),
            arrayOf(75, true, "three quarters"),
            arrayOf(99, true, "just under max"),
            arrayOf(100, true, "exactly max")
        )
    }

    @Test
    fun testPercentageValidation() {
        val result = ValidationUtils.validatePercentage(input)
        val isSuccess = result is ValidationUtils.ValidationResult.Success
        assertEquals(
            "Percentage $input% ($description) should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isSuccess
        )
    }
}

/**
 * Non-parameterized tests for ValidationUtils
 * Testing error messages and specific behaviors
 */
class ValidationUtilsNonParameterizedTest {

    @Test
    fun `validateAmount returns correct error message for negative amount`() {
        val result = ValidationUtils.validateAmount(-100.0)
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        val error = result as ValidationUtils.ValidationResult.Error
        assertEquals("Amount cannot be negative", error.message)
    }

    @Test
    fun `validateAmount returns correct error message for excessive amount`() {
        val result = ValidationUtils.validateAmount(1_000_000_000.0)
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        val error = result as ValidationUtils.ValidationResult.Error
        assertEquals("Amount exceeds maximum limit", error.message)
    }

    @Test
    fun `validateTransactionNote returns correct error message for long note`() {
        val longNote = "a".repeat(501)
        val result = ValidationUtils.validateTransactionNote(longNote)
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        val error = result as ValidationUtils.ValidationResult.Error
        assertTrue(error.message.contains("too long"))
        assertTrue(error.message.contains("500"))
    }

    @Test
    fun `validateCategoryName returns correct error for blank name`() {
        val result = ValidationUtils.validateCategoryName("")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        val error = result as ValidationUtils.ValidationResult.Error
        assertEquals("Name cannot be empty", error.message)
    }

    @Test
    fun `validateCategoryName returns correct error for long name`() {
        val longName = "a".repeat(51)
        val result = ValidationUtils.validateCategoryName(longName)
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        val error = result as ValidationUtils.ValidationResult.Error
        assertTrue(error.message.contains("too long"))
    }

    @Test
    fun `validateCategoryName returns correct error for invalid characters`() {
        val result = ValidationUtils.validateCategoryName("Food@")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        val error = result as ValidationUtils.ValidationResult.Error
        assertEquals("Name contains invalid characters", error.message)
    }

    @Test
    fun `validateBudgetName returns correct error for blank name`() {
        val result = ValidationUtils.validateBudgetName("   ")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        val error = result as ValidationUtils.ValidationResult.Error
        assertEquals("Budget name cannot be empty", error.message)
    }

    @Test
    fun `validateBudgetName returns correct error for long name`() {
        val longName = "a".repeat(51)
        val result = ValidationUtils.validateBudgetName(longName)
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        val error = result as ValidationUtils.ValidationResult.Error
        assertTrue(error.message.contains("too long"))
    }

    @Test
    fun `validatePercentage returns correct error for negative`() {
        val result = ValidationUtils.validatePercentage(-5)
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        val error = result as ValidationUtils.ValidationResult.Error
        assertEquals("Percentage cannot be negative", error.message)
    }

    @Test
    fun `validatePercentage returns correct error for over 100`() {
        val result = ValidationUtils.validatePercentage(150)
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        val error = result as ValidationUtils.ValidationResult.Error
        assertEquals("Percentage cannot exceed 100", error.message)
    }

    @Test
    fun `success results are equal`() {
        val result1 = ValidationUtils.ValidationResult.Success
        val result2 = ValidationUtils.ValidationResult.Success
        assertEquals(result1, result2)
    }

    @Test
    fun `error results with same message are equal`() {
        val result1 = ValidationUtils.ValidationResult.Error("Test error")
        val result2 = ValidationUtils.ValidationResult.Error("Test error")
        assertEquals(result1, result2)
    }

    @Test
    fun `validation results can be used in when expressions`() {
        val result = ValidationUtils.validateAmount(100.0)
        val message = when (result) {
            is ValidationUtils.ValidationResult.Success -> "Valid"
            is ValidationUtils.ValidationResult.Error -> "Error: ${result.message}"
        }
        assertEquals("Valid", message)
    }
}
