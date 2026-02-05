package com.pyera.app.domain.analysis

import com.pyera.app.data.local.entity.TransactionEntity

/**
 * Represents different analysis periods for spending insights
 */
enum class AnalysisPeriod {
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    YEARLY,
    CUSTOM
}

/**
 * Represents the direction of a spending trend
 */
enum class TrendDirection {
    INCREASING,
    DECREASING,
    STABLE
}

/**
 * Types of spending anomalies that can be detected
 */
enum class AnomalyType {
    UNUSUAL_AMOUNT,           // Transaction amount is unusually high
    UNUSUAL_MERCHANT,         // New or rare merchant
    UNUSUAL_TIME,             // Transaction at unusual time
    UNUSUAL_CATEGORY,         // Spending in unusual category
    DUPLICATE_TRANSACTION,    // Possible duplicate
    FREQUENCY_SPIKE           // Sudden increase in transaction frequency
}

/**
 * Severity levels for anomalies
 */
enum class AnomalySeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Types of personalized financial tips
 */
enum class TipType {
    SAVINGS_OPPORTUNITY,      // Ways to save money
    BUDGET_ALERT,             // Budget-related warnings
    SPENDING_PATTERN,         // Insights about spending patterns
    GOAL_PROGRESS,            // Progress towards financial goals
    GENERAL                   // General financial tips
}

/**
 * Comprehensive spending insights for a given period
 */
data class SpendingInsights(
    val totalSpending: Double,
    val averageDaily: Double,
    val topCategories: List<CategorySpending>,
    val spendingTrend: TrendDirection,
    val percentageChange: Double,
    val insights: List<String>,
    val period: AnalysisPeriod,
    val startDate: Long,
    val endDate: Long,
    val transactionCount: Int
)

/**
 * Spending data for a specific category
 */
data class CategorySpending(
    val categoryId: Int,
    val categoryName: String,
    val categoryIcon: String?,
    val categoryColor: Int,
    val amount: Double,
    val percentageOfTotal: Double,
    val transactionCount: Int,
    val trend: TrendDirection,
    val changeFromPreviousPeriod: Double
)

/**
 * Detailed insight for a category including trends
 */
data class CategoryInsight(
    val categoryId: Int,
    val categoryName: String,
    val categoryIcon: String?,
    val categoryColor: Int,
    val currentPeriodSpending: Double,
    val previousPeriodSpending: Double,
    val percentageChange: Double,
    val averageTransactionAmount: Double,
    val transactionCount: Int,
    val monthlyAverage: Double,
    val trendDirection: TrendDirection,
    val isBudgeted: Boolean,
    val budgetAmount: Double?,
    val budgetSpentPercentage: Float?
)

/**
 * Detected spending anomaly
 */
data class SpendingAnomaly(
    val id: Long,
    val transaction: TransactionEntity,
    val anomalyType: AnomalyType,
    val severity: AnomalySeverity,
    val description: String,
    val detectedAt: Long,
    val isDismissed: Boolean = false,
    val suggestedAction: String? = null
)

/**
 * Comparison between two time periods
 */
data class PeriodComparison(
    val currentPeriod: DateRange,
    val previousPeriod: DateRange,
    val currentSpending: Double,
    val previousSpending: Double,
    val absoluteChange: Double,
    val percentageChange: Double,
    val trendDirection: TrendDirection,
    val categoryComparisons: List<CategoryComparison>,
    val transactionCountChange: Int
)

/**
 * Date range for analysis periods
 */
data class DateRange(
    val startDate: Long,
    val endDate: Long
) {
    val durationDays: Int
        get() = ((endDate - startDate) / (1000 * 60 * 60 * 24)).toInt()
}

/**
 * Category-specific period comparison
 */
data class CategoryComparison(
    val categoryId: Int,
    val categoryName: String,
    val currentAmount: Double,
    val previousAmount: Double,
    val absoluteChange: Double,
    val percentageChange: Double,
    val trendDirection: TrendDirection
)

/**
 * Budget adherence data
 */
data class BudgetAdherence(
    val totalBudgets: Int,
    val totalBudgetAmount: Double,
    val totalSpent: Double,
    val overallAdherencePercentage: Float,
    val overBudgetCategories: List<BudgetStatusInfo>,
    val nearLimitCategories: List<BudgetStatusInfo>,
    val healthyCategories: List<BudgetStatusInfo>
)

/**
 * Individual budget status information
 */
data class BudgetStatusInfo(
    val budgetId: Int,
    val categoryName: String,
    val categoryColor: Int,
    val budgetAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val percentageUsed: Float,
    val daysRemaining: Int,
    val projectedOverspend: Double?
)

/**
 * Personalized financial tip
 */
data class FinancialTip(
    val id: String,
    val type: TipType,
    val title: String,
    val description: String,
    val priority: Int,  // Higher number = higher priority
    val actionLabel: String? = null,
    val actionRoute: String? = null,
    val icon: String? = null
)

/**
 * Weekly spending pattern (day of week analysis)
 */
data class WeeklyPattern(
    val dayOfWeekSpending: Map<Int, Double>, // 1 = Sunday, 7 = Saturday
    val highestSpendingDay: Int,
    val lowestSpendingDay: Int,
    val weekendVsWeekdayRatio: Double
)

/**
 * Daily spending point for charts
 */
data class DailySpending(
    val date: Long,
    val amount: Double,
    val transactionCount: Int
)

/**
 * Monthly spending summary for trend charts
 */
data class MonthlySpending(
    val yearMonth: String, // Format: "2024-01"
    val monthName: String,
    val totalSpending: Double,
    val totalIncome: Double,
    val netSavings: Double
)

/**
 * Complete insights state for UI
 */
sealed class InsightsState {
    data object Loading : InsightsState()
    data class Success(
        val spendingInsights: SpendingInsights,
        val anomalies: List<SpendingAnomaly>,
        val categoryInsights: List<CategoryInsight>,
        val budgetAdherence: BudgetAdherence?,
        val tips: List<FinancialTip>,
        val periodComparison: PeriodComparison?,
        val weeklyPattern: WeeklyPattern?
    ) : InsightsState()
    data class Error(val message: String) : InsightsState()
}

/**
 * Data class for chart data points
 */
data class ChartDataPoint(
    val label: String,
    val value: Double,
    val secondaryValue: Double? = null,
    val color: Int? = null
)

/**
 * Heat map data for daily spending visualization
 */
data class SpendingHeatMapData(
    val month: Int,
    val year: Int,
    val dailyAmounts: Map<Int, Double>, // Day of month -> Amount
    val maxAmount: Double,
    val minAmount: Double,
    val averageAmount: Double
)
