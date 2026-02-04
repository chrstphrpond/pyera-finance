package com.pyera.app.ui.debt;

import com.pyera.app.data.repository.DebtRepository;
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
public final class DebtViewModel_Factory implements Factory<DebtViewModel> {
  private final Provider<DebtRepository> debtRepositoryProvider;

  public DebtViewModel_Factory(Provider<DebtRepository> debtRepositoryProvider) {
    this.debtRepositoryProvider = debtRepositoryProvider;
  }

  @Override
  public DebtViewModel get() {
    return newInstance(debtRepositoryProvider.get());
  }

  public static DebtViewModel_Factory create(Provider<DebtRepository> debtRepositoryProvider) {
    return new DebtViewModel_Factory(debtRepositoryProvider);
  }

  public static DebtViewModel newInstance(DebtRepository debtRepository) {
    return new DebtViewModel(debtRepository);
  }
}
