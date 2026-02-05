package com.pyera.app.di

import com.pyera.app.data.repository.TransactionRepository
import com.pyera.app.domain.analysis.SpendingAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Domain module providing domain layer dependencies.
 * Includes analyzers and use cases.
 */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideSpendingAnalyzer(
        transactionRepository: TransactionRepository
    ): SpendingAnalyzer {
        return SpendingAnalyzer(transactionRepository)
    }
}
