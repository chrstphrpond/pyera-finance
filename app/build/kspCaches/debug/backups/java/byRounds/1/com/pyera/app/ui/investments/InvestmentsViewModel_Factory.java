package com.pyera.app.ui.investments;

import com.pyera.app.data.repository.InvestmentRepository;
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
public final class InvestmentsViewModel_Factory implements Factory<InvestmentsViewModel> {
  private final Provider<InvestmentRepository> investmentRepositoryProvider;

  public InvestmentsViewModel_Factory(Provider<InvestmentRepository> investmentRepositoryProvider) {
    this.investmentRepositoryProvider = investmentRepositoryProvider;
  }

  @Override
  public InvestmentsViewModel get() {
    return newInstance(investmentRepositoryProvider.get());
  }

  public static InvestmentsViewModel_Factory create(
      Provider<InvestmentRepository> investmentRepositoryProvider) {
    return new InvestmentsViewModel_Factory(investmentRepositoryProvider);
  }

  public static InvestmentsViewModel newInstance(InvestmentRepository investmentRepository) {
    return new InvestmentsViewModel(investmentRepository);
  }
}
