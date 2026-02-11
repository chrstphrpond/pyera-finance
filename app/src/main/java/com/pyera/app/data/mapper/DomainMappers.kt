package com.pyera.app.data.mapper

import com.pyera.app.data.local.entity.BudgetPeriod as DataBudgetPeriod
import com.pyera.app.data.local.entity.BudgetWithSpending as DataBudgetWithSpending
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.domain.model.BudgetPeriod
import com.pyera.app.domain.model.BudgetWithSpending
import com.pyera.app.domain.model.Category
import com.pyera.app.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        note = note,
        date = date,
        type = type,
        categoryId = categoryId,
        accountId = accountId,
        userId = userId,
        isTransfer = isTransfer,
        transferAccountId = transferAccountId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        receiptImagePath = receiptImagePath,
        receiptCloudUrl = receiptCloudUrl,
        hasReceipt = hasReceipt
    )
}

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        icon = icon,
        color = color,
        type = type
    )
}

fun DataBudgetPeriod.toDomain(): BudgetPeriod {
    return when (this) {
        DataBudgetPeriod.DAILY -> BudgetPeriod.DAILY
        DataBudgetPeriod.WEEKLY -> BudgetPeriod.WEEKLY
        DataBudgetPeriod.MONTHLY -> BudgetPeriod.MONTHLY
        DataBudgetPeriod.YEARLY -> BudgetPeriod.YEARLY
    }
}

fun DataBudgetWithSpending.toDomain(): BudgetWithSpending {
    return BudgetWithSpending(
        id = id,
        userId = userId,
        categoryId = categoryId,
        categoryName = categoryName,
        categoryColor = categoryColor,
        categoryIcon = categoryIcon,
        amount = amount,
        period = period.toDomain(),
        startDate = startDate,
        isActive = isActive,
        alertThreshold = alertThreshold,
        spentAmount = spentAmount,
        remainingAmount = remainingAmount,
        progressPercentage = progressPercentage,
        isOverBudget = isOverBudget,
        daysRemaining = daysRemaining
    )
}
