package com.pyera.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a user-defined transaction categorization rule.
 * Rules automatically categorize transactions based on description patterns.
 */
@Entity(
    tableName = "transaction_rules",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"], name = "idx_rules_user"),
        Index(value = ["isActive"], name = "idx_rules_active"),
        Index(value = ["categoryId"], name = "idx_rules_category")
    ]
)
data class TransactionRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val pattern: String,           // Text pattern to match
    val matchType: String,         // "CONTAINS", "STARTS_WITH", "ENDS_WITH", "EXACT", "REGEX"
    val categoryId: Int,           // Target category
    val priority: Int = 0,         // Higher = applied first
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Enum representing different match types for transaction rules.
 */
enum class MatchType {
    CONTAINS,
    STARTS_WITH,
    ENDS_WITH,
    EXACT,
    REGEX;

    companion object {
        fun fromString(value: String): MatchType {
            return entries.find { it.name == value } ?: CONTAINS
        }
    }
}

/**
 * Extension function to check if a description matches this rule's pattern.
 */
fun TransactionRuleEntity.matches(description: String): Boolean {
    val descLower = description.lowercase()
    val patternLower = pattern.lowercase()
    
    return when (matchType) {
        MatchType.CONTAINS.name -> descLower.contains(patternLower)
        MatchType.STARTS_WITH.name -> descLower.startsWith(patternLower)
        MatchType.ENDS_WITH.name -> descLower.endsWith(patternLower)
        MatchType.EXACT.name -> descLower == patternLower
        MatchType.REGEX.name -> {
            runCatching {
                descLower.matches(Regex(pattern, RegexOption.IGNORE_CASE))
            }.getOrDefault(false)
        }
        else -> descLower.contains(patternLower)
    }
}
