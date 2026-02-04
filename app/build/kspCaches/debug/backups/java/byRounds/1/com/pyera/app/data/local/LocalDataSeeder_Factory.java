package com.pyera.app.data.local;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class LocalDataSeeder_Factory implements Factory<LocalDataSeeder> {
  private final Provider<Context> contextProvider;

  private final Provider<PyeraDatabase> databaseProvider;

  public LocalDataSeeder_Factory(Provider<Context> contextProvider,
      Provider<PyeraDatabase> databaseProvider) {
    this.contextProvider = contextProvider;
    this.databaseProvider = databaseProvider;
  }

  @Override
  public LocalDataSeeder get() {
    return newInstance(contextProvider.get(), databaseProvider.get());
  }

  public static LocalDataSeeder_Factory create(Provider<Context> contextProvider,
      Provider<PyeraDatabase> databaseProvider) {
    return new LocalDataSeeder_Factory(contextProvider, databaseProvider);
  }

  public static LocalDataSeeder newInstance(Context context, PyeraDatabase database) {
    return new LocalDataSeeder(context, database);
  }
}
