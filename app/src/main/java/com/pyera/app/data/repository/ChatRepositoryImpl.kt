package com.pyera.app.data.repository

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Kimi API (Moonshot AI) Request/Response Models
data class KimiMessage(
    val role: String,
    val content: String
)

data class KimiRequest(
    val model: String = "moonshot-v1-8k",
    val messages: List<KimiMessage>,
    val temperature: Double = 0.7
)

data class KimiChoice(
    val message: KimiMessage
)

data class KimiResponse(
    val choices: List<KimiChoice>?,
    val error: KimiError?
)

data class KimiError(
    val message: String,
    val type: String?
)

@Singleton
class ChatRepositoryImpl @Inject constructor() : ChatRepository {

    private val apiKey = com.pyera.app.BuildConfig.KIMI_API_KEY
    private val baseUrl = "https://api.moonshot.cn/v1/chat/completions"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    // System prompt to give context about Pyera
    private val systemPrompt = """You are Pyera AI, a helpful personal finance assistant. 
You help users with:
- Understanding their spending habits
- Providing budgeting advice
- Answering questions about the Pyera app features
- Giving general financial tips
- Analyzing transaction data

Keep responses concise, friendly, and focused on personal finance topics.
If asked about something unrelated to finance or the app, politely redirect to financial topics."""

    override suspend fun sendMessage(prompt: String): Flow<String> = flow {
        try {
            val messages = listOf(
                KimiMessage(role = "system", content = systemPrompt),
                KimiMessage(role = "user", content = prompt)
            )
            
            val requestBody = KimiRequest(messages = messages)
            val jsonBody = gson.toJson(requestBody)
            
            val request = Request.Builder()
                .url(baseUrl)
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(jsonBody.toRequestBody(mediaType))
                .build()
            
            val responseBody = withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected response code: ${response.code}")
                    }
                    response.body?.string()
                }
            }
            
            if (responseBody != null) {
                val kimiResponse = gson.fromJson(responseBody, KimiResponse::class.java)
                
                if (kimiResponse.error != null) {
                    emit("Error: ${kimiResponse.error.message}")
                } else {
                    val content = kimiResponse.choices?.firstOrNull()?.message?.content
                    emit(content ?: "I'm sorry, I couldn't understand that.")
                }
            } else {
                emit("Error: Empty response from server")
            }
        } catch (e: IOException) {
            emit("Network error: ${e.message ?: "Please check your internet connection"}")
        } catch (e: Exception) {
            emit("Error: ${e.message ?: "Something went wrong"}")
        }
    }
}
