package com.pyera.app.di

import android.content.Context
import com.pyera.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Network module providing HTTP client and caching configuration.
 * Optimized for performance with proper caching and timeout settings.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Cache size: 10MB
     */
    private const val CACHE_SIZE = 10L * 1024L * 1024L // 10 MB
    
    /**
     * Cache directory name
     */
    private const val CACHE_DIRECTORY = "http_cache"
    
    /**
     * Default cache duration: 5 minutes
     */
    private const val CACHE_MAX_AGE_SECONDS = 300
    
    /**
     * Connect timeout: 30 seconds
     */
    private const val CONNECT_TIMEOUT_SECONDS = 30L
    
    /**
     * Read timeout: 30 seconds
     */
    private const val READ_TIMEOUT_SECONDS = 30L
    
    /**
     * Write timeout: 30 seconds
     */
    private const val WRITE_TIMEOUT_SECONDS = 30L
    
    /**
     * Pinned host for certificate pinning
     */
    private const val PINNED_HOST = "api.moonshot.cn"

    /**
     * Provides OkHttp cache instance
     */
    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, CACHE_DIRECTORY)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return Cache(cacheDir, CACHE_SIZE)
    }

    /**
     * Provides HTTP logging interceptor for debug builds
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /**
     * Provides OkHttpClient with caching, timeouts, and logging configured
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val request = chain.request()
                val newRequest = request.newBuilder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .apply {
                        // Add cache control for GET requests
                        if (request.method == "GET") {
                            header("Cache-Control", "public, max-age=$CACHE_MAX_AGE_SECONDS")
                        }
                    }
                    .build()
                chain.proceed(newRequest)
            }
            .addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())
                // Add cache headers to responses
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=$CACHE_MAX_AGE_SECONDS")
                    .build()
            }
            .addInterceptor(loggingInterceptor)

        val certificatePins = listOf(
            BuildConfig.CERT_PIN_1,
            BuildConfig.CERT_PIN_2
        )
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { pin ->
                if (pin.startsWith("sha256/")) pin else "sha256/$pin"
            }

        if (BuildConfig.ENABLE_CERT_PINNING && certificatePins.isNotEmpty()) {
            val pinnerBuilder = CertificatePinner.Builder()
            certificatePins.forEach { pin ->
                pinnerBuilder.add(PINNED_HOST, pin)
            }
            builder.certificatePinner(pinnerBuilder.build())
        }

        return builder.build()
    }

    /**
     * Provides Retrofit instance for API calls
     * Note: Base URL should be configured based on your backend
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.pyera.com/") // Replace with actual API base URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
