package com.pyera.app.data.repository

import com.pyera.app.data.local.dao.TransactionDao
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.domain.analysis.AnalysisPeriod
import com.pyera.app.domain.analysis.BudgetAdherence
import com.pyera.app.domain.analysis.CategoryInsight
import com.pyera.app.domain.analysis.ChartDataPoint
import com.pyera.app.domain.analysis.DailySpending
import com.pyera.app.domain.analysis.DateRange
import com.pyera.app.domain.analysis.FinancialTip
import com.pyera.app.domain.analysis.MonthlySpending
import com.pyera.app.domain.analysis.PeriodComparison
import com.pyera.app.domain.analysis.SpendingAnomaly
import com.pyera.app.domain.analysis.SpendingHeatMapData
import com.pyera.app.domain.analysis.SpendingInsights
import com.pyera.app.domain.analysis.SpendingAnalyzer
import com.pyera.app.domain.analysis.WeeklyPattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AnalysisRepository that uses SpendingAnalyzer for analysis logic.
 * Provides comprehensive spending insights, anomaly detection, and personalized tips.
 */
@Singleton
class AnalysisRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val spendingAnalyzer: SpendingAnalyzer
) : AnalysisRepository {

    // Cache for anomalies to support dismiss functionality
    private val anomaliesCache = MutableStateFlow<List<SpendingAnomaly>>(emptyList())
    private val dismissedAnomalyIds = mutableSetOf<Long>()

    override fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    override suspend fun getSpendingInsights(
        period: AnalysisPeriod,
        userId: String
    ): SpendingInsights {
        return spendingAnalyzer.analyzeSpendingPatterns(userId, period)
    }

    override suspend fun getSpendingInsightsForRange(
        dateRange: DateRange,
        userId: String
    ): SpendingInsights {
        return spendingAnalyzer.analyzeSpendingPatterns(
            userId = userId,
            period = AnalysisPeriod.CUSTOM,
            customDateRange = dateRange
        )
    }

    override suspend fun getCategoryInsights(userId: String): List<CategoryInsight> {
        val categories = categoryRepository.getAllCategories().first()
        val budgetsWithSpending = getBudgetsWithSpending(userId)
        return spendingAnalyzer.getCategoryInsights(userId, categories, budgetsWithSpending)
    }

    override suspend fun getCategoryTrends(
        categoryId: Int,
        months: Int
    ): List<Pair<String, Double>> {
        val calendar = Calendar.getInstance()
        val endDate = System.currentTimeMillis()
        
        return (0 until months).map { monthOffset ->
            calendar.timeInMillis = endDate
            calendar.add(Calendar.MONTH, -monthOffset)
            
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            
            // Set to start of month
            calendar.set(year, month, 1, 0, 0, 0)
            val monthStart = calendar.timeInMillis
            
            // Set to end of month
            calendar.add(Calendar.MONTH, 1)
            val monthEnd = calendar.timeInMillis
            
            // Get transactions for this month and category
            val transactions = transactionDao.getTransactionsBetweenDates(monthStart, monthEnd)
                .first()
                .filter { it.categoryId == categoryId && it.type == "EXPENSE" }
            
            val total = transactions.sumOf { it.amount }
            val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, java.util.Locale.getDefault())
                ?: "${month + 1}"
            
            Pair("$monthName $year", total)
        }.reversed()
    }

    override suspend fun getTopSpendingCategories(
        limit: Int,
        period: AnalysisPeriod
    ): List<CategoryInsight> {
        return getCategoryInsights().take(limit)
    }

    override suspend fun detectSpendingAnomalies(
        userId: String,
        lookbackDays: Int
    ): List<SpendingAnomaly> {
        val anomalies = spendingAnalyzer.detectAnomalies(userId, lookbackDays)
            .filter { it.id !in dismissedAnomalyIds }
        
        anomaliesCache.value = anomalies
        return anomalies
    }

    override fun getActiveAnomalies(userId: String): Flow<List<SpendingAnomaly>> {
        return anomaliesCache.map { anomalies ->
            anomalies.filter { !it.isDismissed && it.id !in dismissedAnomalyIds }
        }
    }

    override suspend fun dismissAnomaly(anomalyId: Long) {
        dismissedAnomalyIds.add(anomalyId)
        anomaliesCache.value = anomaliesCache.value.filter { it.id != anomalyId }
    }

    override suspend fun dismissAllAnomalies(userId: String) {
        dismissedAnomalyIds.addAll(anomaliesCache.value.map { it.id })
        anomaliesCache.value = emptyList()
    }

    override suspend fun comparePeriods(period: AnalysisPeriod): PeriodComparison {
        val currentRange = getDateRangeForPeriod(period)
        val previousRange = getPreviousPeriodRange(currentRange)
        val categories = categoryRepository.getAllCategories().first()
        
        return spendingAnalyzer.comparePeriods(currentRange, previousRange, categories)
    }

    override suspend fun compareCustomPeriods(
        currentPeriod: DateRange,
        previousPeriod: DateRange
    ): PeriodComparison {
        val categories = categoryRepository.getAllCategories().first()
        return spendingAnalyzer.comparePeriods(currentPeriod, previousPeriod, categories)
    }

    override suspend fun getBudgetAdherence(userId: String): BudgetAdherence? {
        val budgetsWithSpending = getBudgetsWithSpending(userId)
        return if (budgetsWithSpending.isNotEmpty()) {
            spendingAnalyzer.calculateBudgetAdherence(budgetsWithSpending)
        } else null
    }

    override suspend fun getCategoryBudgetStatus(categoryId: Int): BudgetAdherence? {
        // Get budget for specific category
        val calendar = Calendar.getInstance()
        val endDate = System.currentTimeMillis()
        val startDate = endDate - (30L * 24 * 60 * 60 * 1000)
        
        val budgetFlow = budgetRepository.getBudgetsWithSpending(
            userId = "",
            startDate = startDate,
            endDate = endDate
        )
        
        val budgets = budgetFlow.first()
            .filter { it.categoryId == categoryId }
        
        return if (budgets.isNotEmpty()) {
            spendingAnalyzer.calculateBudgetAdherence(budgets)
        } else null
    }

    override suspend fun generatePersonalizedTips(userId: String): List<FinancialTip> {
        val categoryInsights = getCategoryInsights(userId)
        val budgetAdherence = getBudgetAdherence(userId)
        val periodComparison = comparePeriods(AnalysisPeriod.MONTHLY)
        
        return spendingAnalyzer.generateFinancialTips(
            userId,
            categoryInsights,
            budgetAdherence,
            periodComparison
        )
    }

    override suspend fun getTipsByType(
        tipType: com.pyera.app.domain.analysis.TipType
    ): List<FinancialTip> {
        return generatePersonalizedTips().filter { it.type == tipType }
    }

    override suspend fun getWeeklyPattern(userId: String): WeeklyPattern? {
        return spendingAnalyzer.analyzeWeeklyPattern(userId)
    }

    override suspend fun getSpendingPredictions(periods: Int): List<Double> {
        // Use historical data to predict future spending
        val monthlyData = getMonthlySpendingForChart(6)
        
        if (monthlyData.size < 3) {
            // Not enough data, return simple projection based on last month
            val lastSpending = monthlyData.lastOrNull()?.totalSpending ?: 0.0
            return List(periods) { lastSpending }
        }
        
        // Simple linear regression for prediction
        val spendingValues = monthlyData.map { it.totalSpending }
        val n = spendingValues.size
        val sumX = (0 until n).sumOf { it.toDouble() }
        val sumY = spendingValues.sum()
        val sumXY = spendingValues.mapIndexed { index, value -> index * value }.sum()
        val sumX2 = (0 until n).sumOf { it * it.toDouble() }
        
        val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
        val intercept = (sumY - slope * sumX) / n
        
        return (1..periods).map { period ->
            val prediction = intercept + slope * (n + period - 1)
            prediction.coerceAtLeast(0.0) // Ensure non-negative
        }
    }

    override suspend fun getDailySpendingForChart(
        startDate: Long,
        endDate: Long
    ): List<DailySpending> {
        return spendingAnalyzer.getDailySpendingData(startDate, endDate)
    }

    override suspend fun getMonthlySpendingForChart(months: Int): List<MonthlySpending> {
        return spendingAnalyzer.getMonthlySpendingData(months)
    }

    override suspend fun getCategoryBreakdownForChart(
        period: AnalysisPeriod
    ): List<ChartDataPoint> {
        val categoryInsights = getCategoryInsights()
        val totalSpending = categoryInsights.sumOf { it.currentPeriodSpending }
        
        return categoryInsights
            .filter { it.currentPeriodSpending > 0 }
            .map { insight ->
                ChartDataPoint(
                    label = insight.categoryName,
                    value = insight.currentPeriodSpending,
                    secondaryValue = if (totalSpending > 0) {
                        (insight.currentPeriodSpending / totalSpending) * 100
                    } else 0.0,
                    color = insight.categoryColor
                )
            }
            .sortedByDescending { it.value }
    }

    override suspend fun getSpendingHeatMap(year: Int, month: Int): SpendingHeatMapData {
        return spendingAnalyzer.getSpendingHeatMapData(year, month)
    }

    override suspend fun refreshAnalysis() {
        // Clear cache and force recalculation
        anomaliesCache.value = emptyList()
        // Re-detect anomalies
        detectSpendingAnomalies()
    }

    override suspend fun clearCache() {
        anomaliesCache.value = emptyList()
        dismissedAnomalyIds.clear()
    }

    // ==================== Helper Methods ====================

    private suspend fun getBudgetsWithSpending(userId: String): List<com.pyera.app.data.local.entity.BudgetWithSpending> {
        val calendar = Calendar.getInstance()
        val endDate = System.currentTimeMillis()
        val startDate = endDate - (30L * 24 * 60 * 60 * 1000)
        
        return budgetRepository.getBudgetsWithSpending(userId, startDate, endDate)
            .first()
    }

    private fun getDateRangeForPeriod(period: AnalysisPeriod): DateRange {
        val endDate = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = endDate
        
        val startDate = when (period) {
            AnalysisPeriod.WEEKLY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis
            }
            AnalysisPeriod.MONTHLY -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }
            AnalysisPeriod.QUARTERLY -> {
                calendar.add(Calendar.MONTH, -3)
                calendar.timeInMillis
            }
            AnalysisPeriod.YEARLY -> {
                calendar.add(Calendar.YEAR, -1)
                calendar.timeInMillis
            }
            AnalysisPeriod.CUSTOM -> endDate - (30L * 24 * 60 * 60 * 1000)
        }
        
        return DateRange(startDate, endDate)
    }

    private fun getPreviousPeriodRange(currentRange: DateRange): DateRange {
        val duration = currentRange.endDate - currentRange.startDate
        return DateRange(
            startDate = currentRange.startDate - duration,
            endDate = currentRange.startDate
        )
    }
}
