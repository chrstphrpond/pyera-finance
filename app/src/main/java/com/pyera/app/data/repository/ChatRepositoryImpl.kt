package com.pyera.app.data.repository

import com.pyera.app.domain.repository.*
import com.pyera.app.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor() : ChatRepository {

    @Suppress("UnusedPrivateProperty")
    private val certificatePinner by lazy {
        if (BuildConfig.ENABLE_CERT_PINNING && BuildConfig.CERT_PIN_1.isNotBlank()) {
            CertificatePinner.Builder()
                .add("api.moonshot.cn", "sha256/${BuildConfig.CERT_PIN_1}")
                .apply {
                    if (BuildConfig.CERT_PIN_2.isNotBlank()) {
                        add("api.moonshot.cn", "sha256/${BuildConfig.CERT_PIN_2}")
                    }
                }
                .build()
        } else null
    }

    @Suppress("UnusedPrivateProperty")
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply { certificatePinner?.let { certificatePinner(it) } }
            .build()
    }

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
