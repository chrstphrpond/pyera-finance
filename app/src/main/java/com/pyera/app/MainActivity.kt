package com.pyera.app

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pyera.app.data.biometric.BiometricAuthManager
import com.pyera.app.data.local.LocalDataSeeder
import com.pyera.app.data.preferences.ThemeMode
import com.pyera.app.domain.repository.AuthRepository
import com.pyera.app.data.security.AppLockManager
import com.pyera.app.ui.auth.AuthState
import com.pyera.app.ui.auth.LoginScreen
import com.pyera.app.ui.auth.RegisterScreen
import com.pyera.app.ui.legal.LegalScreen
import com.pyera.app.ui.legal.PrivacyPolicyText
import com.pyera.app.ui.legal.TermsOfServiceText
import com.pyera.app.ui.main.MainScreen
import com.pyera.app.ui.navigation.Screen
import com.pyera.app.ui.onboarding.OnboardingScreen
import com.pyera.app.ui.security.AppLockScreen
import com.pyera.app.ui.theme.PyeraTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var dataSeeder: LocalDataSeeder

    @Inject
    lateinit var googleAuthHelper: com.pyera.app.data.repository.GoogleAuthHelper
    
    @Inject
    lateinit var biometricAuthManager: BiometricAuthManager
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var appLockManager: AppLockManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Prevent screenshots and screen recordings in release builds
        if (!BuildConfig.DEBUG) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        // Initialize Google Auth Client
        googleAuthHelper.initialize(this)
        
        // Register AppLockManager as lifecycle observer
        lifecycle.addObserver(appLockManager)

        setContent {
            PyeraTheme(themeMode = ThemeMode.SYSTEM) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Handle screenshot protection based on current screen
                    ScreenshotProtectionHandler(navController = navController)
                    
                    PyeraAppNavigation(
                        navController = navController,
                        dataSeeder = dataSeeder,
                        googleAuthHelper = googleAuthHelper,
                        biometricAuthManager = biometricAuthManager,
                        authRepository = authRepository,
                        appLockManager = appLockManager
                    )
                }
            }
        }

        // Customize splash screen exit animation (Android 12+)
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Create slide up animation
            val slideUp = ObjectAnimator.ofFloat(
                splashScreenView.view,
                android.view.View.TRANSLATION_Y,
                0f,
                -splashScreenView.view.height.toFloat()
            )

            slideUp.apply {
                interpolator = AnticipateInterpolator()
                duration = 300L
                doOnEnd { splashScreenView.remove() }
                start()
            }
        }
    }
}

/**
 * Routes that require screenshot protection (sensitive screens)
 */
private val SECURE_ROUTES = setOf(
    "security/setup",      // SetPinScreen
    "security/change-pin"  // ChangePinScreen
)

/**
 * Composable that handles enabling/disabling screenshot protection
 * based on the current navigation route
 */
@Composable
private fun ScreenshotProtectionHandler(navController: NavController) {
    if (!BuildConfig.DEBUG) {
        return
    }
    val context = LocalContext.current
    val activity = remember(context) {
        context as? ComponentActivity
    }
    
    // Track current route
    var currentRoute by remember { mutableStateOf<String?>(null) }
    
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            currentRoute = destination.route
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
    
    // Apply FLAG_SECURE based on current route
    LaunchedEffect(currentRoute) {
        activity?.window?.let { window ->
            val shouldSecure = currentRoute in SECURE_ROUTES
            if (shouldSecure) {
                window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}

/**
 * Extension to enable/disable screenshot protection for screens outside of navigation
 */
fun ComponentActivity.setScreenshotProtection(enabled: Boolean) {
    if (enabled) {
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}

@Composable
fun PyeraAppNavigation(
    navController: NavHostController,
    dataSeeder: LocalDataSeeder,
    googleAuthHelper: com.pyera.app.data.repository.GoogleAuthHelper,
    biometricAuthManager: BiometricAuthManager,
    authRepository: AuthRepository,
    appLockManager: AppLockManager
) {
    // Determine start destination based on onboarding and auth state
    val isOnboardingCompleted = dataSeeder.isOnboardingCompleted()
    
    val startDestination = when {
        !isOnboardingCompleted -> Screen.Onboarding.route
        else -> Screen.Login.route
    }
    
    // Observe lock state
    val isLocked by appLockManager.isLocked.collectAsState()
    
    // Show lock screen if locked
    if (isLocked) {
        // Enable screenshot protection on AppLockScreen
        val context = LocalContext.current
        DisposableEffect(Unit) {
            (context as? ComponentActivity)?.setScreenshotProtection(true)
            onDispose {
                (context as? ComponentActivity)?.setScreenshotProtection(false)
            }
        }
        
        AppLockScreen(
            onUnlockSuccess = { appLockManager.unlock() }
        )
    } else {
        NavHost(
            navController = navController, 
            startDestination = startDestination
        ) {
            // Onboarding
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onOnboardingComplete = {
                        dataSeeder.markOnboardingCompleted()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                    onSkip = {
                        dataSeeder.markOnboardingCompleted()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            
            // Auth
            composable(Screen.Login.route) {
                val loginViewModel = hiltViewModel<com.pyera.app.ui.auth.AuthViewModel>()
                val loginState by loginViewModel.uiState.collectAsState()
                
                // Seed data when login is successful
                LaunchedEffect(loginState.authState) {
                    if (loginState.authState is AuthState.Success) {
                        val userId = authRepository.currentUser?.uid ?: return@LaunchedEffect
                        dataSeeder.seedInitialData(userId)
                    }
                }
                
                LoginScreen(
                    onLoginSuccess = { 
                        navController.navigate(Screen.Main.Dashboard.route) { 
                            popUpTo(Screen.Login.route) { inclusive = true } 
                        } 
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    googleAuthHelper = googleAuthHelper,
                    biometricAuthManager = biometricAuthManager
                )
            }
            
            composable(Screen.Register.route) {
                val registerViewModel = hiltViewModel<com.pyera.app.ui.auth.AuthViewModel>()
                val registerState by registerViewModel.uiState.collectAsState()
                
                // Seed data when registration is successful
                LaunchedEffect(registerState.authState) {
                    if (registerState.authState is AuthState.Success) {
                        val userId = authRepository.currentUser?.uid ?: return@LaunchedEffect
                        dataSeeder.seedInitialData(userId)
                    }
                }
                
                RegisterScreen(
                    onRegisterSuccess = { 
                        navController.navigate(Screen.Main.Dashboard.route) { 
                            popUpTo(Screen.Register.route) { inclusive = true } 
                        } 
                    },
                    onNavigateToLogin = { navController.popBackStack() },
                    onOpenTerms = { navController.navigate(Screen.Terms.route) }
                )
            }

            composable(Screen.Terms.route) {
                LegalScreen(
                    title = "Terms of Service",
                    body = TermsOfServiceText,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Privacy.route) {
                LegalScreen(
                    title = "Privacy Policy",
                    body = PrivacyPolicyText,
                    onBack = { navController.popBackStack() }
                )
            }
            
            // Main App
            composable(Screen.Main.Dashboard.route) {
                MainScreen()
            }
        }
    }
}
