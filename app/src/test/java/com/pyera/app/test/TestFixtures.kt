package com.pyera.app.test

import com.pyera.app.data.local.entity.AccountEntity
import com.pyera.app.data.local.entity.AccountType
import com.pyera.app.data.local.entity.BudgetEntity
import com.pyera.app.data.local.entity.BudgetPeriod
import com.pyera.app.data.local.entity.BudgetStatus
import com.pyera.app.data.local.entity.BudgetSummary
import com.pyera.app.data.local.entity.BudgetWithSpending
import com.pyera.app.data.local.entity.CategoryEntity
import com.pyera.app.data.local.entity.DebtEntity
import com.pyera.app.data.local.entity.SavingsGoalEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.domain.model.BudgetPeriod as DomainBudgetPeriod
import com.pyera.app.domain.model.BudgetWithSpending as DomainBudgetWithSpending
import com.pyera.app.domain.model.Transaction as DomainTransaction
import com.google.firebase.auth.FirebaseUser
import org.mockito.kotlin.doReturn

// ==================== Transaction Fixtures ====================

fun createTransaction(
    id: Long = 1,
    amount: Double = 100.0,
    note: String = "Test Transaction",
    date: Long = System.currentTimeMillis(),
    type: String = "EXPENSE",
    categoryId: Int? = 1,
    accountId: Long = 1,
    userId: String = "test_user"
): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        note = note,
        date = date,
        type = type,
        categoryId = categoryId,
        accountId = accountId,
        userId = userId
    )
}

fun createIncomeTransaction(
    id: Long = 1,
    amount: Double = 1000.0,
    note: String = "Salary",
    date: Long = System.currentTimeMillis(),
    categoryId: Int? = 10
): TransactionEntity {
    return createTransaction(
        id = id,
        amount = amount,
        note = note,
        date = date,
        type = "INCOME",
        categoryId = categoryId
    )
}

fun createExpenseTransaction(
    id: Long = 1,
    amount: Double = 50.0,
    note: String = "Groceries",
    date: Long = System.currentTimeMillis(),
    categoryId: Int? = 1
): TransactionEntity {
    return createTransaction(
        id = id,
        amount = amount,
        note = note,
        date = date,
        type = "EXPENSE",
        categoryId = categoryId
    )
}

// ==================== Domain Model Fixtures ====================

fun transaction(
    id: Long = 1,
    amount: Double = 100.0,
    note: String = "Test Transaction",
    date: Long = System.currentTimeMillis(),
    type: String = "EXPENSE",
    categoryId: Int? = 1,
    accountId: Long = 1,
    userId: String = "test_user",
    isTransfer: Boolean = false,
    transferAccountId: Long? = null,
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis(),
    receiptImagePath: String? = null,
    receiptCloudUrl: String? = null,
    hasReceipt: Boolean = false
): DomainTransaction {
    return DomainTransaction(
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

fun budgetWithSpending(
    id: Int = 1,
    userId: String = "test_user",
    categoryId: Int = 1,
    categoryName: String = "Food",
    categoryColor: Int = 0xFF4CAF50.toInt(),
    categoryIcon: String? = "icon",
    amount: Double = 1000.0,
    spentAmount: Double = 200.0,
    remainingAmount: Double = amount - spentAmount,
    progressPercentage: Float = if (amount == 0.0) 0f else (spentAmount / amount).toFloat(),
    period: DomainBudgetPeriod = DomainBudgetPeriod.MONTHLY,
    startDate: Long = System.currentTimeMillis(),
    isActive: Boolean = true,
    alertThreshold: Float = 0.8f,
    isOverBudget: Boolean = spentAmount > amount,
    daysRemaining: Int = 15
): DomainBudgetWithSpending {
    return DomainBudgetWithSpending(
        id = id,
        userId = userId,
        categoryId = categoryId,
        categoryName = categoryName,
        categoryColor = categoryColor,
        categoryIcon = categoryIcon,
        amount = amount,
        period = period,
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

// ==================== Category Fixtures ====================

fun createCategory(
    id: Int = 1,
    name: String = "Food",
    icon: String = "🍔",
    color: Int = 0xFF4CAF50.toInt(),
    type: String = "EXPENSE"
): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        icon = icon,
        color = color,
        type = type
    )
}

fun createIncomeCategory(
    id: Int = 10,
    name: String = "Salary",
    icon: String = "💰"
): CategoryEntity {
    return createCategory(
        id = id,
        name = name,
        icon = icon,
        type = "INCOME"
    )
}

// ==================== Account Fixtures ====================

fun createAccount(
    id: Long = 1,
    name: String = "Bank Account",
    type: AccountType = AccountType.BANK,
    balance: Double = 1000.0,
    currency: String = "PHP",
    color: Int = 0xFF4CAF50.toInt(),
    icon: String = "🏦",
    isDefault: Boolean = false,
    isArchived: Boolean = false,
    userId: String = "test_user"
): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        type = type,
        balance = balance,
        currency = currency,
        color = color,
        icon = icon,
        isDefault = isDefault,
        isArchived = isArchived,
        userId = userId
    )
}

