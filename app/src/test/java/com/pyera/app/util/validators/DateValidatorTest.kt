package com.pyera.app.util.validators

import com.pyera.app.util.ValidationUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Parameterized tests for date validation
 */
@RunWith(Parameterized::class)
class DateValidatorTimestampParameterizedTest(
    private val timestamp: Long,
    private val allowFuture: Boolean,
    private val expectedValid: Boolean,
    private val description: String
) {
    companion object {
        private val now = System.currentTimeMillis()
        private val oneDay = TimeUnit.DAYS.toMillis(1)
        private val oneMonth = TimeUnit.DAYS.toMillis(30)
        private val oneYear = TimeUnit.DAYS.toMillis(365)
        private val tenYears = TimeUnit.DAYS.toMillis(3650)
        
        @JvmStatic
        @Parameterized.Parameters(name = "{3}: allowFuture={1}")
        fun data(): List<Array<Any>> = listOf(
            // Current/past dates (valid)
            arrayOf(now, false, true, "current timestamp"),
            arrayOf(now - oneDay, false, true, "yesterday"),
            arrayOf(now - oneMonth, false, true, "one month ago"),
            arrayOf(now - oneYear, false, true, "one year ago"),
            arrayOf(1L, false, true, "minimal valid timestamp"),
            
            // Past dates (allowFuture=true)
            arrayOf(now - oneDay, true, true, "yesterday with future allowed"),
            arrayOf(now - oneMonth, true, true, "one month ago with future allowed"),
            
            // Future dates (allowFuture=false)
            arrayOf(now + oneDay, false, false, "tomorrow"),
            arrayOf(now + oneMonth, false, false, "one month future"),
            arrayOf(now + oneYear, false, false, "one year future"),
            
            // Future dates (allowFuture=true)
            arrayOf(now + oneDay, true, true, "tomorrow with future allowed"),
            arrayOf(now + oneMonth, true, true, "one month future with future allowed"),
            arrayOf(now + oneYear, true, true, "one year future with future allowed"),
            
            // Too far future (even with allowFuture=true)
            arrayOf(now + tenYears * 2, true, false, "20 years future"),
            
            // Too far past
            arrayOf(now - tenYears * 2, false, false, "20 years ago"),
            arrayOf(0L, false, false, "zero timestamp"),
            arrayOf(-1L, false, false, "negative timestamp"),
            
            // Invalid timestamps
            arrayOf(-1000L, false, false, "negative value"),
            arrayOf(32503680000001L, false, false, "year 3000+"),
            arrayOf(Long.MAX_VALUE, false, false, "Long.MAX_VALUE"),
            arrayOf(Long.MIN_VALUE, false, false, "Long.MIN_VALUE")
        )
    }

    @Test
    fun testTimestampValidation() {
        val result = DateValidator.validateTransactionDate(timestamp, allowFuture)
        val isSuccess = result is ValidationUtils.ValidationResult.Success
        
        assertEquals(
            "Timestamp $description should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isSuccess
        )
    }
}

/**
 * Parameterized tests for date string validation
 */
