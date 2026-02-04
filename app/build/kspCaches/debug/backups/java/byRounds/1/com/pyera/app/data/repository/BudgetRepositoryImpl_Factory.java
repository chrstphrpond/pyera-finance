package com.pyera.app.data.repository;

import com.pyera.app.data.local.dao.BudgetDao;
import com.pyera.app.data.local.dao.TransactionDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class BudgetRepositoryImpl_Factory implements Factory<BudgetRepositoryImpl> {
  private final Provider<BudgetDao> budgetDaoProvider;

  private final Provider<TransactionDao> transactionDaoProvider;

  public BudgetRepositoryImpl_Factory(Provider<BudgetDao> budgetDaoProvider,
      Provider<TransactionDao> transactionDaoProvider) {
    this.budgetDaoProvider = budgetDaoProvider;
    this.transactionDaoProvider = transactionDaoProvider;
  }

  @Override
  public BudgetRepositoryImpl get() {
    return newInstance(budgetDaoProvider.get(), transactionDaoProvider.get());
  }

  public static BudgetRepositoryImpl_Factory create(Provider<BudgetDao> budgetDaoProvider,
      Provider<TransactionDao> transactionDaoProvider) {
    return new BudgetRepositoryImpl_Factory(budgetDaoProvider, transactionDaoProvider);
  }

  public static BudgetRepositoryImpl newInstance(BudgetDao budgetDao,
      TransactionDao transactionDao) {
    return new BudgetRepositoryImpl(budgetDao, transactionDao);
  }
}
