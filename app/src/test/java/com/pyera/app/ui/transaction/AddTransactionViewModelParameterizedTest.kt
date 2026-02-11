package com.pyera.app.ui.transaction

import com.pyera.app.domain.repository.TransactionRepository
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Parameterized tests for amount validation in AddTransactionViewModel
 * Tests edge cases and boundary values comprehensively
 */
@RunWith(Parameterized::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelAmountParameterizedTest(
    private val amount: String,
    private val expectedValid: Boolean,
    private val expectedError: String?,
    private val description: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{3}: \"{0}\" -> valid={1}")
        fun data(): List<Array<Any?>> = listOf(
            // Valid amounts
            arrayOf("100", true, null, "whole number"),
            arrayOf("100.50", true, null, "with cents"),
            arrayOf("0.01", true, null, "minimum valid"),
            arrayOf("0.99", true, null, "under one dollar"),
            arrayOf("999999999.99", true, null, "maximum valid"),
            arrayOf("1.00", true, null, "exact dollar"),
            arrayOf("1234567.89", true, null, "large amount"),

            // Empty/blank amounts
            arrayOf("", false, "Amount is required", "empty string"),
            arrayOf("   ", false, "Amount is required", "whitespace only"),
            arrayOf("  ", false, "Amount is required", "spaces"),
            arrayOf("\t", false, "Amount is required", "tab"),
            arrayOf("\n", false, "Amount is required", "newline"),

            // Zero amounts
            arrayOf("0", false, "Amount must be greater than 0", "zero"),
            arrayOf("0.0", false, "Amount must be greater than 0", "zero decimal"),
            arrayOf("0.00", false, "Amount must be greater than 0", "zero cents"),
            arrayOf("000", false, "Amount must be greater than 0", "multiple zeros"),

            // Negative amounts
            arrayOf("-1", false, "Amount must be greater than 0", "negative integer"),
            arrayOf("-0.01", false, "Amount must be greater than 0", "negative cent"),
            arrayOf("-100.50", false, "Amount must be greater than 0", "negative with cents"),
            arrayOf("-999999", false, "Amount must be greater than 0", "large negative"),

            // Invalid format
            arrayOf("abc", false, "Invalid amount", "letters"),
            arrayOf("10.10.10", false, "Invalid amount", "multiple decimals"),
            arrayOf("10,50", false, "Invalid amount", "comma as decimal"),
            arrayOf("$100", false, "Invalid amount", "currency symbol"),
            arrayOf("100$", false, "Invalid amount", "trailing currency"),
            arrayOf("€50", false, "Invalid amount", "euro symbol"),
            arrayOf("£30", false, "Invalid amount", "pound symbol"),
            arrayOf("ten", false, "Invalid amount", "spelled out"),
            arrayOf("10.5.5", false, "Invalid amount", "double decimal"),
            arrayOf("1,000", false, "Invalid amount", "thousand separator"),
            arrayOf("1 000", false, "Invalid amount", "space separated"),

            // Boundary amounts
            arrayOf("1000000000", false, "Amount is too large", "exactly 1 billion"),
            arrayOf("1000000000.00", false, "Amount is too large", "1 billion with cents"),
            arrayOf("9999999999", false, "Amount is too large", "10 billion"),
            arrayOf("999999999.991", false, "Amount is too large", "just over max"),

            // Special characters
            arrayOf("10.5%", false, "Invalid amount", "percent sign"),
            arrayOf("10.5#", false, "Invalid amount", "hash"),
            arrayOf("10@5", false, "Invalid amount", "at symbol"),
            arrayOf("10&5", false, "Invalid amount", "ampersand"),
            arrayOf("10*5", false, "Invalid amount", "asterisk"),
            arrayOf("10+5", false, "Invalid amount", "plus"),
            arrayOf("10-5", false, "Invalid amount", "minus in middle"),
            arrayOf("10/5", false, "Invalid amount", "slash"),
            arrayOf("10\\5", false, "Invalid amount", "backslash"),
            arrayOf("10=5", false, "Invalid amount", "equals"),
            arrayOf("10?5", false, "Invalid amount", "question mark"),
            arrayOf("10!5", false, "Invalid amount", "exclamation"),
            arrayOf("(10)", false, "Invalid amount", "parentheses"),
            arrayOf("[10]", false, "Invalid amount", "brackets"),
            arrayOf("{10}", false, "Invalid amount", "braces"),

            // Edge cases
            arrayOf(".", false, "Invalid amount", "decimal only"),
            arrayOf(".5", false, "Invalid amount", "leading decimal"),
            arrayOf("5.", false, "Invalid amount", "trailing decimal"),
            arrayOf("000100", true, null, "leading zeros"),
            arrayOf("01.50", true, null, "leading zero"),
            arrayOf("1.5", true, null, "single decimal"),
            arrayOf("1.555", true, null, "three decimals"),

            // Unicode
            arrayOf("१००", false, "Invalid amount", "Hindi numerals"),
            arrayOf("十", false, "Invalid amount", "Chinese numeral"),
            arrayOf("①", false, "Invalid amount", "circled digit"),

            // Scientific notation
            arrayOf("1e2", false, "Invalid amount", "scientific notation"),
            arrayOf("1E2", false, "Invalid amount", "scientific uppercase"),
            arrayOf("1.5e10", false, "Invalid amount", "scientific with decimal"),

            // SQL injection attempts
            arrayOf("1'; DROP TABLE--", false, "Invalid amount", "SQL injection"),
            arrayOf("1 OR 1=1", false, "Invalid amount", "boolean injection"),

            // XSS attempts
            arrayOf("<script>alert(1)</script>", false, "Invalid amount", "XSS attempt"),
            arrayOf("javascript:alert(1)", false, "Invalid amount", "javascript protocol"),

            // Very long inputs
            arrayOf("1".repeat(1000), false, "Amount is too large", "very long number"),
            arrayOf("1." + "5".repeat(100), false, "Amount is too large", "many decimals"),

            // Whitespace variations
            arrayOf(" 100", true, null, "leading space"),
            arrayOf("100 ", true, null, "trailing space"),
            arrayOf(" 100 ", true, null, "both spaces"),
            arrayOf("10 0", false, "Invalid amount", "middle space"),

            // Sign variations
            arrayOf("+100", false, "Invalid amount", "explicit positive"),
            arrayOf("--100", false, "Invalid amount", "double negative"),
            arrayOf("+-100", false, "Invalid amount", "mixed signs")
        )
    }

    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAmountValidation() = runTest(testDispatcher) {
        val viewModel = AddTransactionViewModel(transactionRepository)

        val isValid = viewModel.validateAmount(amount)

        assertEquals(
            "Amount '$amount' ($description) should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isValid
        )

        // Verify error message
        val validationState = viewModel.validationState.value
        if (expectedValid) {
            assertNull("Expected no error for valid amount '$amount'", validationState.amountError)
        } else {
            assertEquals(
                "Expected error message for '$amount'",
                expectedError,
                validationState.amountError
            )
        }
    }
}

