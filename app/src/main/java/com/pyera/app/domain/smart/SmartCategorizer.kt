package com.pyera.app.domain.smart

import javax.inject.Inject

class SmartCategorizer @Inject constructor() {

    private val keywords = mapOf(
        "uber" to "Transport",
        "grab" to "Transport",
        "bus" to "Transport",
        "train" to "Transport",
        "starbucks" to "Food",
        "mcdonalds" to "Food",
        "restaurant" to "Food",
        "grocery" to "Food",
        "supermarket" to "Food",
        "netflix" to "Entertainment",
        "spotify" to "Entertainment",
        "cinema" to "Entertainment",
        "salary" to "Income",
        "rent" to "Housing",
        "electric" to "Utilities",
        "water" to "Utilities",
        "internet" to "Utilities",
        "gym" to "Health"
    )

    fun predict(note: String): String? {
        val lowerNote = note.lowercase()
        for ((keyword, category) in keywords) {
            if (lowerNote.contains(keyword)) {
                return category
            }
        }
        return null
    }
}
