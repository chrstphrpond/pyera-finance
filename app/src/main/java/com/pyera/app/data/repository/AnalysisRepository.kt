package com.pyera.app.data.repository

import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.domain.analysis.AnalysisPeriod
import com.pyera.app.domain.analysis.BudgetAdherence
import com.pyera.app.domain.analysis.CategoryInsight
import com.pyera.app.domain.analysis.DateRange
import com.pyera.app.domain.analysis.FinancialTip
import com.pyera.app.domain.analysis.PeriodComparison
import com.pyera.app.domain.analysis.SpendingAnomaly
import com.pyera.app.domain.analysis.SpendingInsights
import com.pyera.app.domain.analysis.WeeklyPattern
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for comprehensive spending analysis and insights.
 * Provides methods for analyzing spending patterns, detecting anomalies,
 * comparing periods, and generating personalized financial insights.
 */
interface AnalysisRepository {

    // ==================== Core Analysis ====================

    /**
     * Get all transactions as a flow
     */
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    /**
     * Get spending insights for a specific time period
     * @param period The analysis period (weekly, monthly, quarterly, yearly)
     * @param userId The user ID for user-specific analysis
     * @return Spending insights including trends, top categories, and generated insights
     */
    suspend fun getSpendingInsights(
        period: AnalysisPeriod = AnalysisPeriod.MONTHLY,
        userId: String = ""
    ): SpendingInsights

    /**
     * Get spending insights for a custom date range
     * @param dateRange Custom start and end dates
     * @param userId The user ID for user-specific analysis
     */
    suspend fun getSpendingInsightsForRange(
        dateRange: DateRange,
        userId: String = ""
    ): SpendingInsights

    // ==================== Category Analysis ====================

    /**
     * Get detailed insights for all spending categories
     * @param userId The user ID
     * @return List of category insights with trends and comparisons
     */
    suspend fun getCategoryInsights(userId: String = ""): List<CategoryInsight>

    /**
     * Get spending trends for a specific category over multiple months
     * @param categoryId The category ID to analyze
     * @param months Number of months to look back
     * @return List of monthly spending amounts for the category
     */
    suspend fun getCategoryTrends(
        categoryId: Int,
        months: Int = 6
    ): List<Pair<String, Double>>

    /**
     * Get top spending categories for a given period
     * @param limit Maximum number of categories to return
     * @param period The analysis period
     */
    suspend fun getTopSpendingCategories(
        limit: Int = 5,
        period: AnalysisPeriod = AnalysisPeriod.MONTHLY
    ): List<CategoryInsight>

    // ==================== Anomaly Detection ====================

    /**
     * Detect spending anomalies in recent transactions
     * @param userId The user ID
     * @param lookbackDays Number of days to look back for anomalies
     * @return List of detected anomalies sorted by severity
     */
    suspend fun detectSpendingAnomalies(
        userId: String = "",
        lookbackDays: Int = 30
    ): List<SpendingAnomaly>

    /**
     * Get active (non-dismissed) anomalies
     * @param userId The user ID
     */
    fun getActiveAnomalies(userId: String = ""): Flow<List<SpendingAnomaly>>

    /**
     * Dismiss a specific anomaly
     * @param anomalyId The anomaly ID to dismiss
     */
    suspend fun dismissAnomaly(anomalyId: Long)

    /**
     * Dismiss all anomalies for a user
     * @param userId The user ID
     */
    suspend fun dismissAllAnomalies(userId: String = "")

    // ==================== Period Comparison ====================

    /**
     * Compare spending between current and previous period
     * @param period The analysis period to compare
     * @return Detailed comparison including trends and category breakdowns
     */
    suspend fun comparePeriods(
        period: AnalysisPeriod = AnalysisPeriod.MONTHLY
    ): PeriodComparison

    /**
     * Compare spending between two custom date ranges
     * @param currentPeriod The current period date range
     * @param previousPeriod The previous period date range for comparison
     */
    suspend fun compareCustomPeriods(
        currentPeriod: DateRange,
        previousPeriod: DateRange
    ): PeriodComparison

    // ==================== Budget Analysis ====================

    /**
     * Get budget adherence metrics
     * @param userId The user ID
     * @return Comprehensive budget adherence data including over-budget alerts
     */
    suspend fun getBudgetAdherence(userId: String = ""): BudgetAdherence?

    /**
     * Get budget adherence for a specific category
     * @param categoryId The category ID
     */
    suspend fun getCategoryBudgetStatus(categoryId: Int): BudgetAdherence?

    // ==================== Personalized Tips ====================

    /**
     * Generate personalized financial tips based on user's spending patterns
     * @param userId The user ID
     * @return List of personalized tips sorted by priority
     */
    suspend fun generatePersonalizedTips(userId: String = ""): List<FinancialTip>

    /**
     * Get tips filtered by type
     * @param tipType The type of tips to retrieve
     */
    suspend fun getTipsByType(tipType: com.pyera.app.domain.analysis.TipType): List<FinancialTip>

    // ==================== Pattern Analysis ====================

    /**
     * Analyze weekly spending patterns
     * @param userId The user ID
     * @return Weekly pattern analysis including day-of-week trends
     */
    suspend fun getWeeklyPattern(userId: String = ""): WeeklyPattern?

    /**
     * Get spending predictions for the next period
     * @param periods Number of periods to predict
     * @return Predicted spending amounts
     */
    suspend fun getSpendingPredictions(periods: Int = 1): List<Double>

    // ==================== Chart Data ====================

    /**
     * Get daily spending data for line charts
     * @param startDate Start date timestamp
     * @param endDate End date timestamp
     * @return List of daily spending amounts
     */
    suspend fun getDailySpendingForChart(
        startDate: Long,
        endDate: Long
    ): List<com.pyera.app.domain.analysis.DailySpending>

    /**
     * Get monthly spending data for trend charts
     * @param months Number of months to include
     * @return List of monthly spending summaries
     */
    suspend fun getMonthlySpendingForChart(
        months: Int = 6
    ): List<com.pyera.app.domain.analysis.MonthlySpending>

    /**
     * Get category breakdown data for pie/donut charts
     * @param period The analysis period
     * @return List of category spending data
     */
    suspend fun getCategoryBreakdownForChart(
        period: AnalysisPeriod = AnalysisPeriod.MONTHLY
    ): List<com.pyera.app.domain.analysis.ChartDataPoint>

    /**
     * Get heat map data for daily spending visualization
     * @param year Year to analyze
     * @param month Month to analyze (1-12)
     * @return Heat map data for the month
     */
    suspend fun getSpendingHeatMap(
        year: Int,
        month: Int
    ): com.pyera.app.domain.analysis.SpendingHeatMapData

    // ==================== Data Refresh ====================

    /**
     * Refresh all analysis data
     * Forces re-calculation of insights and anomalies
     */
    suspend fun refreshAnalysis()

    /**
     * Clear cached analysis data
     */
    suspend fun clearCache()
}