@RunWith(Parameterized::class)
class DateValidatorStringParameterizedTest(
    private val dateString: String,
    private val format: String,
    private val expectedValid: Boolean,
    private val description: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{3}: \"{0}\" format={1}")
        fun data(): List<Array<Any>> = listOf(
            // Valid dates (yyyy-MM-dd format)
            arrayOf("2024-01-15", "yyyy-MM-dd", true, "standard date"),
            arrayOf("2024-12-31", "yyyy-MM-dd", true, "year end"),
            arrayOf("2024-01-01", "yyyy-MM-dd", true, "year start"),
            arrayOf("2020-02-29", "yyyy-MM-dd", true, "leap year"),
            
            // Invalid dates
            arrayOf("", "yyyy-MM-dd", false, "empty string"),
            arrayOf("   ", "yyyy-MM-dd", false, "whitespace"),
            arrayOf("2024-13-01", "yyyy-MM-dd", false, "invalid month"),
            arrayOf("2024-01-32", "yyyy-MM-dd", false, "invalid day"),
            arrayOf("2023-02-29", "yyyy-MM-dd", false, "non-leap year Feb 29"),
            arrayOf("2024/01/15", "yyyy-MM-dd", false, "wrong separator"),
            arrayOf("01-15-2024", "yyyy-MM-dd", false, "wrong format"),
            arrayOf("2024-1-15", "yyyy-MM-dd", false, "single digit month"),
            arrayOf("24-01-15", "yyyy-MM-dd", false, "two digit year"),
            
            // Different formats
            arrayOf("15/01/2024", "dd/MM/yyyy", true, "EU format"),
            arrayOf("01/15/2024", "MM/dd/yyyy", true, "US format"),
            arrayOf("15-01-2024", "dd-MM-yyyy", true, "dash EU format"),
            
            // Edge cases
            arrayOf("0001-01-01", "yyyy-MM-dd", false, "year 1"),
            arrayOf("9999-12-31", "yyyy-MM-dd", true, "year 9999"),
            
            // Invalid formats
            arrayOf("2024-01-15", "invalid", false, "invalid format"),
            arrayOf("2024-01-15", "", false, "empty format"),
            
            // SQL injection attempts
            arrayOf("2024-01-15'; DROP TABLE--", "yyyy-MM-dd", false, "SQL injection"),
            
            // XSS attempts
            arrayOf("<script>alert(1)</script>", "yyyy-MM-dd", false, "XSS attempt")
        )
    }

    @Test
    fun testDateStringValidation() {
        val result = DateValidator.validateDateString(dateString, format, allowFuture = true)
        val isSuccess = result is ValidationUtils.ValidationResult.Success
        
        assertEquals(
            "Date string '$dateString' ($description) should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isSuccess
        )
    }
}

/**
 * Parameterized tests for date range validation
 */
@RunWith(Parameterized::class)
class DateValidatorRangeParameterizedTest(
    private val startOffset: Long,
    private val endOffset: Long,
    private val maxRangeDays: Int,
    private val expectedValid: Boolean,
    private val description: String
) {
    companion object {
        private val now = System.currentTimeMillis()
        private val oneDay = TimeUnit.DAYS.toMillis(1)
        
        @JvmStatic
        @Parameterized.Parameters(name = "{4}")
        fun data(): List<Array<Any>> = listOf(
            // Valid ranges
            arrayOf(-30 * oneDay, -10 * oneDay, 0, true, "past to past"),
            arrayOf(-10 * oneDay, 0L, 0, true, "past to now"),
            arrayOf(0L, 10 * oneDay, 0, true, "now to future"),
            arrayOf(-5 * oneDay, 5 * oneDay, 0, true, "past to future"),
            arrayOf(-10 * oneDay, 0L, 15, true, "within max range"),
            
            // Invalid: Start after end
            arrayOf(0L, -10 * oneDay, 0, false, "start after end"),
            arrayOf(10 * oneDay, 5 * oneDay, 0, false, "reversed dates"),
            
            // Invalid: Same date
            arrayOf(0L, 0L, 0, false, "same date"),
            arrayOf(-5 * oneDay, -5 * oneDay, 0, false, "same past date"),
            
            // Invalid: Range too large
            arrayOf(-100 * oneDay, 0L, 50, false, "range exceeds max"),
            arrayOf(0L, 100 * oneDay, 50, false, "future range too large"),
            
            // Edge cases
            arrayOf(-1 * oneDay, 0L, 1, true, "exactly max range"),
            arrayOf(-2 * oneDay, 0L, 1, false, "one day over max")
        )
    }

    @Test
    fun testDateRangeValidation() {
        val start = now + startOffset
        val end = now + endOffset
        
        val result = DateValidator.validateDateRange(start, end, maxRangeDays)
        val isSuccess = result is ValidationUtils.ValidationResult.Success
        
        assertEquals(
            "Date range $description should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isSuccess
        )
    }
}

/**
 * Parameterized tests for recurring date validation
 */
