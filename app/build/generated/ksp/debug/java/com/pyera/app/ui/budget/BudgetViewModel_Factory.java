package com.pyera.app.ui.budget;

import com.pyera.app.data.repository.AuthRepository;
import com.pyera.app.data.repository.BudgetRepository;
import com.pyera.app.data.repository.CategoryRepository;
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
public final class BudgetViewModel_Factory implements Factory<BudgetViewModel> {
  private final Provider<BudgetRepository> budgetRepositoryProvider;

  private final Provider<CategoryRepository> categoryRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public BudgetViewModel_Factory(Provider<BudgetRepository> budgetRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.budgetRepositoryProvider = budgetRepositoryProvider;
    this.categoryRepositoryProvider = categoryRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public BudgetViewModel get() {
    return newInstance(budgetRepositoryProvider.get(), categoryRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static BudgetViewModel_Factory create(Provider<BudgetRepository> budgetRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new BudgetViewModel_Factory(budgetRepositoryProvider, categoryRepositoryProvider, authRepositoryProvider);
  }

  public static BudgetViewModel newInstance(BudgetRepository budgetRepository,
      CategoryRepository categoryRepository, AuthRepository authRepository) {
    return new BudgetViewModel(budgetRepository, categoryRepository, authRepository);
  }
}
