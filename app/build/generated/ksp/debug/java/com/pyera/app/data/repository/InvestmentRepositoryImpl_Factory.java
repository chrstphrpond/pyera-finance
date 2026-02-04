package com.pyera.app.data.repository;

import com.pyera.app.data.local.dao.InvestmentDao;
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
public final class InvestmentRepositoryImpl_Factory implements Factory<InvestmentRepositoryImpl> {
  private final Provider<InvestmentDao> investmentDaoProvider;

  public InvestmentRepositoryImpl_Factory(Provider<InvestmentDao> investmentDaoProvider) {
    this.investmentDaoProvider = investmentDaoProvider;
  }

  @Override
  public InvestmentRepositoryImpl get() {
    return newInstance(investmentDaoProvider.get());
  }

  public static InvestmentRepositoryImpl_Factory create(
      Provider<InvestmentDao> investmentDaoProvider) {
    return new InvestmentRepositoryImpl_Factory(investmentDaoProvider);
  }

  public static InvestmentRepositoryImpl newInstance(InvestmentDao investmentDao) {
    return new InvestmentRepositoryImpl(investmentDao);
  }
}
