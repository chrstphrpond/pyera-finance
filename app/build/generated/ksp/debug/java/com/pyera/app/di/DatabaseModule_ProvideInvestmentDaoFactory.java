package com.pyera.app.di;

import com.pyera.app.data.local.PyeraDatabase;
import com.pyera.app.data.local.dao.InvestmentDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideInvestmentDaoFactory implements Factory<InvestmentDao> {
  private final Provider<PyeraDatabase> databaseProvider;

  public DatabaseModule_ProvideInvestmentDaoFactory(Provider<PyeraDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public InvestmentDao get() {
    return provideInvestmentDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideInvestmentDaoFactory create(
      Provider<PyeraDatabase> databaseProvider) {
    return new DatabaseModule_ProvideInvestmentDaoFactory(databaseProvider);
  }

  public static InvestmentDao provideInvestmentDao(PyeraDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideInvestmentDao(database));
  }
}
