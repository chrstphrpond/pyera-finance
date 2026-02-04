package com.pyera.app

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pyera.app.data.biometric.BiometricAuthManager
import com.pyera.app.data.local.LocalDataSeeder
import com.pyera.app.data.repository.AuthRepository
import com.pyera.app.ui.auth.AuthState
import com.pyera.app.ui.auth.LoginScreen
import com.pyera.app.ui.auth.RegisterScreen
import com.pyera.app.ui.main.MainScreen
import com.pyera.app.ui.navigation.Screen
import com.pyera.app.ui.onboarding.OnboardingScreen
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Initialize Google Auth Client
        googleAuthHelper.initialize(this)

        setContent {
            PyeraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PyeraAppNavigation(
                        dataSeeder = dataSeeder,
                        googleAuthHelper = googleAuthHelper,
                        biometricAuthManager = biometricAuthManager,
                        authRepository = authRepository,
                        activity = this
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

@Composable
fun PyeraAppNavigation(
    dataSeeder: LocalDataSeeder,
    googleAuthHelper: com.pyera.app.data.repository.GoogleAuthHelper,
    biometricAuthManager: BiometricAuthManager,
    authRepository: AuthRepository,
    activity: ComponentActivity
) {
    val navController = rememberNavController()
    
    // Determine start destination based on onboarding and auth state
    // TEMP: Reset onboarding to test new UI
    dataSeeder.resetOnboarding()
    val isOnboardingCompleted = dataSeeder.isOnboardingCompleted()
    
    val startDestination = when {
        !isOnboardingCompleted -> Screen.Onboarding.route
        else -> Screen.Auth.Login.route
    }
    
    NavHost(
        navController = navController, 
        startDestination = startDestination
    ) {
        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    dataSeeder.markOnboardingCompleted()
                    navController.navigate(Screen.Auth.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onSkip = {
                    dataSeeder.markOnboardingCompleted()
                    navController.navigate(Screen.Auth.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Auth
        composable(Screen.Auth.Login.route) {
            val loginViewModel = hiltViewModel<com.pyera.app.ui.auth.AuthViewModel>()
            val loginState by loginViewModel.uiState.collectAsState()
            
            // Seed data when login is successful
            LaunchedEffect(loginState.authState) {
                if (loginState.authState is AuthState.Success) {
                    dataSeeder.seedInitialData()
                }
            }
            
            LoginScreen(
                onLoginSuccess = { 
                    navController.navigate(Screen.Main.route) { 
                        popUpTo(Screen.Auth.Login.route) { inclusive = true } 
                    } 
                },
                onNavigateToRegister = { navController.navigate(Screen.Auth.Register.route) },
                googleAuthHelper = googleAuthHelper,
                biometricAuthManager = biometricAuthManager
            )
        }
        
        composable(Screen.Auth.Register.route) {
            val registerViewModel = hiltViewModel<com.pyera.app.ui.auth.AuthViewModel>()
            val registerState by registerViewModel.uiState.collectAsState()
            
            // Seed data when registration is successful
            LaunchedEffect(registerState.authState) {
                if (registerState.authState is AuthState.Success) {
                    dataSeeder.seedInitialData()
                }
            }
            
            RegisterScreen(
                onRegisterSuccess = { 
                    navController.navigate(Screen.Main.route) { 
                        popUpTo(Screen.Auth.Register.route) { inclusive = true } 
                    } 
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        
        // Main App
        composable(Screen.Main.route) {
            MainScreen()
        }
    }
}
