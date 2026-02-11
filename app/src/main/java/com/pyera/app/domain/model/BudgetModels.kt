package com.pyera.app.domain.model

/**
 * Budget period types for flexible budget planning.
 */
enum class BudgetPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * Enum representing the health status of a budget.
 */
enum class BudgetStatus {
    HEALTHY,      // Under 50% spent
    ON_TRACK,     // 50% to alert threshold
    WARNING,      // Above alert threshold but under 100%
    OVER_BUDGET   // Over 100% spent
}

/**
 * Domain model representing a budget with calculated spending information.
 */
data class BudgetWithSpending(
    val id: Int,
    val userId: String,
    val categoryId: Int,
    val categoryName: String,
    val categoryColor: Int,
    val categoryIcon: String?,
    val amount: Double,
    val period: BudgetPeriod,
    val startDate: Long,
    val isActive: Boolean,
    val alertThreshold: Float,
    val spentAmount: Double,
    val remainingAmount: Double,
    val progressPercentage: Float,
    val isOverBudget: Boolean,
    val daysRemaining: Int
) {
    val status: BudgetStatus
        get() = when {
            isOverBudget -> BudgetStatus.OVER_BUDGET
            progressPercentage >= alertThreshold -> BudgetStatus.WARNING
            progressPercentage >= 0.5f -> BudgetStatus.ON_TRACK
            else -> BudgetStatus.HEALTHY
        }
}
