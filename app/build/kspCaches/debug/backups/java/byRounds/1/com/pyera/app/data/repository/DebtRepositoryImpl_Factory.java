package com.pyera.app.data.repository;

import com.pyera.app.data.local.dao.DebtDao;
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
public final class DebtRepositoryImpl_Factory implements Factory<DebtRepositoryImpl> {
  private final Provider<DebtDao> debtDaoProvider;

  public DebtRepositoryImpl_Factory(Provider<DebtDao> debtDaoProvider) {
    this.debtDaoProvider = debtDaoProvider;
  }

  @Override
  public DebtRepositoryImpl get() {
    return newInstance(debtDaoProvider.get());
  }

  public static DebtRepositoryImpl_Factory create(Provider<DebtDao> debtDaoProvider) {
    return new DebtRepositoryImpl_Factory(debtDaoProvider);
  }

  public static DebtRepositoryImpl newInstance(DebtDao debtDao) {
    return new DebtRepositoryImpl(debtDao);
  }
}
