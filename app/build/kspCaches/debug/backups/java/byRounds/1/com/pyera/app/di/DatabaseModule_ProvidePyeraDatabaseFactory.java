package com.pyera.app.di;

import android.content.Context;
import com.pyera.app.data.local.PyeraDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvidePyeraDatabaseFactory implements Factory<PyeraDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvidePyeraDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PyeraDatabase get() {
    return providePyeraDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvidePyeraDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvidePyeraDatabaseFactory(contextProvider);
  }

  public static PyeraDatabase providePyeraDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePyeraDatabase(context));
  }
}