/**
 * Parameterized tests for description validation in AddTransactionViewModel
 */
@RunWith(Parameterized::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelDescriptionParameterizedTest(
    private val description: String,
    private val expectedValid: Boolean,
    private val expectedError: String?,
    private val testDescription: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{3}")
        fun data(): List<Array<Any?>> = listOf(
            // Valid descriptions
            arrayOf("Grocery shopping", true, null, "normal description"),
            arrayOf("A", true, null, "single character"),
            arrayOf("Coffee", true, null, "single word"),
            arrayOf("Coffee at Starbucks", true, null, "multiple words"),
            arrayOf("Q1 2024 Expenses", true, null, "with numbers"),
            arrayOf("Food & Drinks", true, null, "with ampersand"),
            arrayOf("Lunch @ 1pm", true, null, "with at symbol"),
            arrayOf("Item #123", true, null, "with hash"),
            arrayOf("$50 purchase", true, null, "with dollar sign"),
            arrayOf("20% discount", true, null, "with percent"),
            arrayOf("(Important)", true, null, "with parentheses"),
            arrayOf("Item - Sub-item", true, null, "with hyphen"),
            arrayOf("Item_Underscore", true, null, "with underscore"),
            arrayOf("Item.Key", true, null, "with period"),
            arrayOf("Item/Path", true, null, "with slash"),
            arrayOf("Line1\nLine2", true, null, "with newline"),
            arrayOf("Tab\there", true, null, "with tab"),

            // Empty/blank descriptions
            arrayOf("", false, "Description is required", "empty string"),
            arrayOf("   ", false, "Description is required", "whitespace only"),
            arrayOf("\t", false, "Description is required", "tab only"),
            arrayOf("\n", false, "Description is required", "newline only"),

            // Too long descriptions
            arrayOf("a".repeat(201), false, "Description is too long", "201 characters"),
            arrayOf("a".repeat(500), false, "Description is too long", "500 characters"),
            arrayOf("Description ".repeat(50), false, "Description is too long", "long repeated text"),

            // Unicode descriptions
            arrayOf("日本語の説明", true, null, "Japanese text"),
            arrayOf("中文描述", true, null, "Chinese text"),
            arrayOf("한국어 설명", true, null, "Korean text"),
            arrayOf("العربية", true, null, "Arabic text"),
            arrayOf("Café", true, null, "accented text"),
            arrayOf("🍕 Pizza", true, null, "with emoji"),
            arrayOf("💰 Salary", true, null, "money emoji"),
            arrayOf("🛒 Groceries", true, null, "shopping emoji"),
            arrayOf("€50", true, null, "euro symbol"),
            arrayOf("£30", true, null, "pound symbol"),
            arrayOf("¥1000", true, null, "yen symbol"),
            arrayOf("₹500", true, null, "rupee symbol"),

            // Special cases
            arrayOf("   trimmed   ", true, null, "with padding"),
            arrayOf("123", true, null, "numbers only"),
            arrayOf("2024-01-15", true, null, "date format"),
            arrayOf("10:30 AM", true, null, "time format"),
            arrayOf("user@example.com", true, null, "email format"),
            arrayOf("https://example.com", true, null, "URL format"),

            // SQL injection (should pass validation, handled at DB layer)
            arrayOf("'; DROP TABLE transactions; --", true, null, "SQL injection attempt"),
            arrayOf("1 OR 1=1", true, null, "boolean injection"),

            // XSS attempts (should pass validation, handled at display layer)
            arrayOf("<script>alert('xss')</script>", true, null, "XSS script"),
            arrayOf("<img src=x onerror=alert(1)>", true, null, "XSS image"),
            arrayOf("javascript:alert(1)", true, null, "javascript protocol"),

            // Boundary: exactly 200 characters
            arrayOf("a".repeat(200), true, null, "exactly 200 chars"),
            arrayOf("a".repeat(199), true, null, "199 characters")
        )
    }

    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testDescriptionValidation() = runTest(testDispatcher) {
        val viewModel = AddTransactionViewModel(transactionRepository)

        val isValid = viewModel.validateDescription(description)

        assertEquals(
            "Description '$testDescription' should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isValid
        )

        val validationState = viewModel.validationState.value
        if (expectedValid) {
            assertNull("Expected no error for valid description", validationState.descriptionError)
        } else {
            assertEquals(
                "Expected error message",
                expectedError,
                validationState.descriptionError
            )
        }
    }
}

