package com.pyera.app.ui.bills;

import com.pyera.app.data.repository.BillRepository;
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
public final class BillsViewModel_Factory implements Factory<BillsViewModel> {
  private final Provider<BillRepository> billRepositoryProvider;

  public BillsViewModel_Factory(Provider<BillRepository> billRepositoryProvider) {
    this.billRepositoryProvider = billRepositoryProvider;
  }

  @Override
  public BillsViewModel get() {
    return newInstance(billRepositoryProvider.get());
  }

  public static BillsViewModel_Factory create(Provider<BillRepository> billRepositoryProvider) {
    return new BillsViewModel_Factory(billRepositoryProvider);
  }

  public static BillsViewModel newInstance(BillRepository billRepository) {
    return new BillsViewModel(billRepository);
  }
}