fun createCashAccount(
    id: Long = 2,
    balance: Double = 500.0
): AccountEntity {
    return createAccount(
        id = id,
        name = "Cash",
        type = AccountType.CASH,
        balance = balance,
        icon = "💵"
    )
}

// ==================== Budget Fixtures ====================

fun createBudgetEntity(
    id: Int = 1,
    userId: String = "test_user",
    categoryId: Int = 1,
    amount: Double = 1000.0,
    period: BudgetPeriod = BudgetPeriod.MONTHLY,
    isActive: Boolean = true
): BudgetEntity {
    return BudgetEntity(
        id = id,
        userId = userId,
        categoryId = categoryId,
        amount = amount,
        period = period,
        isActive = isActive
    )
}

fun createBudgetWithSpending(
    id: Int = 1,
    userId: String = "test_user",
    categoryId: Int = 1,
    categoryName: String = "Food",
    categoryColor: Int = 0xFF4CAF50.toInt(),
    categoryIcon: String? = "🍔",
    amount: Double = 1000.0,
    spentAmount: Double = 200.0,
    period: BudgetPeriod = BudgetPeriod.MONTHLY,
    isActive: Boolean = true,
    alertThreshold: Float = 0.8f
): BudgetWithSpending {
    return BudgetWithSpending(
        id = id,
        userId = userId,
        categoryId = categoryId,
        categoryName = categoryName,
        categoryColor = categoryColor,
        categoryIcon = categoryIcon,
        amount = amount,
        period = period,
        startDate = System.currentTimeMillis(),
        isActive = isActive,
        alertThreshold = alertThreshold,
        spentAmount = spentAmount,
        remainingAmount = amount - spentAmount,
        progressPercentage = (spentAmount / amount).toFloat(),
        isOverBudget = spentAmount > amount,
        daysRemaining = 15
    )
}

fun createBudgetSummary(
    totalBudgets: Int = 3,
    totalBudgetAmount: Double = 3000.0,
    totalSpent: Double = 1500.0,
    overBudgetCount: Int = 0,
    warningCount: Int = 1,
    healthyCount: Int = 2
): BudgetSummary {
    return BudgetSummary(
        totalBudgets = totalBudgets,
        totalBudgetAmount = totalBudgetAmount,
        totalSpent = totalSpent,
        totalRemaining = totalBudgetAmount - totalSpent,
        overallProgress = (totalSpent / totalBudgetAmount).toFloat(),
        overBudgetCount = overBudgetCount,
        warningCount = warningCount,
        healthyCount = healthyCount
    )
}

// ==================== Debt Fixtures ====================

fun createDebt(
    id: Int = 1,
    name: String = "John Doe",
    amount: Double = 500.0,
    dueDate: Long = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000,
    type: String = "PAYABLE",
    isPaid: Boolean = false
): DebtEntity {
    return DebtEntity(
        id = id,
        name = name,
        amount = amount,
        dueDate = dueDate,
        type = type,
        isPaid = isPaid
    )
}

fun createReceivableDebt(
    id: Int = 2,
    name: String = "Jane Smith",
    amount: Double = 1000.0
): DebtEntity {
    return createDebt(
        id = id,
        name = name,
        amount = amount,
        type = "RECEIVABLE"
    )
}

// ==================== Savings Goal Fixtures ====================

fun createSavingsGoal(
    id: Int = 1,
    name: String = "Vacation",
    targetAmount: Double = 10000.0,
    currentAmount: Double = 2500.0,
    deadline: Long = System.currentTimeMillis() + 180 * 24 * 60 * 60 * 1000,
    icon: Int = 0,
    color: Int = 0xFF2196F3.toInt()
): SavingsGoalEntity {
    return SavingsGoalEntity(
        id = id,
        name = name,
        targetAmount = targetAmount,
        currentAmount = currentAmount,
        deadline = deadline,
        icon = icon,
        color = color
    )
}

// ==================== Firebase User Mock ====================

fun createMockFirebaseUser(
    uid: String = "test_user",
    email: String = "test@example.com",
    displayName: String = "Test User"
): FirebaseUser {
    return org.mockito.kotlin.mock<FirebaseUser> {
        on { getUid() } doReturn uid
        on { getEmail() } doReturn email
        on { getDisplayName() } doReturn displayName
    }
}
