package com.pyera.app.di

import com.pyera.app.domain.analysis.SpendingAnalyzer
import com.pyera.app.domain.repository.SpendingDataRepository
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
        spendingDataRepository: SpendingDataRepository
    ): SpendingAnalyzer {
        return SpendingAnalyzer(spendingDataRepository)
    }
}
