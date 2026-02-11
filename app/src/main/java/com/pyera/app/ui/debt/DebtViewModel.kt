package com.pyera.app.ui.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.DebtEntity
import com.pyera.app.domain.repository.DebtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtViewModel @Inject constructor(
    private val debtRepository: DebtRepository
) : ViewModel() {

    val debts: StateFlow<List<DebtEntity>> = debtRepository.getAllDebts()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    fun addDebt(name: String, amount: Double, dueDate: Long, type: String) {
        viewModelScope.launch {
            debtRepository.addDebt(
                DebtEntity(name = name, amount = amount, dueDate = dueDate, type = type)
            )
        }
    }

    fun markAsPaid(debt: DebtEntity) {
        viewModelScope.launch {
            debtRepository.updateDebt(debt.copy(isPaid = true))
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            debtRepository.deleteDebt(debt)
        }
    }

    fun updateDebt(debt: DebtEntity) {
        viewModelScope.launch {
            debtRepository.updateDebt(debt)
        }
    }

    /**
     * Refreshes the debt list.
     * Since debts are observed via Flow, this triggers any refresh logic if needed.
     */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // The Flow automatically updates, but we could add force refresh logic here
            kotlinx.coroutines.delay(500) // Simulate refresh delay for UX
            _isRefreshing.value = false
        }
    }
}
