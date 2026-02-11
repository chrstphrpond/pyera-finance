package com.pyera.app.ui.insights

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.domain.repository.AnalysisRepository
import com.pyera.app.domain.analysis.AnalysisPeriod
import com.pyera.app.domain.analysis.BudgetAdherence
import com.pyera.app.domain.analysis.CategoryInsight
import com.pyera.app.domain.analysis.DateRange
import com.pyera.app.domain.analysis.FinancialTip
import com.pyera.app.domain.analysis.PeriodComparison
import com.pyera.app.domain.analysis.SpendingAnomaly
import com.pyera.app.domain.analysis.SpendingInsights
import com.pyera.app.domain.analysis.WeeklyPattern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/**
 * State representing the insights screen UI state
 */
@Immutable
data class InsightsUiState(
    val selectedPeriod: AnalysisPeriod = AnalysisPeriod.MONTHLY,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    
    // Core insights data
    val spendingInsights: SpendingInsights? = null,
    val anomalies: List<SpendingAnomaly> = emptyList(),
    val categoryInsights: List<CategoryInsight> = emptyList(),
    val budgetAdherence: BudgetAdherence? = null,
    val tips: List<FinancialTip> = emptyList(),
    val periodComparison: PeriodComparison? = null,
    val weeklyPattern: WeeklyPattern? = null,
    
    // UI State
    val dismissedAnomalyIds: Set<Long> = emptySet(),
    val showAllCategories: Boolean = false,
    val showAllAnomalies: Boolean = false,
    val selectedCategoryId: Int? = null
)

/**
 * Sealed class representing insights state for legacy compatibility
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
 * ViewModel for the Insights screen that manages spending analysis data
 * and provides UI state for the insights feature.
 */
@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    // Legacy state for compatibility
    private val _insights = MutableStateFlow<InsightsState>(InsightsState.Loading)
    val insights: StateFlow<InsightsState> = _insights.asStateFlow()

    init {
        loadInsights()
    }

    /**
     * Load insights for the currently selected period
     */
    fun loadInsights(period: AnalysisPeriod = _uiState.value.selectedPeriod) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedPeriod = period,
                error = null
            )
            _insights.value = InsightsState.Loading

            try {
                // Load all data in parallel
                val spendingInsights = analysisRepository.getSpendingInsights(period)
                val anomalies = analysisRepository.detectSpendingAnomalies(lookbackDays = 30)
                val categoryInsights = analysisRepository.getCategoryInsights()
                val budgetAdherence = analysisRepository.getBudgetAdherence()
                val tips = analysisRepository.generatePersonalizedTips()
                val periodComparison = analysisRepository.comparePeriods(period)
                val weeklyPattern = analysisRepository.getWeeklyPattern()

                val newState = InsightsUiState(
                    selectedPeriod = period,
                    isLoading = false,
                    spendingInsights = spendingInsights,
                    anomalies = anomalies.filter { it.id !in _uiState.value.dismissedAnomalyIds },
                    categoryInsights = categoryInsights,
                    budgetAdherence = budgetAdherence,
                    tips = tips,
                    periodComparison = periodComparison,
                    weeklyPattern = weeklyPattern
                )

                _uiState.value = newState
                _insights.value = InsightsState.Success(
                    spendingInsights = spendingInsights,
                    anomalies = anomalies,
                    categoryInsights = categoryInsights,
                    budgetAdherence = budgetAdherence,
                    tips = tips,
                    periodComparison = periodComparison,
                    weeklyPattern = weeklyPattern
                )
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is IOException -> "Network error. Please check your connection."
                    else -> e.message ?: "Failed to load insights"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
                _insights.value = InsightsState.Error(errorMessage)
            }
        }
    }

    /**
     * Load insights for a custom date range
     */
    fun loadInsightsForRange(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val dateRange = DateRange(startDate, endDate)
                val spendingInsights = analysisRepository.getSpendingInsightsForRange(dateRange)
                val categoryInsights = analysisRepository.getCategoryInsights()
                val periodComparison = analysisRepository.compareCustomPeriods(
                    dateRange,
                    getPreviousPeriodRange(dateRange)
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    spendingInsights = spendingInsights,
                    categoryInsights = categoryInsights,
                    periodComparison = periodComparison
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load insights"
                )
            }
        }
    }

    /**
     * Refresh insights data
     */
    fun refreshInsights() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            try {
                analysisRepository.refreshAnalysis()
                loadInsights(_uiState.value.selectedPeriod)
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    /**
     * Dismiss a specific anomaly
     */
    fun dismissAnomaly(anomalyId: Long) {
        viewModelScope.launch {
            analysisRepository.dismissAnomaly(anomalyId)
            _uiState.value = _uiState.value.copy(
                dismissedAnomalyIds = _uiState.value.dismissedAnomalyIds + anomalyId,
                anomalies = _uiState.value.anomalies.filter { it.id != anomalyId }
            )
        }
    }

    /**
     * Dismiss all anomalies
     */
    fun dismissAllAnomalies() {
        viewModelScope.launch {
            analysisRepository.dismissAllAnomalies()
            val allAnomalyIds = _uiState.value.anomalies.map { it.id }.toSet()
            _uiState.value = _uiState.value.copy(
                dismissedAnomalyIds = _uiState.value.dismissedAnomalyIds + allAnomalyIds,
                anomalies = emptyList()
            )
        }
    }

    /**
     * Change the analysis period
     */
    fun setPeriod(period: AnalysisPeriod) {
        if (period != _uiState.value.selectedPeriod) {
            loadInsights(period)
        }
    }

    /**
     * Toggle showing all categories vs top categories only
     */
    fun toggleShowAllCategories() {
        _uiState.value = _uiState.value.copy(
            showAllCategories = !_uiState.value.showAllCategories
        )
    }

    /**
     * Toggle showing all anomalies vs high priority only
     */
    fun toggleShowAllAnomalies() {
        _uiState.value = _uiState.value.copy(
            showAllAnomalies = !_uiState.value.showAllAnomalies
        )
    }

    /**
     * Select a category to filter insights
     */
    fun selectCategory(categoryId: Int?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    /**
     * Clear any error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Retry loading after an error
     */
    fun retry() {
        loadInsights(_uiState.value.selectedPeriod)
    }

    // ==================== Private Helper Methods ====================

    private fun getPreviousPeriodRange(currentRange: DateRange): DateRange {
        val duration = currentRange.endDate - currentRange.startDate
        return DateRange(
            startDate = currentRange.startDate - duration,
            endDate = currentRange.startDate
        )
    }
}
