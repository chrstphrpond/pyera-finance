package com.pyera.app.data.repository;

import com.pyera.app.data.local.dao.SavingsGoalDao;
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
public final class SavingsRepositoryImpl_Factory implements Factory<SavingsRepositoryImpl> {
  private final Provider<SavingsGoalDao> savingsGoalDaoProvider;

  public SavingsRepositoryImpl_Factory(Provider<SavingsGoalDao> savingsGoalDaoProvider) {
    this.savingsGoalDaoProvider = savingsGoalDaoProvider;
  }

  @Override
  public SavingsRepositoryImpl get() {
    return newInstance(savingsGoalDaoProvider.get());
  }

  public static SavingsRepositoryImpl_Factory create(
      Provider<SavingsGoalDao> savingsGoalDaoProvider) {
    return new SavingsRepositoryImpl_Factory(savingsGoalDaoProvider);
  }

  public static SavingsRepositoryImpl newInstance(SavingsGoalDao savingsGoalDao) {
    return new SavingsRepositoryImpl(savingsGoalDao);
  }
}
