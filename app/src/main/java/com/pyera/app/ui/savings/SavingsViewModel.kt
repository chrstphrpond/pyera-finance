package com.pyera.app.ui.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pyera.app.data.local.entity.SavingsGoalEntity
import com.pyera.app.data.repository.SavingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavingsViewModel @Inject constructor(
    private val savingsRepository: SavingsRepository
) : ViewModel() {

    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = savingsRepository.getAllSavingsGoals()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSavingsGoal(name: String, targetAmount: Double, deadline: Long, icon: Int, color: Int) {
        viewModelScope.launch {
            savingsRepository.addSavingsGoal(
                SavingsGoalEntity(
                    name = name,
                    targetAmount = targetAmount,
                    currentAmount = 0.0,
                    deadline = deadline,
                    icon = icon,
                    color = color
                )
            )
        }
    }

    fun updateProgress(goal: SavingsGoalEntity, newAmount: Double) {
        viewModelScope.launch {
            savingsRepository.updateSavingsGoal(goal.copy(currentAmount = newAmount))
        }
    }
    
    fun deleteGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            savingsRepository.deleteSavingsGoal(goal)
        }
    }
}
