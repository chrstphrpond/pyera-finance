package com.pyera.app.di

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.pyera.app.data.biometric.BiometricAuthManager
import com.pyera.app.domain.ocr.ReceiptParser
import com.pyera.app.data.security.AppLockManager
import com.pyera.app.data.security.BiometricHelper
import com.pyera.app.data.security.SecurityChecker
import com.pyera.app.data.security.SecurityPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pyera.app.BuildConfig
import com.pyera.app.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideBiometricAuthManager(
        @ApplicationContext context: Context
    ): BiometricAuthManager = BiometricAuthManager(context)

    @Provides
    @Singleton
    fun provideReceiptParser(): ReceiptParser = ReceiptParser()

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    @Provides
    @Singleton
    fun provideSecurityChecker(@ApplicationContext context: Context): SecurityChecker = 
        SecurityChecker(context)

    @Provides
    @Singleton
    fun provideSecurityPreferences(
        @ApplicationContext context: Context
    ): SecurityPreferences = SecurityPreferences(context)

    @Provides
    @Singleton
    fun provideAppLockManager(
        securityPreferences: SecurityPreferences,
        authRepository: AuthRepository
    ): AppLockManager = AppLockManager(securityPreferences, authRepository)

    @Provides
    @Singleton
    fun provideBiometricHelper(
        biometricAuthManager: BiometricAuthManager,
        appLockManager: AppLockManager
    ): BiometricHelper = BiometricHelper(biometricAuthManager, appLockManager)

}
