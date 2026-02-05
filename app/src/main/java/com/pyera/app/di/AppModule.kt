package com.pyera.app.di

import android.content.Context
import com.pyera.app.data.biometric.BiometricAuthManager
import com.pyera.app.domain.ocr.ReceiptParser
import com.pyera.app.security.SecurityChecker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    fun provideSecurityChecker(@ApplicationContext context: Context): SecurityChecker = 
        SecurityChecker(context)

}
