package com.pyera.app.domain.model

/**
 * Domain model representing a spending category.
 */
data class Category(
    val id: Int,
    val name: String,
    val icon: String?,
    val color: Int,
    val type: String
)
