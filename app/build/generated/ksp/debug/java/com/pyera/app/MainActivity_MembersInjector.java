package com.pyera.app;

import com.pyera.app.data.biometric.BiometricAuthManager;
import com.pyera.app.data.local.LocalDataSeeder;
import com.pyera.app.data.repository.AuthRepository;
import com.pyera.app.data.repository.GoogleAuthHelper;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<LocalDataSeeder> dataSeederProvider;

  private final Provider<GoogleAuthHelper> googleAuthHelperProvider;

  private final Provider<BiometricAuthManager> biometricAuthManagerProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public MainActivity_MembersInjector(Provider<LocalDataSeeder> dataSeederProvider,
      Provider<GoogleAuthHelper> googleAuthHelperProvider,
      Provider<BiometricAuthManager> biometricAuthManagerProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.dataSeederProvider = dataSeederProvider;
    this.googleAuthHelperProvider = googleAuthHelperProvider;
    this.biometricAuthManagerProvider = biometricAuthManagerProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  public static MembersInjector<MainActivity> create(Provider<LocalDataSeeder> dataSeederProvider,
      Provider<GoogleAuthHelper> googleAuthHelperProvider,
      Provider<BiometricAuthManager> biometricAuthManagerProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new MainActivity_MembersInjector(dataSeederProvider, googleAuthHelperProvider, biometricAuthManagerProvider, authRepositoryProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectDataSeeder(instance, dataSeederProvider.get());
    injectGoogleAuthHelper(instance, googleAuthHelperProvider.get());
    injectBiometricAuthManager(instance, biometricAuthManagerProvider.get());
    injectAuthRepository(instance, authRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.pyera.app.MainActivity.dataSeeder")
  public static void injectDataSeeder(MainActivity instance, LocalDataSeeder dataSeeder) {
    instance.dataSeeder = dataSeeder;
  }

  @InjectedFieldSignature("com.pyera.app.MainActivity.googleAuthHelper")
  public static void injectGoogleAuthHelper(MainActivity instance,
      GoogleAuthHelper googleAuthHelper) {
    instance.googleAuthHelper = googleAuthHelper;
  }

  @InjectedFieldSignature("com.pyera.app.MainActivity.biometricAuthManager")
  public static void injectBiometricAuthManager(MainActivity instance,
      BiometricAuthManager biometricAuthManager) {
    instance.biometricAuthManager = biometricAuthManager;
  }

  @InjectedFieldSignature("com.pyera.app.MainActivity.authRepository")
  public static void injectAuthRepository(MainActivity instance, AuthRepository authRepository) {
    instance.authRepository = authRepository;
  }
}