@RunWith(Parameterized::class)
class DateValidatorRecurringParameterizedTest(
    private val dayOfMonth: Int,
    private val month: Int,
    private val year: Int,
    private val frequency: DateValidator.RecurringFrequency,
    private val expectedValid: Boolean,
    private val description: String
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{5}: {2}-{1}-{0} {3}")
        fun data(): List<Array<Any>> = listOf(
            // Daily (always valid)
            arrayOf(31, 1, 2024, DateValidator.RecurringFrequency.DAILY, true, "Jan 31 daily"),
            arrayOf(29, 1, 2024, DateValidator.RecurringFrequency.DAILY, true, "any day daily"),
            
            // Weekly (always valid)
            arrayOf(15, 1, 2024, DateValidator.RecurringFrequency.WEEKLY, true, "Monday weekly"),
            arrayOf(20, 1, 2024, DateValidator.RecurringFrequency.WEEKLY, true, "Saturday weekly"),
            
            // Monthly - valid days
            arrayOf(1, 1, 2024, DateValidator.RecurringFrequency.MONTHLY, true, "first of month"),
            arrayOf(15, 1, 2024, DateValidator.RecurringFrequency.MONTHLY, true, "mid month"),
            arrayOf(28, 1, 2024, DateValidator.RecurringFrequency.MONTHLY, true, "day 28"),
            arrayOf(29, 1, 2024, DateValidator.RecurringFrequency.MONTHLY, true, "Jan 29"),
            arrayOf(31, 1, 2024, DateValidator.RecurringFrequency.MONTHLY, true, "Jan 31"),
            
            // Monthly - invalid days for February
            arrayOf(30, 1, 2024, DateValidator.RecurringFrequency.MONTHLY, true, "Jan 30 valid"),
            arrayOf(31, 2, 2024, DateValidator.RecurringFrequency.MONTHLY, false, "Feb 31 invalid"),
            
            // Yearly - Feb 29 leap year
            arrayOf(29, 2, 2024, DateValidator.RecurringFrequency.YEARLY, true, "Feb 29 leap year"),
            arrayOf(29, 2, 2023, DateValidator.RecurringFrequency.YEARLY, false, "Feb 29 non-leap"),
            
            // Yearly - other dates
            arrayOf(31, 12, 2024, DateValidator.RecurringFrequency.YEARLY, true, "Dec 31"),
            arrayOf(1, 1, 2024, DateValidator.RecurringFrequency.YEARLY, true, "Jan 1"),
            arrayOf(15, 6, 2024, DateValidator.RecurringFrequency.YEARLY, true, "mid year")
        )
    }

    @Test
    fun testRecurringDateValidation() {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, dayOfMonth, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val result = DateValidator.validateRecurringDate(calendar.timeInMillis, frequency)
        val isSuccess = result is ValidationUtils.ValidationResult.Success
        
        assertEquals(
            "Recurring date $description should be ${if (expectedValid) "valid" else "invalid"}",
            expectedValid,
            isSuccess
        )
    }
}

/**
 * Non-parameterized tests for DateValidator
 */
class DateValidatorNonParameterizedTest {

    @Test
    fun `isToday returns true for current date`() {
        val now = System.currentTimeMillis()
        assertTrue(DateValidator.isToday(now))
    }

    @Test
    fun `isToday returns false for different dates`() {
        val yesterday = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        val tomorrow = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
        
        assertFalse(DateValidator.isToday(yesterday))
        assertFalse(DateValidator.isToday(tomorrow))
    }

    @Test
    fun `isFutureDate returns true for future dates`() {
        val tomorrow = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
        assertTrue(DateValidator.isFutureDate(tomorrow))
    }

    @Test
    fun `isFutureDate returns false for past and present`() {
        val now = System.currentTimeMillis()
        val yesterday = now - TimeUnit.DAYS.toMillis(1)
        
        assertFalse(DateValidator.isFutureDate(now))
        assertFalse(DateValidator.isFutureDate(yesterday))
    }

    @Test
    fun `getAgeInDays returns correct value`() {
        val oneDayAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        
        assertEquals(1, DateValidator.getAgeInDays(oneDayAgo))
        assertEquals(30, DateValidator.getAgeInDays(thirtyDaysAgo))
    }

