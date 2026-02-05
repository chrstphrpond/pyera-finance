package com.pyera.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["type"], name = "idx_categories_type"),
        Index(value = ["name"], name = "idx_categories_name")
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val icon: String, // Store icon name or unicode
    val color: Int, // Store color as Int
    val type: String // "INCOME" or "EXPENSE"
)
