package com.pyera.app.util.validators

import com.pyera.app.util.ValidationUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Validator for dates in financial transactions
 */
object DateValidator {

    private const val ONE_YEAR_MILLIS = 365L * 24 * 60 * 60 * 1000
    private const val MAX_FUTURE_DAYS = 365
    private const val MAX_PAST_YEARS = 10

    /**
     * Validates a transaction date
     * @param timestamp The date timestamp in milliseconds
     * @param allowFuture Whether to allow future dates
     * @param maxFutureDays Maximum days in the future allowed (default 365)
     * @return ValidationResult indicating success or specific error
     */
    fun validateTransactionDate(
        timestamp: Long,
        allowFuture: Boolean = false,
        maxFutureDays: Int = MAX_FUTURE_DAYS
    ): ValidationUtils.ValidationResult {
        return when {
            timestamp <= 0 -> 
                ValidationUtils.ValidationResult.Error("Invalid date")
            
            !isValidTimestamp(timestamp) -> 
                ValidationUtils.ValidationResult.Error("Invalid timestamp value")
            
            isTooFarInPast(timestamp) -> 
                ValidationUtils.ValidationResult.Error("Date is too far in the past (max $MAX_PAST_YEARS years)")
            
            !allowFuture && isFutureDate(timestamp) -> 
                ValidationUtils.ValidationResult.Error("Future dates are not allowed")
            
            allowFuture && isTooFarInFuture(timestamp, maxFutureDays) -> 
                ValidationUtils.ValidationResult.Error("Date is too far in the future (max $maxFutureDays days)")
            
            isToday(timestamp) -> 
                // Today's date is always valid
                ValidationUtils.ValidationResult.Success
            
            else -> ValidationUtils.ValidationResult.Success
        }
    }

    /**
     * Validates a date string in various formats
     * @param dateString The date string to validate
     * @param format The expected date format (default: "yyyy-MM-dd")
     * @param allowFuture Whether to allow future dates
     * @return ValidationResult indicating success or specific error
     */
    fun validateDateString(
        dateString: String,
        format: String = "yyyy-MM-dd",
        allowFuture: Boolean = false
    ): ValidationUtils.ValidationResult {
        return when {
            dateString.isBlank() -> 
                ValidationUtils.ValidationResult.Error("Date cannot be empty")
            
            !isValidFormat(dateString, format) -> 
                ValidationUtils.ValidationResult.Error("Invalid date format. Expected: $format")
            
            else -> {
                val timestamp = parseDate(dateString, format)
                    ?: return ValidationUtils.ValidationResult.Error("Could not parse date")
                validateTransactionDate(timestamp, allowFuture)
            }
        }
    }

    /**
     * Validates a date range (start date to end date)
     * @param startTimestamp Start date timestamp
     * @param endTimestamp End date timestamp
     * @param maxRangeDays Maximum allowed range in days (0 = unlimited)
     * @return ValidationResult indicating success or specific error
     */
    fun validateDateRange(
        startTimestamp: Long,
        endTimestamp: Long,
        maxRangeDays: Int = 0
    ): ValidationUtils.ValidationResult {
        return when {
            startTimestamp <= 0 -> 
                ValidationUtils.ValidationResult.Error("Invalid start date")
            
            endTimestamp <= 0 -> 
                ValidationUtils.ValidationResult.Error("Invalid end date")
            
            startTimestamp > endTimestamp -> 
                ValidationUtils.ValidationResult.Error("Start date must be before end date")
            
            startTimestamp == endTimestamp -> 
                ValidationUtils.ValidationResult.Error("Start and end date cannot be the same")
            
            maxRangeDays > 0 && isRangeTooLarge(startTimestamp, endTimestamp, maxRangeDays) -> 
                ValidationUtils.ValidationResult.Error("Date range too large (max $maxRangeDays days)")
            
            isTooFarInPast(startTimestamp) -> 
                ValidationUtils.ValidationResult.Error("Start date is too far in the past")
            
            else -> ValidationUtils.ValidationResult.Success
        }
    }

    /**
     * Validates a recurring transaction date
     * Ensures the date is valid for recurring patterns
     */
    fun validateRecurringDate(
        timestamp: Long,
        frequency: RecurringFrequency
    ): ValidationUtils.ValidationResult {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        
        return when (frequency) {
            RecurringFrequency.DAILY -> ValidationUtils.ValidationResult.Success
            
            RecurringFrequency.WEEKLY -> {
                // Any day is valid for weekly
                ValidationUtils.ValidationResult.Success
            }
            
            RecurringFrequency.MONTHLY -> {
                // Validate day of month is valid
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                if (dayOfMonth > maxDay) {
                    ValidationUtils.ValidationResult.Error("Invalid day for monthly recurrence")
                } else {
                    ValidationUtils.ValidationResult.Success
                }
            }
            
            RecurringFrequency.YEARLY -> {
                // Check for February 29 on non-leap years
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                if (month == Calendar.FEBRUARY && day == 29) {
                    if (!isLeapYear(calendar.get(Calendar.YEAR))) {
                        ValidationUtils.ValidationResult.Error("February 29 is only valid in leap years")
                    } else {
                        ValidationUtils.ValidationResult.Success
                    }
                } else {
                    ValidationUtils.ValidationResult.Success
                }
            }
        }
    }

    /**
     * Checks if a date is today
     */
    fun isToday(timestamp: Long): Boolean {
        val today = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = timestamp }
        
        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Checks if a date is in the future
     */
    fun isFutureDate(timestamp: Long): Boolean {
        return timestamp > System.currentTimeMillis()
    }

    /**
     * Gets the age of a transaction in days
     */
    fun getAgeInDays(timestamp: Long): Int {
        val diff = System.currentTimeMillis() - timestamp
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    private fun isLeapYear(year: Int): Boolean = GregorianCalendar().isLeapYear(year)

    /**
     * Formats a timestamp for display
     */
    fun formatForDisplay(
        timestamp: Long, 
        format: String = "yyyy-MM-dd",
        locale: Locale = Locale.getDefault()
    ): String {
        val sdf = SimpleDateFormat(format, locale)
        return sdf.format(Date(timestamp))
    }

    private fun isValidTimestamp(timestamp: Long): Boolean {
        // Check if timestamp is within reasonable bounds
        // Unix epoch starts at 1970, so minimum is 0
        // Maximum is around year 3000 (roughly)
        return timestamp in 0..32503680000000L
    }

    private fun isTooFarInPast(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -MAX_PAST_YEARS)
        return timestamp < calendar.timeInMillis
    }

    private fun isTooFarInFuture(timestamp: Long, maxDays: Int): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, maxDays)
        return timestamp > calendar.timeInMillis
    }

    private fun isValidFormat(dateString: String, format: String): Boolean {
        return try {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(dateString)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun parseDate(dateString: String, format: String): Long? {
        return try {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            sdf.timeZone = TimeZone.getDefault()
            sdf.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }

    private fun isRangeTooLarge(start: Long, end: Long, maxDays: Int): Boolean {
        val diff = end - start
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        return days > maxDays
    }

    /**
     * Recurring frequency enum
     */
    enum class RecurringFrequency {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }

    /**
     * Gets the start of day timestamp
     */
    fun startOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Gets the end of day timestamp
     */
    fun endOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
