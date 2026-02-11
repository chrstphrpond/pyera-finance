package com.pyera.app.di

import com.pyera.app.data.repository.AccountRepositoryImpl
import com.pyera.app.data.repository.AnalysisRepositoryImpl
import com.pyera.app.data.repository.BillRepositoryImpl
import com.pyera.app.data.repository.BudgetRepositoryImpl
import com.pyera.app.data.repository.CategoryRepositoryImpl
import com.pyera.app.data.repository.ChatRepositoryImpl
import com.pyera.app.data.repository.DebtRepositoryImpl
import com.pyera.app.data.repository.InvestmentRepositoryImpl
import com.pyera.app.data.repository.OcrRepositoryImpl
import com.pyera.app.data.repository.RecurringTransactionRepositoryImpl
import com.pyera.app.data.repository.SavingsRepositoryImpl
import com.pyera.app.data.repository.TransactionRepositoryImpl
import com.pyera.app.data.repository.TransactionRuleRepositoryImpl
import com.pyera.app.data.repository.TransactionTemplateRepositoryImpl
import com.pyera.app.data.repository.SpendingDataRepositoryImpl
import com.pyera.app.domain.repository.AccountRepository
import com.pyera.app.domain.repository.AnalysisRepository
import com.pyera.app.domain.repository.AuthRepository
import com.pyera.app.domain.repository.AuthRepositoryImpl
import com.pyera.app.domain.repository.BillRepository
import com.pyera.app.domain.repository.BudgetRepository
import com.pyera.app.domain.repository.CategoryRepository
import com.pyera.app.domain.repository.ChatRepository
import com.pyera.app.domain.repository.DebtRepository
import com.pyera.app.domain.repository.InvestmentRepository
import com.pyera.app.domain.repository.OcrRepository
import com.pyera.app.domain.repository.RecurringTransactionRepository
import com.pyera.app.domain.repository.SavingsRepository
import com.pyera.app.domain.repository.TransactionRepository
import com.pyera.app.domain.repository.TransactionRuleRepository
import com.pyera.app.domain.repository.TransactionTemplateRepository
import com.pyera.app.domain.repository.SpendingDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository module for dependency injection.
 * Provides repository bindings for all repositories using @Binds.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindBillRepository(
        impl: BillRepositoryImpl
    ): BillRepository

    @Binds
    @Singleton
    abstract fun bindDebtRepository(
        impl: DebtRepositoryImpl
    ): DebtRepository

    @Binds
    @Singleton
    abstract fun bindInvestmentRepository(
        impl: InvestmentRepositoryImpl
    ): InvestmentRepository

    @Binds
    @Singleton
    abstract fun bindSavingsRepository(
        impl: SavingsRepositoryImpl
    ): SavingsRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindOcrRepository(
        impl: OcrRepositoryImpl
    ): OcrRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindAnalysisRepository(
        impl: AnalysisRepositoryImpl
    ): AnalysisRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        impl: BudgetRepositoryImpl
    ): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        impl: AccountRepositoryImpl
    ): AccountRepository

    @Binds
    @Singleton
    abstract fun bindRecurringTransactionRepository(
        impl: RecurringTransactionRepositoryImpl
    ): RecurringTransactionRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRuleRepository(
        impl: TransactionRuleRepositoryImpl
    ): TransactionRuleRepository

    @Binds
    @Singleton
    abstract fun bindTransactionTemplateRepository(
        impl: TransactionTemplateRepositoryImpl
    ): TransactionTemplateRepository

    @Binds
    @Singleton
    abstract fun bindSpendingDataRepository(
        impl: SpendingDataRepositoryImpl
    ): SpendingDataRepository
}
