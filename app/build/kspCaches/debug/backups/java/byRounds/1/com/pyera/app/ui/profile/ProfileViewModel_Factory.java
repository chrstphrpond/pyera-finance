package com.pyera.app.ui.profile;

import com.pyera.app.data.biometric.BiometricAuthManager;
import com.pyera.app.data.repository.AuthRepository;
import com.pyera.app.data.repository.BudgetRepository;
import com.pyera.app.data.repository.SavingsRepository;
import com.pyera.app.data.repository.TransactionRepository;
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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<TransactionRepository> transactionRepositoryProvider;

  private final Provider<SavingsRepository> savingsRepositoryProvider;

  private final Provider<BudgetRepository> budgetRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<BiometricAuthManager> biometricAuthManagerProvider;

  public ProfileViewModel_Factory(Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<SavingsRepository> savingsRepositoryProvider,
      Provider<BudgetRepository> budgetRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<BiometricAuthManager> biometricAuthManagerProvider) {
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.savingsRepositoryProvider = savingsRepositoryProvider;
    this.budgetRepositoryProvider = budgetRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.biometricAuthManagerProvider = biometricAuthManagerProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(transactionRepositoryProvider.get(), savingsRepositoryProvider.get(), budgetRepositoryProvider.get(), authRepositoryProvider.get(), biometricAuthManagerProvider.get());
  }

  public static ProfileViewModel_Factory create(
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<SavingsRepository> savingsRepositoryProvider,
      Provider<BudgetRepository> budgetRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<BiometricAuthManager> biometricAuthManagerProvider) {
    return new ProfileViewModel_Factory(transactionRepositoryProvider, savingsRepositoryProvider, budgetRepositoryProvider, authRepositoryProvider, biometricAuthManagerProvider);
  }

  public static ProfileViewModel newInstance(TransactionRepository transactionRepository,
      SavingsRepository savingsRepository, BudgetRepository budgetRepository,
      AuthRepository authRepository, BiometricAuthManager biometricAuthManager) {
    return new ProfileViewModel(transactionRepository, savingsRepository, budgetRepository, authRepository, biometricAuthManager);
  }
}
