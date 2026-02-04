package com.pyera.app.ui.analysis;

import com.pyera.app.data.repository.AnalysisRepository;
import com.pyera.app.data.repository.CategoryRepository;
import com.pyera.app.data.repository.TransactionRepository;
import com.pyera.app.domain.smart.PredictiveBudgetUseCase;
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
public final class AnalysisViewModel_Factory implements Factory<AnalysisViewModel> {
  private final Provider<AnalysisRepository> analysisRepositoryProvider;

  private final Provider<CategoryRepository> categoryRepositoryProvider;

  private final Provider<PredictiveBudgetUseCase> predictiveBudgetUseCaseProvider;

  private final Provider<TransactionRepository> transactionRepositoryProvider;

  public AnalysisViewModel_Factory(Provider<AnalysisRepository> analysisRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<PredictiveBudgetUseCase> predictiveBudgetUseCaseProvider,
      Provider<TransactionRepository> transactionRepositoryProvider) {
    this.analysisRepositoryProvider = analysisRepositoryProvider;
    this.categoryRepositoryProvider = categoryRepositoryProvider;
    this.predictiveBudgetUseCaseProvider = predictiveBudgetUseCaseProvider;
    this.transactionRepositoryProvider = transactionRepositoryProvider;
  }

  @Override
  public AnalysisViewModel get() {
    return newInstance(analysisRepositoryProvider.get(), categoryRepositoryProvider.get(), predictiveBudgetUseCaseProvider.get(), transactionRepositoryProvider.get());
  }

  public static AnalysisViewModel_Factory create(
      Provider<AnalysisRepository> analysisRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<PredictiveBudgetUseCase> predictiveBudgetUseCaseProvider,
      Provider<TransactionRepository> transactionRepositoryProvider) {
    return new AnalysisViewModel_Factory(analysisRepositoryProvider, categoryRepositoryProvider, predictiveBudgetUseCaseProvider, transactionRepositoryProvider);
  }

  public static AnalysisViewModel newInstance(AnalysisRepository analysisRepository,
      CategoryRepository categoryRepository, PredictiveBudgetUseCase predictiveBudgetUseCase,
      TransactionRepository transactionRepository) {
    return new AnalysisViewModel(analysisRepository, categoryRepository, predictiveBudgetUseCase, transactionRepository);
  }
}
