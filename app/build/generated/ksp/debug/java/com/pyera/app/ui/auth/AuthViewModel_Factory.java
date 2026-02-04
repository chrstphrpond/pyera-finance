package com.pyera.app.ui.auth;

import com.pyera.app.data.biometric.BiometricAuthManager;
import com.pyera.app.data.repository.AuthRepository;
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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<AuthRepository> repositoryProvider;

  private final Provider<BiometricAuthManager> biometricAuthManagerProvider;

  public AuthViewModel_Factory(Provider<AuthRepository> repositoryProvider,
      Provider<BiometricAuthManager> biometricAuthManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.biometricAuthManagerProvider = biometricAuthManagerProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(repositoryProvider.get(), biometricAuthManagerProvider.get());
  }

  public static AuthViewModel_Factory create(Provider<AuthRepository> repositoryProvider,
      Provider<BiometricAuthManager> biometricAuthManagerProvider) {
    return new AuthViewModel_Factory(repositoryProvider, biometricAuthManagerProvider);
  }

  public static AuthViewModel newInstance(AuthRepository repository,
      BiometricAuthManager biometricAuthManager) {
    return new AuthViewModel(repository, biometricAuthManager);
  }
}