/**
 * Parameterized tests for category validation in AddTransactionViewModel
 */
@RunWith(Parameterized::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelCategoryParameterizedTest(
    private val categoryId: Long,
    private val expectedValid: Boolean,
    private val expectedError: String?,
    private val description: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{3}: categoryId={0}")
        fun data(): List<Array<Any?>> = listOf(
            // Valid category IDs
            arrayOf(1L, true, null, "valid positive ID"),
            arrayOf(2L, true, null, "another valid ID"),
            arrayOf(100L, true, null, "large valid ID"),
            arrayOf(Long.MAX_VALUE, true, null, "maximum long value"),

            // Invalid category IDs
            arrayOf(0L, false, "Please select a category", "zero ID"),
            arrayOf(-1L, false, "Please select a category", "negative ID"),
            arrayOf(-100L, false, "Please select a category", "large negative"),
            arrayOf(Long.MIN_VALUE, false, "Please select a category", "minimum long value")
        )
    }

    private val transactionRepository: TransactionRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testCategoryValidation() = runTest(testDispatcher) {
        val viewModel = AddTransactionViewModel(transactionRepository)

        val isValid = viewModel.validateCategory(categoryId)

        assertEquals(
            "Category ID $categoryId ($description) should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isValid
        )

        val validationState = viewModel.validationState.value
        if (expectedValid) {
            assertNull("Expected no error for valid category", validationState.categoryError)
        } else {
            assertEquals(
                "Expected error message",
                expectedError,
                validationState.categoryError
            )
        }
    }
}
