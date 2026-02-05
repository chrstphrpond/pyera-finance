package com.pyera.app.ui.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.InvestmentEntity
import com.pyera.app.data.repository.InvestmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvestmentsViewModel @Inject constructor(
    private val investmentRepository: InvestmentRepository
) : ViewModel() {

    val investments: StateFlow<List<InvestmentEntity>> = investmentRepository.getAllInvestments()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalPortfolioValue: StateFlow<Double> = investments.map { list ->
        list.sumOf { it.currentValue }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun addInvestment(name: String, type: String, amountInvested: Double, currentValue: Double) {
        viewModelScope.launch {
            investmentRepository.addInvestment(
                InvestmentEntity(
                    name = name,
                    type = type,
                    amountInvested = amountInvested,
                    currentValue = currentValue
                )
            )
        }
    }

    fun updateValue(investment: InvestmentEntity, newValue: Double) {
        viewModelScope.launch {
            investmentRepository.updateInvestment(investment.copy(currentValue = newValue))
        }
    }

    fun deleteInvestment(investment: InvestmentEntity) {
        viewModelScope.launch {
            investmentRepository.deleteInvestment(investment)
        }
    }
}
