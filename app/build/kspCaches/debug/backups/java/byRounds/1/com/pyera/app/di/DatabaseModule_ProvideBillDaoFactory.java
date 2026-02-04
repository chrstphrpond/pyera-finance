package com.pyera.app.di;

import com.pyera.app.data.local.PyeraDatabase;
import com.pyera.app.data.local.dao.BillDao;
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
public final class DatabaseModule_ProvideBillDaoFactory implements Factory<BillDao> {
  private final Provider<PyeraDatabase> databaseProvider;

  public DatabaseModule_ProvideBillDaoFactory(Provider<PyeraDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public BillDao get() {
    return provideBillDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideBillDaoFactory create(
      Provider<PyeraDatabase> databaseProvider) {
    return new DatabaseModule_ProvideBillDaoFactory(databaseProvider);
  }

  public static BillDao provideBillDao(PyeraDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideBillDao(database));
  }
}
