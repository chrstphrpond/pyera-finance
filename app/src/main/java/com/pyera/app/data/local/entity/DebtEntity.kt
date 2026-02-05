package com.pyera.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debts",
    indices = [
        Index(value = ["dueDate"], name = "idx_debts_due_date"),
        Index(value = ["isPaid"], name = "idx_debts_status"),
        Index(value = ["type"], name = "idx_debts_type")
    ]
)
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String, // Person or entity name
    val amount: Double,
    val dueDate: Long,
    val type: String, // "PAYABLE" (I owe) or "RECEIVABLE" (Owed to me)
    val isPaid: Boolean = false
)
