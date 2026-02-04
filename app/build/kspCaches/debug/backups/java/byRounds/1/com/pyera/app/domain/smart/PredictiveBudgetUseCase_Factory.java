package com.pyera.app.domain.smart;

import com.pyera.app.data.repository.TransactionRepository;
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
public final class PredictiveBudgetUseCase_Factory implements Factory<PredictiveBudgetUseCase> {
  private final Provider<TransactionRepository> transactionRepositoryProvider;

  public PredictiveBudgetUseCase_Factory(
      Provider<TransactionRepository> transactionRepositoryProvider) {
    this.transactionRepositoryProvider = transactionRepositoryProvider;
  }

  @Override
  public PredictiveBudgetUseCase get() {
    return newInstance(transactionRepositoryProvider.get());
  }

  public static PredictiveBudgetUseCase_Factory create(
      Provider<TransactionRepository> transactionRepositoryProvider) {
    return new PredictiveBudgetUseCase_Factory(transactionRepositoryProvider);
  }

  public static PredictiveBudgetUseCase newInstance(TransactionRepository transactionRepository) {
    return new PredictiveBudgetUseCase(transactionRepository);
  }
}
