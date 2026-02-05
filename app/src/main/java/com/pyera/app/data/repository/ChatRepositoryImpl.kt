package com.pyera.app.data.repository

import com.pyera.app.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor() : ChatRepository {

    override suspend fun sendMessage(prompt: String): Flow<String> = flow {
        emit("The AI chat feature has been disabled for security reasons.")
        emit("")
        emit("You can still manage your finances using all other Pyera features:")
        emit("• Track transactions and budgets")
        emit("• View spending analytics")
        emit("• Manage savings goals")
        emit("• Track debts and bills")
    }
}
