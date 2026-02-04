package com.pyera.app.ui.transaction;

import com.pyera.app.data.repository.CategoryRepository;
import com.pyera.app.data.repository.OcrRepository;
import com.pyera.app.data.repository.TransactionRepository;
import com.pyera.app.domain.smart.SmartCategorizer;
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
public final class TransactionViewModel_Factory implements Factory<TransactionViewModel> {
  private final Provider<TransactionRepository> transactionRepositoryProvider;

  private final Provider<CategoryRepository> categoryRepositoryProvider;

  private final Provider<OcrRepository> ocrRepositoryProvider;

  private final Provider<SmartCategorizer> smartCategorizerProvider;

  public TransactionViewModel_Factory(Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<OcrRepository> ocrRepositoryProvider,
      Provider<SmartCategorizer> smartCategorizerProvider) {
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.categoryRepositoryProvider = categoryRepositoryProvider;
    this.ocrRepositoryProvider = ocrRepositoryProvider;
    this.smartCategorizerProvider = smartCategorizerProvider;
  }

  @Override
  public TransactionViewModel get() {
    return newInstance(transactionRepositoryProvider.get(), categoryRepositoryProvider.get(), ocrRepositoryProvider.get(), smartCategorizerProvider.get());
  }

  public static TransactionViewModel_Factory create(
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<OcrRepository> ocrRepositoryProvider,
      Provider<SmartCategorizer> smartCategorizerProvider) {
    return new TransactionViewModel_Factory(transactionRepositoryProvider, categoryRepositoryProvider, ocrRepositoryProvider, smartCategorizerProvider);
  }

  public static TransactionViewModel newInstance(TransactionRepository transactionRepository,
      CategoryRepository categoryRepository, OcrRepository ocrRepository,
      SmartCategorizer smartCategorizer) {
    return new TransactionViewModel(transactionRepository, categoryRepository, ocrRepository, smartCategorizer);
  }
}
