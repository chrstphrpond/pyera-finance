package com.pyera.app.domain.nlp

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.pyera.app.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NaturalLanguageParser @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val categoryRepository: CategoryRepository
) {

    data class ParsedTransaction(
        val description: String,
        val amount: Double?,
        val categoryId: Long?,
        val type: String,
        val date: Long?,
        val confidence: Float
    )

    suspend fun parse(input: String): Result<ParsedTransaction> = withContext(Dispatchers.IO) {
        try {
            val categories = categoryRepository.getAllCategories().first()
            val categoryList = categories.joinToString("\n") { "${it.id}: ${it.name}" }

            val prompt = buildString {
                appendLine("Parse this transaction: \"$input\"")
                appendLine()
                appendLine("Available categories:")
                appendLine(categoryList)
                appendLine()
                appendLine("Return ONLY a JSON object with this structure:")
                appendLine(
                    """{
                    \"description\": \"merchant or description\",
                    \"amount\": 123.45,
                    \"categoryId\": 1,
                    \"type\": \"EXPENSE\" or \"INCOME\",
                    \"date\": \"2026-02-11\" or null for today,
                    \"confidence\": 0.95
                }"""
                )
                appendLine()
                appendLine("Rules:")
                appendLine("- Amount should be positive number")
                appendLine("- Type is EXPENSE unless explicitly income-related")
                appendLine("- Date format: YYYY-MM-DD or null")
                appendLine("- Confidence is 0.0-1.0 based on parsing certainty")
            }

            val response = generativeModel.generateContent(content { text(prompt) })
            val jsonText = response.text?.trim()
                ?: return@withContext Result.failure(Exception("Empty response from AI"))

            val parsed = parseJsonResponse(jsonText)

            if (parsed.confidence < 0.7f) {
                Result.failure(Exception("Low confidence parsing: ${parsed.confidence}"))
            } else {
                Result.success(parsed)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseJsonResponse(json: String): ParsedTransaction {
        val gson = Gson()
        val sanitized = sanitizeJson(json)
        val jsonObject = gson.fromJson(sanitized, JsonObject::class.java)

        val typeValue = jsonObject.get("type")?.asString?.uppercase(Locale.getDefault())
        val type = if (typeValue == "INCOME") "INCOME" else "EXPENSE"

        return ParsedTransaction(
            description = jsonObject.get("description")?.asString.orEmpty(),
            amount = jsonObject.get("amount")?.takeIf { !it.isJsonNull }?.asDouble,
            categoryId = jsonObject.get("categoryId")?.takeIf { !it.isJsonNull }?.asLong,
            type = type,
            date = jsonObject.get("date")?.takeIf { !it.isJsonNull }?.asString?.let { parseDate(it) },
            confidence = jsonObject.get("confidence")?.takeIf { !it.isJsonNull }?.asFloat ?: 0f
        )
    }

    private fun sanitizeJson(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```")) {
            text = text.removePrefix("```")
            if (text.startsWith("json")) {
                text = text.removePrefix("json")
            }
            text = text.removeSuffix("```")
        }
        text = text.trim()
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        return if (start >= 0 && end > start) {
            text.substring(start, end + 1)
        } else {
            text
        }
    }

    private fun parseDate(dateStr: String): Long? {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse(dateStr)?.time
        } catch (e: Exception) {
            null
        }
    }
}
