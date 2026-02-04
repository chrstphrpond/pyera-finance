package com.pyera.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Budget period types for flexible budget planning
 */
enum class BudgetPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * Room entity representing a budget in the database.
 * Budgets are linked to categories and track spending limits over specific periods.
 */
@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("categoryId"),
        Index("userId"),
        Index("isActive")
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String = "", // For multi-user support and Firebase sync
    val categoryId: Int,
    val amount: Double,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val startDate: Long = System.currentTimeMillis(), // When the budget period starts
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val alertThreshold: Float = 0.8f // Alert when spending reaches 80% of budget
)

/**
 * Data class representing budget with calculated spending information.
 * This is used for UI display and combines budget data with actual spending.
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
    /**
     * Status of the budget based on spending progress
     */
    val status: BudgetStatus
        get() = when {
            isOverBudget -> BudgetStatus.OVER_BUDGET
            progressPercentage >= alertThreshold -> BudgetStatus.WARNING
            progressPercentage >= 0.5f -> BudgetStatus.ON_TRACK
            else -> BudgetStatus.HEALTHY
        }
}

/**
 * Enum representing the health status of a budget
 */
enum class BudgetStatus {
    HEALTHY,      // Under 50% spent
    ON_TRACK,     // 50% to alert threshold
    WARNING,      // Above alert threshold but under 100%
    OVER_BUDGET   // Over 100% spent
}

/**
 * Summary of all budgets for a user
 */
data class BudgetSummary(
    val totalBudgets: Int,
    val totalBudgetAmount: Double,
    val totalSpent: Double,
    val totalRemaining: Double,
    val overallProgress: Float,
    val overBudgetCount: Int,
    val warningCount: Int,
    val healthyCount: Int
)
