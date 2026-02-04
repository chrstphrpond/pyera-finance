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
public final class ChatRepositoryImpl_Factory implements Factory<ChatRepositoryImpl> {
  @Override
  public ChatRepositoryImpl get() {
    return newInstance();
  }

  public static ChatRepositoryImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ChatRepositoryImpl newInstance() {
    return new ChatRepositoryImpl();
  }

  private static final class InstanceHolder {
    private static final ChatRepositoryImpl_Factory INSTANCE = new ChatRepositoryImpl_Factory();
  }
}
