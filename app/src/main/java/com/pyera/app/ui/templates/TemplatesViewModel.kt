package com.pyera.app.ui.templates

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.TransactionTemplateEntity
import com.pyera.app.data.repository.AuthRepository
import com.pyera.app.data.repository.TransactionTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State for the templates screen
 */
@Immutable
data class TemplatesUiState(
    val templates: List<TransactionTemplateEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val showInactive: Boolean = false
)

/**
 * State for add/edit template form
 */
@Immutable
data class TemplateFormState(
    val name: String = "",
    val description: String = "",
    val amount: String = "",
    val hasVariableAmount: Boolean = false,
    val type: String = "EXPENSE",
    val categoryId: Int? = null,
    val accountId: Long? = null,
    val icon: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val templateRepository: TransactionTemplateRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplatesUiState(isLoading = true))
    val uiState: StateFlow<TemplatesUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(TemplateFormState())
    val formState: StateFlow<TemplateFormState> = _formState.asStateFlow()

    private var editingTemplateId: Long? = null

    init {
        loadTemplates()
    }

    fun loadTemplates() {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            templateRepository.getAllTemplates(userId)
                .onEach { templates ->
                    _uiState.value = _uiState.value.copy(
                        templates = templates,
                        isLoading = false
                    )
                }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load templates"
                    )
                }
                .launchIn(viewModelScope)
        }
    }

    fun searchTemplates(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        // Filter is done in UI based on searchQuery
    }

    fun deleteTemplate(templateId: Long) {
        viewModelScope.launch {
            templateRepository.deleteTemplate(templateId)
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to delete template"
                    )
                }
        }
    }

    fun toggleTemplateActive(templateId: Long, isActive: Boolean) {
        viewModelScope.launch {
            templateRepository.toggleActive(templateId, isActive)
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to update template"
                    )
                }
        }
    }

    // ==================== Form Methods ====================

    fun initFormForEdit(template: TransactionTemplateEntity) {
        editingTemplateId = template.id
        _formState.value = TemplateFormState(
            name = template.name,
            description = template.description,
            amount = template.amount?.toString() ?: "",
            hasVariableAmount = template.amount == null,
            type = template.type,
            categoryId = template.categoryId,
            accountId = template.accountId,
            icon = template.icon
        )
    }

    fun initFormForCreate(
        defaultType: String = "EXPENSE",
        defaultCategoryId: Int? = null,
        defaultAccountId: Long? = null
    ) {
        editingTemplateId = null
        _formState.value = TemplateFormState(
            type = defaultType,
            categoryId = defaultCategoryId,
            accountId = defaultAccountId
        )
    }

    fun onNameChange(name: String) {
        _formState.value = _formState.value.copy(name = name)
    }

    fun onDescriptionChange(description: String) {
        _formState.value = _formState.value.copy(description = description)
    }

    fun onAmountChange(amount: String) {
        _formState.value = _formState.value.copy(amount = amount)
    }

    fun onVariableAmountToggle(hasVariable: Boolean) {
        _formState.value = _formState.value.copy(
            hasVariableAmount = hasVariable,
            amount = if (hasVariable) "" else _formState.value.amount
        )
    }

    fun onTypeChange(type: String) {
        _formState.value = _formState.value.copy(type = type)
    }

    fun onCategoryChange(categoryId: Int?) {
        _formState.value = _formState.value.copy(categoryId = categoryId)
    }

    fun onAccountChange(accountId: Long?) {
        _formState.value = _formState.value.copy(accountId = accountId)
    }

    fun onIconChange(icon: String?) {
        _formState.value = _formState.value.copy(icon = icon)
    }

    fun saveTemplate() {
        val currentState = _formState.value
        val userId = authRepository.getCurrentUserId()

        if (userId == null) {
            _formState.value = currentState.copy(error = "User not authenticated")
            return
        }

        // Validation
        if (currentState.name.isBlank()) {
            _formState.value = currentState.copy(error = "Template name is required")
            return
        }

        val amount = if (currentState.hasVariableAmount) {
            null
        } else {
            currentState.amount.toDoubleOrNull()
        }

        if (!currentState.hasVariableAmount && amount == null) {
            _formState.value = currentState.copy(error = "Please enter a valid amount or enable variable amount")
            return
        }

        _formState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = if (editingTemplateId != null) {
                // Update existing
                val template = TransactionTemplateEntity(
                    id = editingTemplateId!!,
                    userId = userId,
                    name = currentState.name.trim(),
                    description = currentState.description.trim(),
                    amount = amount,
                    type = currentState.type,
                    categoryId = currentState.categoryId,
                    accountId = currentState.accountId,
                    icon = currentState.icon,
                    isActive = true
                )
                templateRepository.updateTemplate(template)
            } else {
                // Create new
                templateRepository.createTemplate(
                    userId = userId,
                    name = currentState.name.trim(),
                    description = currentState.description.trim(),
                    amount = amount,
                    type = currentState.type,
                    categoryId = currentState.categoryId,
                    accountId = currentState.accountId,
                    icon = currentState.icon
                )
            }

            result.fold(
                onSuccess = {
                    _formState.value = TemplateFormState(isSuccess = true)
                },
                onFailure = { e ->
                    _formState.value = currentState.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to save template"
                    )
                }
            )
        }
    }

    fun clearFormError() {
        _formState.value = _formState.value.copy(error = null)
    }

    fun clearForm() {
        _formState.value = TemplateFormState()
        editingTemplateId = null
    }

    /**
     * Get a template by ID - used for edit screen initialization
     */
    suspend fun getTemplateById(templateId: Long): TransactionTemplateEntity? {
        return templateRepository.getTemplateById(templateId)
    }
}
