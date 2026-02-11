package com.pyera.app.domain.repository

import com.pyera.app.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

interface SavingsRepository {
    fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>>
    suspend fun addSavingsGoal(goal: SavingsGoalEntity)
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity)
    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity)
}
