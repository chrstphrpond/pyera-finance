package com.pyera.app.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class GoogleAuthHelper_Factory implements Factory<GoogleAuthHelper> {
  @Override
  public GoogleAuthHelper get() {
    return newInstance();
  }

  public static GoogleAuthHelper_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static GoogleAuthHelper newInstance() {
    return new GoogleAuthHelper();
  }

  private static final class InstanceHolder {
    private static final GoogleAuthHelper_Factory INSTANCE = new GoogleAuthHelper_Factory();
  }
}
