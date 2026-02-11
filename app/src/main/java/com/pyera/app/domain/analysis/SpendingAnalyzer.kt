package com.pyera.app.domain.analysis

import com.pyera.app.domain.model.BudgetWithSpending
import com.pyera.app.domain.model.Category
import com.pyera.app.domain.model.Transaction
import com.pyera.app.domain.repository.SpendingDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Core spending analysis engine that processes transactions and generates insights.
 * This analyzer uses statistical methods to detect patterns, anomalies, and trends.
 */
@Singleton
class SpendingAnalyzer @Inject constructor(
    private val spendingDataRepository: SpendingDataRepository
) {

    companion object {
        // Thresholds for anomaly detection
        private const val UNUSUAL_AMOUNT_THRESHOLD = 2.5 // Standard deviations from mean
        private const val HIGH_SEVERITY_THRESHOLD = 4.0
        private const val DUPLICATE_TIME_WINDOW_MS = 24 * 60 * 60 * 1000 // 24 hours
        private const val FREQUENCY_SPIKE_THRESHOLD = 2.0 // 2x normal frequency
        
        // Budget alert thresholds
        private const val NEAR_LIMIT_THRESHOLD = 0.8f // 80% of budget
        private const val MIN_TRANSACTIONS_FOR_STATS = 5
    }

    // ==================== Main Analysis Methods ====================

    /**
     * Analyze spending patterns for a specific user and time period
     */
    suspend fun analyzeSpendingPatterns(
        userId: String,
        period: AnalysisPeriod,
        customDateRange: DateRange? = null
    ): SpendingInsights {
        val dateRange = customDateRange ?: getDateRangeForPeriod(period)
        val previousPeriodRange = getPreviousPeriodRange(dateRange)
        
        val transactions = getTransactionsForRange(dateRange)
        val previousTransactions = getTransactionsForRange(previousPeriodRange)
        
        val expenses = transactions.filter { it.type == "EXPENSE" }
        val previousExpenses = previousTransactions.filter { it.type == "EXPENSE" }
        
        val totalSpending = expenses.sumOf { it.amount }
        val previousSpending = previousExpenses.sumOf { it.amount }
        
        val percentageChange = if (previousSpending > 0) {
            ((totalSpending - previousSpending) / previousSpending) * 100
        } else 0.0
        
        val trend = when {
            percentageChange > 5 -> TrendDirection.INCREASING
            percentageChange < -5 -> TrendDirection.DECREASING
            else -> TrendDirection.STABLE
        }
        
        val dailyAverage = totalSpending / maxOf(dateRange.durationDays, 1)
        
        return SpendingInsights(
            totalSpending = totalSpending,
            averageDaily = dailyAverage,
            topCategories = emptyList(), // Will be populated when categories are available
            spendingTrend = trend,
            percentageChange = percentageChange,
            insights = generateInsights(totalSpending, previousSpending, trend, percentageChange),
            period = period,
            startDate = dateRange.startDate,
            endDate = dateRange.endDate,
            transactionCount = expenses.size
        )
    }

    /**
     * Detect spending anomalies in user's transactions
     */
    suspend fun detectAnomalies(
        userId: String,
        lookbackDays: Int = 30
    ): List<SpendingAnomaly> {
        val endDate = System.currentTimeMillis()
        val startDate = endDate - (lookbackDays * 24 * 60 * 60 * 1000L)
        val dateRange = DateRange(startDate, endDate)
        
        val transactions = getTransactionsForRange(dateRange)
            .filter { it.type == "EXPENSE" }
            .sortedByDescending { it.date }
        
        if (transactions.size < MIN_TRANSACTIONS_FOR_STATS) {
            return emptyList()
        }
        
        val anomalies = mutableListOf<SpendingAnomaly>()
        
        // Detect unusual amounts
        anomalies.addAll(detectUnusualAmounts(transactions))
        
        // Detect duplicate transactions
        anomalies.addAll(detectDuplicateTransactions(transactions))
        
        // Detect frequency spikes
        anomalies.addAll(detectFrequencySpikes(transactions, lookbackDays))
        
        return anomalies.sortedByDescending { it.severity.ordinal }
    }

    /**
     * Compare spending between two time periods
     */
    suspend fun comparePeriods(
        currentPeriod: DateRange,
        previousPeriod: DateRange,
        categories: List<Category> = emptyList()
    ): PeriodComparison {
        val currentTransactions = getTransactionsForRange(currentPeriod)
            .filter { it.type == "EXPENSE" }
        val previousTransactions = getTransactionsForRange(previousPeriod)
            .filter { it.type == "EXPENSE" }
        
        val currentSpending = currentTransactions.sumOf { it.amount }
        val previousSpending = previousTransactions.sumOf { it.amount }
        
        val absoluteChange = currentSpending - previousSpending
        val percentageChange = if (previousSpending > 0) {
            (absoluteChange / previousSpending) * 100
        } else 0.0
        
        val trend = when {
            percentageChange > 5 -> TrendDirection.INCREASING
            percentageChange < -5 -> TrendDirection.DECREASING
            else -> TrendDirection.STABLE
        }
        
        val categoryMap = categories.associateBy { it.id }
        
        val categoryComparisons = generateCategoryComparisons(
            currentTransactions,
            previousTransactions,
            categoryMap
        )
        
        return PeriodComparison(
            currentPeriod = currentPeriod,
            previousPeriod = previousPeriod,
            currentSpending = currentSpending,
            previousSpending = previousSpending,
            absoluteChange = absoluteChange,
            percentageChange = percentageChange,
            trendDirection = trend,
            categoryComparisons = categoryComparisons,
            transactionCountChange = currentTransactions.size - previousTransactions.size
        )
    }

    /**
     * Get detailed insights for each spending category
     */
    suspend fun getCategoryInsights(
        userId: String,
        categories: List<Category>,
        budgetsWithSpending: List<BudgetWithSpending> = emptyList()
    ): List<CategoryInsight> {
        val endDate = System.currentTimeMillis()
        val startDate = endDate - (30L * 24 * 60 * 60 * 1000) // Last 30 days
        val previousStartDate = startDate - (30L * 24 * 60 * 60 * 1000)
        
        val currentTransactions = getTransactionsForRange(DateRange(startDate, endDate))
            .filter { it.type == "EXPENSE" }
        val previousTransactions = getTransactionsForRange(DateRange(previousStartDate, startDate))
            .filter { it.type == "EXPENSE" }
        
        val budgetMap = budgetsWithSpending.associateBy { it.categoryId }
        
        return categories.filter { it.type == "EXPENSE" }.map { category ->
            val categoryTransactions = currentTransactions.filter { it.categoryId == category.id }
            val previousCategoryTransactions = previousTransactions.filter { it.categoryId == category.id }
            
            val currentSpending = categoryTransactions.sumOf { it.amount }
            val previousSpending = previousCategoryTransactions.sumOf { it.amount }
            
            val percentageChange = if (previousSpending > 0) {
                ((currentSpending - previousSpending) / previousSpending) * 100
            } else 0.0
            
            val trend = when {
                percentageChange > 10 -> TrendDirection.INCREASING
                percentageChange < -10 -> TrendDirection.DECREASING
                else -> TrendDirection.STABLE
            }
            
            val avgTransaction = if (categoryTransactions.isNotEmpty()) {
                currentSpending / categoryTransactions.size
            } else 0.0
            
            val budget = budgetMap[category.id]
            
            CategoryInsight(
                categoryId = category.id,
                categoryName = category.name,
                categoryIcon = category.icon,
                categoryColor = category.color,
                currentPeriodSpending = currentSpending,
                previousPeriodSpending = previousSpending,
                percentageChange = percentageChange,
                averageTransactionAmount = avgTransaction,
                transactionCount = categoryTransactions.size,
                monthlyAverage = currentSpending,
                trendDirection = trend,
                isBudgeted = budget != null,
                budgetAmount = budget?.amount,
                budgetSpentPercentage = budget?.progressPercentage
            )
        }.sortedByDescending { it.currentPeriodSpending }
    }

    /**
     * Calculate budget adherence metrics
     */
    fun calculateBudgetAdherence(
        budgetsWithSpending: List<BudgetWithSpending>
    ): BudgetAdherence {
        val totalBudget = budgetsWithSpending.sumOf { it.amount }
        val totalSpent = budgetsWithSpending.sumOf { it.spentAmount }
        val adherencePercentage = if (totalBudget > 0) {
            ((totalBudget - totalSpent) / totalBudget).toFloat().coerceIn(0f, 1f)
        } else 1f
        
        val budgetInfos = budgetsWithSpending.map { budget ->
            BudgetStatusInfo(
                budgetId = budget.id,
                categoryName = budget.categoryName,
                categoryColor = budget.categoryColor,
                budgetAmount = budget.amount,
                spentAmount = budget.spentAmount,
                remainingAmount = budget.remainingAmount,
                percentageUsed = budget.progressPercentage,
                daysRemaining = budget.daysRemaining,
                projectedOverspend = calculateProjectedOverspend(budget)
            )
        }
        
        return BudgetAdherence(
            totalBudgets = budgetsWithSpending.size,
            totalBudgetAmount = totalBudget,
            totalSpent = totalSpent,
            overallAdherencePercentage = adherencePercentage,
            overBudgetCategories = budgetInfos.filter { it.percentageUsed > 1f }
                .sortedByDescending { it.percentageUsed },
            nearLimitCategories = budgetInfos.filter { 
                it.percentageUsed in NEAR_LIMIT_THRESHOLD..1f 
            }.sortedByDescending { it.percentageUsed },
            healthyCategories = budgetInfos.filter { it.percentageUsed < NEAR_LIMIT_THRESHOLD }
                .sortedByDescending { it.percentageUsed }
        )
    }

    /**
     * Generate personalized financial tips based on spending data
     */
    suspend fun generateFinancialTips(
        userId: String,
        categoryInsights: List<CategoryInsight>,
        budgetAdherence: BudgetAdherence?,
        periodComparison: PeriodComparison?
    ): List<FinancialTip> {
        val tips = mutableListOf<FinancialTip>()
        var priority = 100
        
        // Tip: Spending increase
        periodComparison?.let { comparison ->
            if (comparison.percentageChange > 20) {
                tips.add(FinancialTip(
                    id = "spending_spike",
                    type = TipType.SPENDING_PATTERN,
                    title = "Spending Increase Detected",
                    description = "Your spending increased by ${String.format("%.1f", comparison.percentageChange)}% compared to last month. Consider reviewing your expenses.",
                    priority = priority--,
                    actionLabel = "View Details",
                    actionRoute = "transactions",
                    icon = "trending_up"
                ))
            }
        }
        
        // Tip: Budget warnings
        budgetAdherence?.let { adherence ->
            adherence.overBudgetCategories.firstOrNull()?.let { overBudget ->
                tips.add(FinancialTip(
                    id = "over_budget_${overBudget.budgetId}",
                    type = TipType.BUDGET_ALERT,
                    title = "Over Budget: ${overBudget.categoryName}",
                    description = "You've exceeded your ${overBudget.categoryName} budget by ${String.format("%.0f", (overBudget.percentageUsed - 1) * 100)}%.",
                    priority = priority--,
                    actionLabel = "Adjust Budget",
                    actionRoute = "budget/edit/${overBudget.budgetId}",
                    icon = "warning"
                ))
            }
            
            adherence.nearLimitCategories.firstOrNull()?.let { nearLimit ->
                tips.add(FinancialTip(
                    id = "near_limit_${nearLimit.budgetId}",
                    type = TipType.BUDGET_ALERT,
                    title = "Budget Alert: ${nearLimit.categoryName}",
                    description = "You've used ${String.format("%.0f", nearLimit.percentageUsed * 100)}% of your ${nearLimit.categoryName} budget.",
                    priority = priority--,
                    actionLabel = "View Budget",
                    actionRoute = "budget/detail/${nearLimit.budgetId}",
                    icon = "info"
                ))
            }
        }
        
        // Tip: Top spending category
        categoryInsights.firstOrNull()?.let { topCategory ->
            if (topCategory.currentPeriodSpending > 0) {
                tips.add(FinancialTip(
                    id = "top_category",
                    type = TipType.SPENDING_PATTERN,
                    title = "Top Spending: ${topCategory.categoryName}",
                    description = "${topCategory.categoryName} is your highest spending category this month at ${String.format("%.0f", topCategory.percentageChange)}% of total.",
                    priority = priority--,
                    actionLabel = "View Transactions",
                    actionRoute = "transactions?category=${topCategory.categoryId}",
                    icon = "category"
                ))
            }
        }
        
        // Tip: Savings opportunity (if under budget)
        budgetAdherence?.let { adherence ->
            if (adherence.overallAdherencePercentage > 0.8f && adherence.overBudgetCategories.isEmpty()) {
                val potentialSavings = adherence.totalBudgetAmount * 0.1
                tips.add(FinancialTip(
                    id = "savings_opportunity",
                    type = TipType.SAVINGS_OPPORTUNITY,
                    title = "Savings Opportunity",
                    description = "You're on track with your budgets! You could save an extra ₱${String.format("%,.0f", potentialSavings)} this month.",
                    priority = priority--,
                    actionLabel = "Set Goal",
                    actionRoute = "savings/create",
                    icon = "savings"
                ))
            }
        }
        
        return tips.sortedByDescending { it.priority }
    }

    /**
     * Analyze weekly spending patterns
     */
    suspend fun analyzeWeeklyPattern(userId: String): WeeklyPattern {
        val endDate = System.currentTimeMillis()
        val startDate = endDate - (90L * 24 * 60 * 60 * 1000) // Last 90 days
        
        val transactions = getTransactionsForRange(DateRange(startDate, endDate))
            .filter { it.type == "EXPENSE" }
        
        val calendar = Calendar.getInstance()
        
        // Group spending by day of week (1 = Sunday, 7 = Saturday)
        val dayOfWeekSpending = mutableMapOf<Int, Double>()
        (1..7).forEach { dayOfWeekSpending[it] = 0.0 }
        
        transactions.forEach { transaction ->
            calendar.timeInMillis = transaction.date
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            dayOfWeekSpending[dayOfWeek] = dayOfWeekSpending.getOrDefault(dayOfWeek, 0.0) + transaction.amount
        }
        
        val maxDay = dayOfWeekSpending.maxByOrNull { it.value }?.key ?: 1
        val minDay = dayOfWeekSpending.minByOrNull { it.value }?.key ?: 1
        
        // Calculate weekend vs weekday ratio
        val weekendTotal = (dayOfWeekSpending[1] ?: 0.0) + (dayOfWeekSpending[7] ?: 0.0)
        val weekdayTotal = (2..6).sumOf { dayOfWeekSpending[it] ?: 0.0 }
        val ratio = if (weekdayTotal > 0) weekendTotal / weekdayTotal else 1.0
        
        return WeeklyPattern(
            dayOfWeekSpending = dayOfWeekSpending,
            highestSpendingDay = maxDay,
            lowestSpendingDay = minDay,
            weekendVsWeekdayRatio = ratio
        )
    }

    // ==================== Helper Methods ====================

    private suspend fun getTransactionsForRange(dateRange: DateRange): List<Transaction> {
        return spendingDataRepository.getAllTransactions()
            .map { transactions ->
                transactions.filter { it.date in dateRange.startDate..dateRange.endDate }
            }
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
            AnalysisPeriod.CUSTOM -> endDate - (30L * 24 * 60 * 60 * 1000) // Default to 30 days
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

    private fun generateInsights(
        totalSpending: Double,
        previousSpending: Double,
        trend: TrendDirection,
        percentageChange: Double
    ): List<String> {
        val insights = mutableListOf<String>()
        
        when (trend) {
            TrendDirection.INCREASING -> {
                insights.add("Your spending increased ${String.format("%.1f", percentageChange)}% from the previous period.")
            }
            TrendDirection.DECREASING -> {
                insights.add("Great job! Your spending decreased ${String.format("%.1f", abs(percentageChange))}% from the previous period.")
            }
            TrendDirection.STABLE -> {
                insights.add("Your spending is stable compared to the previous period.")
            }
        }
        
        return insights
    }

    private fun detectUnusualAmounts(transactions: List<Transaction>): List<SpendingAnomaly> {
        if (transactions.size < MIN_TRANSACTIONS_FOR_STATS) return emptyList()
        
        val amounts = transactions.map { it.amount }
        val mean = amounts.average()
        val stdDev = calculateStandardDeviation(amounts)
        
        return transactions.mapNotNull { transaction ->
            val zScore = if (stdDev > 0) abs(transaction.amount - mean) / stdDev else 0.0
            
            when {
                zScore >= HIGH_SEVERITY_THRESHOLD -> createAnomaly(
                    transaction, AnomalyType.UNUSUAL_AMOUNT, AnomalySeverity.HIGH,
                    "Unusually high transaction amount: ₱${String.format("%,.2f", transaction.amount)}"
                )
                zScore >= UNUSUAL_AMOUNT_THRESHOLD -> createAnomaly(
                    transaction, AnomalyType.UNUSUAL_AMOUNT, AnomalySeverity.MEDIUM,
                    "Higher than usual transaction: ₱${String.format("%,.2f", transaction.amount)}"
                )
                else -> null
            }
        }
    }

    private fun detectDuplicateTransactions(transactions: List<Transaction>): List<SpendingAnomaly> {
        val duplicates = mutableListOf<SpendingAnomaly>()
        val grouped = transactions.groupBy { it.amount }
        
        grouped.forEach { (amount, txs) ->
            if (txs.size > 1) {
                txs.sortedBy { it.date }.windowed(2, 1, partialWindows = false).forEach { pair ->
                    val timeDiff = abs(pair[0].date - pair[1].date)
                    if (timeDiff <= DUPLICATE_TIME_WINDOW_MS) {
                        duplicates.add(createAnomaly(
                            pair[1], AnomalyType.DUPLICATE_TRANSACTION, AnomalySeverity.MEDIUM,
                            "Possible duplicate transaction of ₱${String.format("%,.2f", amount)}",
                            "Review and delete if duplicate"
                        ))
                    }
                }
            }
        }
        
        return duplicates
    }

    private fun detectFrequencySpikes(
        transactions: List<Transaction>,
        lookbackDays: Int
    ): List<SpendingAnomaly> {
        // Group by day
        val calendar = Calendar.getInstance()
        val dailyCount = transactions.groupBy { transaction ->
            calendar.timeInMillis = transaction.date
            calendar.get(Calendar.DAY_OF_YEAR)
        }.mapValues { it.value.size }
        
        val avgDailyCount = dailyCount.values.average()
        
        // Find days with unusually high transaction counts
        return dailyCount.filter { it.value > avgDailyCount * FREQUENCY_SPIKE_THRESHOLD }
            .flatMap { (day, count) ->
                transactions.filter { transaction ->
                    calendar.timeInMillis = transaction.date
                    calendar.get(Calendar.DAY_OF_YEAR) == day
                }.take(1) // Take first transaction of the day as representative
            }
            .map { transaction ->
                createAnomaly(
                    transaction, AnomalyType.FREQUENCY_SPIKE, AnomalySeverity.LOW,
                    "Unusually high transaction activity detected",
                    "Review your transactions for this day"
                )
            }
    }

    private fun createAnomaly(
        transaction: Transaction,
        type: AnomalyType,
        severity: AnomalySeverity,
        description: String,
        suggestedAction: String? = null
    ): SpendingAnomaly {
        return SpendingAnomaly(
            id = transaction.id.toLong() * 1000 + type.ordinal,
            transaction = transaction,
            anomalyType = type,
            severity = severity,
            description = description,
            detectedAt = System.currentTimeMillis(),
            suggestedAction = suggestedAction
        )
    }

    private fun calculateStandardDeviation(values: List<Double>): Double {
        if (values.size < 2) return 0.0
        val mean = values.average()
        val variance = values.sumOf { (it - mean) * (it - mean) } / values.size
        return kotlin.math.sqrt(variance)
    }

    private fun calculateProjectedOverspend(budget: BudgetWithSpending): Double? {
        if (budget.daysRemaining <= 0 || budget.progressPercentage <= 0) return null
        
        val dailySpendRate = budget.spentAmount / (budget.progressPercentage * 30) // Approximate
        val projectedTotal = dailySpendRate * 30
        
        return if (projectedTotal > budget.amount) projectedTotal - budget.amount else null
    }

    private fun generateCategoryComparisons(
        currentTransactions: List<Transaction>,
        previousTransactions: List<Transaction>,
        categoryMap: Map<Int, Category>
    ): List<CategoryComparison> {
        val currentByCategory = currentTransactions.groupBy { it.categoryId }
            .mapValues { it.value.sumOf { t -> t.amount } }
        val previousByCategory = previousTransactions.groupBy { it.categoryId }
            .mapValues { it.value.sumOf { t -> t.amount } }
        
        val allCategoryIds = (currentByCategory.keys + previousByCategory.keys).filterNotNull()
        
        return allCategoryIds.mapNotNull { categoryId ->
            val category = categoryMap[categoryId] ?: return@mapNotNull null
            val current = currentByCategory[categoryId] ?: 0.0
            val previous = previousByCategory[categoryId] ?: 0.0
            val change = current - previous
            val percentage = if (previous > 0) (change / previous) * 100 else 0.0
            
            CategoryComparison(
                categoryId = categoryId,
                categoryName = category.name,
                currentAmount = current,
                previousAmount = previous,
                absoluteChange = change,
                percentageChange = percentage,
                trendDirection = when {
                    percentage > 10 -> TrendDirection.INCREASING
                    percentage < -10 -> TrendDirection.DECREASING
                    else -> TrendDirection.STABLE
                }
            )
        }.sortedByDescending { abs(it.absoluteChange) }
    }

    // ==================== Chart Data Methods ====================

    /**
     * Get daily spending data for line charts
     */
    suspend fun getDailySpendingData(
        startDate: Long,
        endDate: Long
    ): List<DailySpending> {
        val transactions = getTransactionsForRange(DateRange(startDate, endDate))
            .filter { it.type == "EXPENSE" }
        
        val calendar = Calendar.getInstance()
        val daySpending = mutableMapOf<Long, Pair<Double, Int>>() // date -> (amount, count)
        
        transactions.forEach { transaction ->
            calendar.timeInMillis = transaction.date
            // Normalize to start of day
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val dayStart = calendar.timeInMillis
            
            val current = daySpending[dayStart] ?: Pair(0.0, 0)
            daySpending[dayStart] = Pair(current.first + transaction.amount, current.second + 1)
        }
        
        return daySpending.map { (date, data) ->
            DailySpending(date, data.first, data.second)
        }.sortedBy { it.date }
    }

    /**
     * Get monthly spending data for trend charts
     */
    suspend fun getMonthlySpendingData(
        months: Int = 6
    ): List<MonthlySpending> {
        val calendar = Calendar.getInstance()
        val endDate = System.currentTimeMillis()
        calendar.timeInMillis = endDate
        calendar.add(Calendar.MONTH, -months)
        val startDate = calendar.timeInMillis
        
        val transactions = getTransactionsForRange(DateRange(startDate, endDate))
        
        val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        
        val monthlyData = transactions.groupBy { transaction ->
            calendar.timeInMillis = transaction.date
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            Pair(year, month)
        }
        
        return monthlyData.map { (yearMonth, txs) ->
            val spending = txs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val income = txs.filter { it.type == "INCOME" }.sumOf { it.amount }
            
            MonthlySpending(
                yearMonth = "${yearMonth.first}-${String.format("%02d", yearMonth.second + 1)}",
                monthName = monthNames[yearMonth.second],
                totalSpending = spending,
                totalIncome = income,
                netSavings = income - spending
            )
        }.sortedBy { it.yearMonth }
    }

    /**
     * Get spending heat map data
     */
    suspend fun getSpendingHeatMapData(
        year: Int,
        month: Int
    ): SpendingHeatMapData {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1, 0, 0, 0)
        val startDate = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val endDate = calendar.timeInMillis
        
        val transactions = getTransactionsForRange(DateRange(startDate, endDate))
            .filter { it.type == "EXPENSE" }
        
        val dailyAmounts = mutableMapOf<Int, Double>()
        transactions.forEach { transaction ->
            calendar.timeInMillis = transaction.date
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            dailyAmounts[day] = dailyAmounts.getOrDefault(day, 0.0) + transaction.amount
        }
        
        val amounts = dailyAmounts.values
        return SpendingHeatMapData(
            month = month,
            year = year,
            dailyAmounts = dailyAmounts,
            maxAmount = amounts.maxOrNull() ?: 0.0,
            minAmount = amounts.minOrNull() ?: 0.0,
            averageAmount = if (amounts.isNotEmpty()) amounts.average() else 0.0
        )
    }
}
