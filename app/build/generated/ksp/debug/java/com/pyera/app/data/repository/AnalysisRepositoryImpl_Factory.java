package com.pyera.app.data.repository;

import com.pyera.app.data.local.dao.TransactionDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AnalysisRepositoryImpl_Factory implements Factory<AnalysisRepositoryImpl> {
  private final Provider<TransactionDao> transactionDaoProvider;

  public AnalysisRepositoryImpl_Factory(Provider<TransactionDao> transactionDaoProvider) {
    this.transactionDaoProvider = transactionDaoProvider;
  }

  @Override
  public AnalysisRepositoryImpl get() {
    return newInstance(transactionDaoProvider.get());
  }

  public static AnalysisRepositoryImpl_Factory create(
      Provider<TransactionDao> transactionDaoProvider) {
    return new AnalysisRepositoryImpl_Factory(transactionDaoProvider);
  }

  public static AnalysisRepositoryImpl newInstance(TransactionDao transactionDao) {
    return new AnalysisRepositoryImpl(transactionDao);
  }
}
