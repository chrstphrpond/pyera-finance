package com.pyera.app.domain.smart;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class SmartCategorizer_Factory implements Factory<SmartCategorizer> {
  @Override
  public SmartCategorizer get() {
    return newInstance();
  }

  public static SmartCategorizer_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SmartCategorizer newInstance() {
    return new SmartCategorizer();
  }

  private static final class InstanceHolder {
    private static final SmartCategorizer_Factory INSTANCE = new SmartCategorizer_Factory();
  }
}
