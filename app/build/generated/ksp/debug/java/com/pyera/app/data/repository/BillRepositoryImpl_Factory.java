package com.pyera.app.data.repository;

import com.pyera.app.data.local.dao.BillDao;
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
public final class BillRepositoryImpl_Factory implements Factory<BillRepositoryImpl> {
  private final Provider<BillDao> billDaoProvider;

  public BillRepositoryImpl_Factory(Provider<BillDao> billDaoProvider) {
    this.billDaoProvider = billDaoProvider;
  }

  @Override
  public BillRepositoryImpl get() {
    return newInstance(billDaoProvider.get());
  }

  public static BillRepositoryImpl_Factory create(Provider<BillDao> billDaoProvider) {
    return new BillRepositoryImpl_Factory(billDaoProvider);
  }

  public static BillRepositoryImpl newInstance(BillDao billDao) {
    return new BillRepositoryImpl(billDao);
  }
}
