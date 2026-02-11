package com.pyera.app.worker

import com.pyera.app.data.local.entity.RecurringFrequency
import com.pyera.app.data.local.entity.RecurringTransactionEntity
import com.pyera.app.data.local.entity.TransactionEntity
import com.pyera.app.data.local.entity.TransactionType

/**
 * Test fixtures for Worker tests
 */
object WorkerTestFixtures {

    private var idCounter = 1L

    fun recurringTransaction(
        id: Long = idCounter++,
        amount: Double = 100.0,
        type: TransactionType = TransactionType.EXPENSE,
        categoryId: Long? = 1L,
        accountId: Long? = 1L,
        description: String = "Test Recurring Transaction",
        frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
        startDate: Long = System.currentTimeMillis(),
        endDate: Long? = null,
        nextDueDate: Long = System.currentTimeMillis(),
        isActive: Boolean = true
    ): RecurringTransactionEntity {
        return RecurringTransactionEntity(
            id = id,
            amount = amount,
            type = type,
            categoryId = categoryId,
            accountId = accountId,
            description = description,
            frequency = frequency,
            startDate = startDate,
            endDate = endDate,
            nextDueDate = nextDueDate,
            isActive = isActive
        )
    }

    fun transaction(
        id: Long = 0,
        amount: Double = 100.0,
        note: String = "Test Transaction",
        date: Long = System.currentTimeMillis(),
        type: String = "EXPENSE",
        categoryId: Int? = 1,
        accountId: Long = 1,
        userId: String = "test_user_123"
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

    fun dueRecurringTransactions(count: Int): List<RecurringTransactionEntity> {
        return (1..count).map { index ->
            recurringTransaction(
                id = index.toLong(),
                amount = 100.0 * index,
                description = "Recurring $index",
                nextDueDate = System.currentTimeMillis() - 86400000 // Yesterday (due)
            )
        }
    }

    fun resetCounter() {
        idCounter = 1L
    }
}
