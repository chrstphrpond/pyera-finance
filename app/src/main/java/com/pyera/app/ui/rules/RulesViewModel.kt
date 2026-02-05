package com.pyera.app.ui.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.MatchType
import com.pyera.app.data.local.entity.TransactionRuleEntity
import com.pyera.app.data.repository.AuthRepository
import com.pyera.app.data.repository.CategoryRepository
import com.pyera.app.data.repository.TransactionRuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Rules screen.
 */
data class RulesUiState(
    val rules: List<TransactionRuleEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    
    // Form state for add/edit
    val pattern: String = "",
    val selectedMatchType: MatchType = MatchType.CONTAINS,
    val selectedCategoryId: Int? = null,
    val priority: Int = 5,
    val isActive: Boolean = true,
    
    // Test state
    val testDescription: String = "",
    val testResult: Boolean? = null,
    
    // Editing state
    val editingRuleId: Long? = null,
    val isEditMode: Boolean = false
)

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val transactionRuleRepository: TransactionRuleRepository,
    private val authRepository: AuthRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "RulesViewModel"
    }

    private val _state = MutableStateFlow(RulesUiState())
    val state: StateFlow<RulesUiState> = _state.asStateFlow()

    private val currentUserId: String
        get() = authRepository.currentUser?.uid ?: ""

    init {
        loadRules()
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories()
                .catch { e ->
                    _state.update { it.copy(error = "Failed to load categories: ${e.message}") }
                }
                .collect { categories ->
                    _state.update { it.copy(categories = categories) }
                }
        }
    }

    private fun loadRules() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            transactionRuleRepository.getAllRules(currentUserId)
                .catch { e ->
                    _state.update { 
                        it.copy(isLoading = false, error = e.message) 
                    }
                }
                .collect { rules ->
                    _state.update { 
                        it.copy(isLoading = false, rules = rules) 
                    }
                }
        }
    }

    fun refresh() {
        loadRules()
    }

    // Form field updaters
    fun updatePattern(pattern: String) {
        _state.update { 
            it.copy(pattern = pattern, testResult = null) 
        }
    }

    fun updateMatchType(matchType: MatchType) {
        _state.update { 
            it.copy(selectedMatchType = matchType, testResult = null) 
        }
    }

    fun updateCategory(categoryId: Int?) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun updatePriority(priority: Int) {
        _state.update { it.copy(priority = priority.coerceIn(0, 10)) }
    }

    fun updateTestDescription(description: String) {
        _state.update { 
            it.copy(testDescription = description, testResult = null) 
        }
    }

    fun toggleActive() {
        _state.update { it.copy(isActive = !it.isActive) }
    }

    /**
     * Test if the current pattern matches the test description.
     */
    fun testRule() {
        val currentState = _state.value
        if (currentState.pattern.isBlank() || currentState.testDescription.isBlank()) {
            _state.update { it.copy(testResult = false) }
            return
        }

        val result = transactionRuleRepository.testRule(
            pattern = currentState.pattern,
            matchType = currentState.selectedMatchType,
            testDescription = currentState.testDescription
        )
        _state.update { it.copy(testResult = result) }
    }

    /**
     * Save a new rule or update an existing one.
     */
    fun saveRule() {
        viewModelScope.launch {
            val currentState = _state.value
            
            // Validation
            if (currentState.pattern.isBlank()) {
                _state.update { it.copy(error = "Pattern cannot be empty") }
                return@launch
            }
            
            if (currentState.selectedCategoryId == null) {
                _state.update { it.copy(error = "Please select a category") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, error = null) }

            val result = if (currentState.isEditMode && currentState.editingRuleId != null) {
                // Update existing rule
                val existingRule = transactionRuleRepository.getRuleById(currentState.editingRuleId)
                if (existingRule != null) {
                    val updatedRule = existingRule.copy(
                        pattern = currentState.pattern.trim(),
                        matchType = currentState.selectedMatchType.name,
                        categoryId = currentState.selectedCategoryId,
                        priority = currentState.priority,
                        isActive = currentState.isActive
                    )
                    transactionRuleRepository.updateRule(updatedRule)
                } else {
                    Result.failure(IllegalStateException("Rule not found"))
                }
            } else {
                // Create new rule
                transactionRuleRepository.createRule(
                    userId = currentUserId,
                    pattern = currentState.pattern.trim(),
                    matchType = currentState.selectedMatchType,
                    categoryId = currentState.selectedCategoryId,
                    priority = currentState.priority
                )
            }

            result.fold(
                onSuccess = {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = if (currentState.isEditMode) "Rule updated" else "Rule created",
                            // Reset form
                            pattern = "",
                            selectedMatchType = MatchType.CONTAINS,
                            selectedCategoryId = null,
                            priority = 5,
                            isActive = true,
                            testDescription = "",
                            testResult = null,
                            editingRuleId = null,
                            isEditMode = false
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { 
                        it.copy(isLoading = false, error = e.message) 
                    }
                }
            )
        }
    }

    /**
     * Load a rule for editing.
     */
    fun loadRuleForEdit(ruleId: Long) {
        viewModelScope.launch {
            val rule = transactionRuleRepository.getRuleById(ruleId)
            rule?.let {
                _state.update { state ->
                    state.copy(
                        editingRuleId = ruleId,
                        isEditMode = true,
                        pattern = it.pattern,
                        selectedMatchType = MatchType.fromString(it.matchType),
                        selectedCategoryId = it.categoryId,
                        priority = it.priority,
                        isActive = it.isActive
                    )
                }
            }
        }
    }

    /**
     * Delete a rule.
     */
    fun deleteRule(ruleId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            transactionRuleRepository.deleteRule(ruleId)
                .fold(
                    onSuccess = {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                successMessage = "Rule deleted"
                            )
                        }
                    },
                    onFailure = { e ->
                        _state.update { 
                            it.copy(isLoading = false, error = e.message) 
                        }
                    }
                )
        }
    }

    /**
     * Toggle a rule's active status.
     */
    fun toggleRuleActive(ruleId: Long, isActive: Boolean) {
        viewModelScope.launch {
            transactionRuleRepository.toggleRuleActive(ruleId, isActive)
                .fold(
                    onSuccess = {
                        _state.update { 
                            it.copy(successMessage = if (isActive) "Rule enabled" else "Rule disabled")
                        }
                    },
                    onFailure = { e ->
                        _state.update { it.copy(error = e.message) }
                    }
                )
        }
    }

    /**
     * Reset the form to default state.
     */
    fun resetForm() {
        _state.update {
            it.copy(
                pattern = "",
                selectedMatchType = MatchType.CONTAINS,
                selectedCategoryId = null,
                priority = 5,
                isActive = true,
                testDescription = "",
                testResult = null,
                editingRuleId = null,
                isEditMode = false,
                error = null,
                successMessage = null
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }
}
