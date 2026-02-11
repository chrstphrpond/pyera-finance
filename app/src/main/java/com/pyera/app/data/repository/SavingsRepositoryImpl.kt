package com.pyera.app.data.repository

import com.pyera.app.domain.repository.*

import com.pyera.app.data.local.dao.SavingsGoalDao
import com.pyera.app.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SavingsRepositoryImpl @Inject constructor(
    private val savingsGoalDao: SavingsGoalDao
) : SavingsRepository {

    override fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>> {
        return savingsGoalDao.getAllSavingsGoals()
    }

    override suspend fun addSavingsGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.insertSavingsGoal(goal)
    }

    override suspend fun updateSavingsGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.updateSavingsGoal(goal)
    }

    override suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.deleteSavingsGoal(goal)
    }
}