    @Test
    fun `formatForDisplay formats correctly`() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, 0, 15, 0, 0, 0) // Jan 15, 2024
        
        val formatted = DateValidator.formatForDisplay(calendar.timeInMillis, "yyyy-MM-dd")
        assertEquals("2024-01-15", formatted)
    }

    @Test
    fun `startOfDay returns midnight timestamp`() {
        val now = System.currentTimeMillis()
        val startOfDay = DateValidator.startOfDay(now)
        
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startOfDay
        
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
        assertEquals(0, calendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `endOfDay returns last millisecond of day`() {
        val now = System.currentTimeMillis()
        val endOfDay = DateValidator.endOfDay(now)
        
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = endOfDay
        
        assertEquals(23, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, calendar.get(Calendar.MINUTE))
        assertEquals(59, calendar.get(Calendar.SECOND))
        assertEquals(999, calendar.get(Calendar.MILLISECOND))
    }

    @Test
    fun `validateTransactionDate returns correct error for invalid timestamp`() {
        val result = DateValidator.validateTransactionDate(-1)
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertEquals("Invalid date", (result as ValidationUtils.ValidationResult.Error).message)
    }

    @Test
    fun `validateTransactionDate returns correct error for future date when not allowed`() {
        val future = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365)
        val result = DateValidator.validateTransactionDate(future, allowFuture = false)
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertEquals("Future dates are not allowed", (result as ValidationUtils.ValidationResult.Error).message)
    }

    @Test
    fun `validateTransactionDate returns correct error for too far in past`() {
        val tooOld = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365 * 11)
        val result = DateValidator.validateTransactionDate(tooOld)
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("too far in the past"))
    }

    @Test
    fun `validateTransactionDate returns correct error for too far in future`() {
        val tooFuture = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(366)
        val result = DateValidator.validateTransactionDate(tooFuture, allowFuture = true)
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("too far in the future"))
    }

    @Test
    fun `validateDateRange returns correct error for start after end`() {
        val now = System.currentTimeMillis()
        val yesterday = now - TimeUnit.DAYS.toMillis(1)
        
        val result = DateValidator.validateDateRange(now, yesterday)
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertEquals("Start date must be before end date", (result as ValidationUtils.ValidationResult.Error).message)
    }

    @Test
    fun `validateDateRange returns correct error for same date`() {
        val now = System.currentTimeMillis()
        val result = DateValidator.validateDateRange(now, now)
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertEquals("Start and end date cannot be the same", (result as ValidationUtils.ValidationResult.Error).message)
    }

    @Test
    fun `validateRecurringDate returns correct error for Feb 29 on non-leap year`() {
        val calendar = Calendar.getInstance()
        calendar.set(2023, 1, 29) // Feb 29, 2023 (not a leap year)
        
        val result = DateValidator.validateRecurringDate(
            calendar.timeInMillis, 
            DateValidator.RecurringFrequency.YEARLY
        )
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("leap year"))
    }

    @Test
    fun `RecurringFrequency enum has correct values`() {
        val frequencies = DateValidator.RecurringFrequency.values()
        assertEquals(4, frequencies.size)
        assertEquals(DateValidator.RecurringFrequency.DAILY, frequencies[0])
        assertEquals(DateValidator.RecurringFrequency.WEEKLY, frequencies[1])
        assertEquals(DateValidator.RecurringFrequency.MONTHLY, frequencies[2])
        assertEquals(DateValidator.RecurringFrequency.YEARLY, frequencies[3])
    }

    @Test
    fun `validateDateString returns correct error for empty string`() {
        val result = DateValidator.validateDateString("", "yyyy-MM-dd")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertEquals("Date cannot be empty", (result as ValidationUtils.ValidationResult.Error).message)
    }

    @Test
    fun `validateDateString returns correct error for invalid format`() {
        val result = DateValidator.validateDateString("not-a-date", "yyyy-MM-dd")
        assertTrue(result is ValidationUtils.ValidationResult.Error)
        assertTrue((result as ValidationUtils.ValidationResult.Error).message.contains("Invalid date format"))
    }
}
