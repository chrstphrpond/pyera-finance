package com.pyera.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["categoryId"], name = "idx_transactions_category"),
        Index(value = ["date"], name = "idx_transactions_date"),
        Index(value = ["type"], name = "idx_transactions_type")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val note: String,
    val date: Long, // Epoch timestamp
    val type: String, // "INCOME" or "EXPENSE"
    val categoryId: Int?
)
