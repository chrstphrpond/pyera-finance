package com.pyera.app.ui.savings;

import com.pyera.app.data.repository.SavingsRepository;
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
public final class SavingsViewModel_Factory implements Factory<SavingsViewModel> {
  private final Provider<SavingsRepository> savingsRepositoryProvider;

  public SavingsViewModel_Factory(Provider<SavingsRepository> savingsRepositoryProvider) {
    this.savingsRepositoryProvider = savingsRepositoryProvider;
  }

  @Override
  public SavingsViewModel get() {
    return newInstance(savingsRepositoryProvider.get());
  }

  public static SavingsViewModel_Factory create(
      Provider<SavingsRepository> savingsRepositoryProvider) {
    return new SavingsViewModel_Factory(savingsRepositoryProvider);
  }

  public static SavingsViewModel newInstance(SavingsRepository savingsRepository) {
    return new SavingsViewModel(savingsRepository);
  }
}
