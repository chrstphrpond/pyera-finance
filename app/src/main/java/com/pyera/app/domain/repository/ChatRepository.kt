package com.pyera.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(prompt: String): Flow<String>
}
