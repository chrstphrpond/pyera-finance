package com.pyera.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.AccountType
import com.pyera.app.data.local.entity.displayName
import com.pyera.app.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    // ==================== UI States ====================
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedAccount = MutableStateFlow<AccountEntity?>(null)
    val selectedAccount: StateFlow<AccountEntity?> = _selectedAccount

    // ==================== Form States ====================
    
    private val _formState = MutableStateFlow(AccountFormState())
    val formState: StateFlow<AccountFormState> = _formState

    // ==================== Data States ====================
    
    val accounts: StateFlow<List<AccountEntity>> = accountRepository.getAllAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeAccounts: StateFlow<List<AccountEntity>> = accountRepository.getActiveAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalBalance: StateFlow<Double> = combine(accounts) { accs ->
        accs.firstOrNull()?.filter { !it.isArchived }?.sumOf { it.balance } ?: 0.0
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // ==================== Public Methods ====================

    fun selectAccount(account: AccountEntity?) {
        _selectedAccount.value = account
    }

    fun loadAccountDetail(accountId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val account = accountRepository.getAccountById(accountId)
                _selectedAccount.value = account
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== Form Methods ====================

    fun updateFormState(
        name: String? = null,
        type: AccountType? = null,
        initialBalance: String? = null,
        currency: String? = null,
        color: Int? = null,
        icon: String? = null,
        isDefault: Boolean? = null
    ) {
        _formState.value = _formState.value.copy(
            name = name ?: _formState.value.name,
            type = type ?: _formState.value.type,
            initialBalance = initialBalance ?: _formState.value.initialBalance,
            currency = currency ?: _formState.value.currency,
            color = color ?: _formState.value.color,
            icon = icon ?: _formState.value.icon,
            isDefault = isDefault ?: _formState.value.isDefault
        )
    }

    fun resetFormState() {
        _formState.value = AccountFormState()
    }

    fun initEditForm(account: AccountEntity) {
        _formState.value = AccountFormState(
            name = account.name,
            type = account.type,
            initialBalance = account.balance.toString(),
            currency = account.currency,
            color = account.color,
            icon = account.icon,
            isDefault = account.isDefault,
            isEditing = true,
            editingAccountId = account.id
        )
    }

    // ==================== CRUD Operations ====================

    fun createAccount(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val state = _formState.value
                
                // Validation
                val validationError = validateForm(state)
                if (validationError != null) {
                    _error.value = validationError
                    _isLoading.value = false
                    return@launch
                }

                val result = accountRepository.createAccount(
                    name = state.name.trim(),
                    type = state.type,
                    initialBalance = state.initialBalance.toDoubleOrNull() ?: 0.0,
                    currency = state.currency,
                    color = state.color,
                    icon = state.icon,
                    isDefault = state.isDefault
                )

                result.fold(
                    onSuccess = {
                        resetFormState()
                        onSuccess()
                    },
                    onFailure = { e ->
                        _error.value = e.message
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAccount(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val state = _formState.value
                val accountId = state.editingAccountId
                    ?: throw IllegalStateException("No account ID for editing")

                val existingAccount = accountRepository.getAccountById(accountId)
                    ?: throw IllegalStateException("Account not found")

                val updatedAccount = existingAccount.copy(
                    name = state.name.trim(),
                    type = state.type,
                    balance = state.initialBalance.toDoubleOrNull() ?: existingAccount.balance,
                    currency = state.currency,
                    color = state.color,
                    icon = state.icon,
                    isDefault = state.isDefault,
                    updatedAt = System.currentTimeMillis()
                )

                val result = accountRepository.updateAccount(updatedAccount)
                
                result.fold(
                    onSuccess = {
                        resetFormState()
                        onSuccess()
                    },
                    onFailure = { e ->
                        _error.value = e.message
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAccount(accountId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = accountRepository.deleteAccount(accountId)
                result.fold(
                    onSuccess = { onSuccess() },
                    onFailure = { e ->
                        _error.value = e.message
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun archiveAccount(accountId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = accountRepository.archiveAccount(accountId)
                result.fold(
                    onSuccess = {},
                    onFailure = { e ->
                        _error.value = e.message
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun unarchiveAccount(accountId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = accountRepository.unarchiveAccount(accountId)
                result.fold(
                    onSuccess = {},
                    onFailure = { e ->
                        _error.value = e.message
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setDefaultAccount(accountId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = accountRepository.setDefaultAccount(accountId)
                result.fold(
                    onSuccess = {},
                    onFailure = { e ->
                        _error.value = e.message
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== Transfer ====================

    fun transferBetweenAccounts(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        description: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = accountRepository.transferBetweenAccounts(
                    fromAccountId = fromAccountId,
                    toAccountId = toAccountId,
                    amount = amount,
                    description = description
                )

                result.fold(
                    onSuccess = { onSuccess() },
                    onFailure = { e ->
                        _error.value = e.message
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== Validation ====================

    private fun validateForm(state: AccountFormState): String? {
        return when {
            state.name.isBlank() -> "Account name is required"
            state.name.length > 50 -> "Account name is too long (max 50 characters)"
            state.initialBalance.toDoubleOrNull() == null && state.initialBalance.isNotBlank() -> "Invalid balance amount"
            else -> null
        }
    }

    fun clearError() {
        _error.value = null
    }
}

// ==================== State Classes ====================

data class AccountFormState(
    val name: String = "",
    val type: AccountType = AccountType.BANK,
    val initialBalance: String = "",
    val currency: String = "PHP",
    val color: Int = 0xFF4CAF50.toInt(), // Default green color
    val icon: String = AccountType.BANK.defaultIcon(),
    val isDefault: Boolean = false,
    val isEditing: Boolean = false,
    val editingAccountId: Long? = null
)

data class AccountsUiState(
    val accounts: List<AccountEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalBalance: Double = 0.0
)

// Predefined colors for account selection
val AccountColors = listOf(
    0xFF4CAF50.toInt(), // Green
    0xFF2196F3.toInt(), // Blue
    0xFFFF9800.toInt(), // Orange
    0xFFE91E63.toInt(), // Pink
    0xFF9C27B0.toInt(), // Purple
    0xFF00BCD4.toInt(), // Cyan
    0xFFFFEB3B.toInt(), // Yellow
    0xFFFF5722.toInt(), // Deep Orange
    0xFF795548.toInt(), // Brown
    0xFF607D8B.toInt()  // Blue Grey
)
