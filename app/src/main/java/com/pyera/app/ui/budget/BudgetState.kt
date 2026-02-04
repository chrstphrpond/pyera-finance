package com.pyera.app.ui.budget

import com.pyera.app.data.local.entity.BudgetEntity
import com.pyera.app.data.local.entity.CategoryEntity

data class BudgetState(
    val isLoading: Boolean = false,
    val items: List<BudgetItem> = emptyList(),
    val currentPeriod: String = "" // "YYYY-MM"
)

data class BudgetItem(
    val category: CategoryEntity,
    val budgetAmount: Double,
    val spentAmount: Double
) {
    val progress: Float
        get() = if (budgetAmount > 0) (spentAmount / budgetAmount).toFloat().coerceIn(0f, 1f) else 0f
    
    val remaining: Double
        get() = budgetAmount - spentAmount
}
