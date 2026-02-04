package com.pyera.app.data.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.pyera.app.BuildConfig
import com.pyera.app.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthHelper @Inject constructor() {
    
    private var googleSignInClient: GoogleSignInClient? = null
    
    companion object {
        private const val TAG = "GoogleSignIn"
    }
    
    /**
     * Initialize Google Sign-In client
     * Call this in your Activity's onCreate
     */
    fun initialize(context: Context) {
        val webClientId = context.getString(R.string.default_web_client_id)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Initializing Google Sign-In with Web Client ID: $webClientId")
        }
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, gso)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Google Sign-In client initialized successfully")
        }
    }
    
    /**
     * Get the sign-in intent to launch
     */
    fun getSignInIntent(): Intent? {
        val intent = googleSignInClient?.signInIntent
        if (intent == null) {
            Log.e(TAG, "Sign-in intent is null! GoogleSignInClient not initialized?")
        } else {
            Log.d(TAG, "Sign-in intent created successfully")
        }
        return intent
    }
    
    /**
     * Handle the sign-in result
     * Returns a Result containing the ID token or the error
     */
    fun handleSignInResult(data: Intent?): Result<String> {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Handling sign-in result")
        }
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Sign-in task received, attempting to get account")
            }
            val account = task.getResult(ApiException::class.java)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Account retrieved: ${account?.displayName}, email: ${account?.email}")
            }
            if (account?.idToken != null) {
                Log.d(TAG, "ID Token obtained successfully")
                Result.success(account.idToken!!)
            } else {
                Log.e(TAG, "ID Token is null!")
                Result.failure(Exception("ID Token is null"))
            }
        } catch (e: ApiException) {
            // Include status code in error message for debugging
            val errorMessage = when (e.statusCode) {
                10 -> "Developer error (10): SHA-1 or package name mismatch. Check Firebase console."
                12500 -> "OAuth consent screen not configured (12500). Follow GOOGLE_SIGNIN_FIX_GUIDE.md"
                12501 -> "Sign-in cancelled by user (12501)"
                12502 -> "Sign-in already in progress (12502)"
                else -> "Sign-in failed: ${e.statusCode} - ${e.message}"
            }
            Log.e(TAG, errorMessage, e)
            Result.failure(Exception(errorMessage))
        }
    }
    
    /**
     * Sign out the current user
     */
    fun signOut(context: Context, onComplete: () -> Unit = {}) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val client = GoogleSignIn.getClient(context, gso)
        client.signOut().addOnCompleteListener {
            onComplete()
        }
    }
    
    /**
     * Check if user is already signed in with Google
     */
    fun isSignedIn(context: Context): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null
    }
}
