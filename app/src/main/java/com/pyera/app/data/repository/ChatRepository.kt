package com.pyera.app.data.repository

import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(prompt: String): Flow<String>
}
