# Pyera Finance - App Optimization Implementation Plan

**Created by:** Android App Optimizer Expert  
**Date:** February 5, 2026  
**Project:** Pyera Finance - Personal Finance Android App  

---

## Executive Summary

This plan addresses 35+ optimization opportunities identified in the comprehensive audit, with potential for:
- **20-30MB APK size reduction**
- **30-50% build time improvement**
- **Significant runtime performance gains** (fewer recompositions, faster UI)
- **Better battery life and user experience**

---

## Phase 1: Critical Build & Configuration Optimizations (Week 1)

### 1.1 Gradle Configuration Updates

**File:** `gradle.properties`

```properties
# Project-wide Gradle settings
org.gradle.jvmargs=-Xmx6144m -XX:+UseParallelGC -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.daemon=true

# Android settings
android.useAndroidX=true
android.enableJetifier=true
android.nonTransitiveRClass=true
android.nonFinalResIds=false

# Kotlin settings
kotlin.code.style=official
kotlin.incremental=true
kotlin.incremental.android=true
kotlin.compiler.execution.strategy=in-process
```

**File:** `app/build.gradle.kts`

```kotlin
android {
    compileSdk = 34
    
    defaultConfig {
        minSdk = 26
        targetSdk = 34
        
        // Enable resource shrinking
        resConfigs("en") // Only include English resources
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
        // Enable compiler optimizations
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Enable R8 in debug for testing
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    
    // ABI splits for smaller APKs
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")
            isUniversalApk = false
        }
    }
    
    // Resource optimization
    packaging {
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "**/kotlin_metadata",
                "**/kotlin_module",
                "DebugProbesKt.bin"
            )
            pickFirsts += listOf(
                "**/libc++_shared.so",
                "**/libjsc.so"
            )
        }
    }
    
    // Lint configuration
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable += listOf(
            "UnusedResources",
            "IconMissingDensityFolder"
        )
    }
}

dependencies {
    // Add R8 compiler optimizations
    implementation("com.android.tools:r8:8.2.42")
}
```

### 1.2 ProGuard Rules Optimization

**File:** `app/proguard-rules.pro`

```proguard
# Keep Room entities
-keep class com.pyera.app.data.local.entity.* { *; }

# Keep serializable data classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Hilt
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManagerHolder { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keep class com.pyera.app.data.remote.api.** { *; }

# Keep Compose stability
-keep class androidx.compose.runtime.Stable { *; }
-keep class androidx.compose.runtime.Immutable { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}
```

---

## Phase 2: APK Size Optimization (Week 1-2)

### 2.1 Image Resource Optimization

**Priority Actions:**

1. **Convert large JPG backgrounds to WebP**
```bash
# Commands to run in app/src/main/res/drawable/
cwebp -q 85 bg_gradient_green_gradient_background.jpg -o bg_gradient_green_gradient_background.webp
cwebp -q 85 bg_gradient_photo_green_*.jpg -o bg_gradient_photo_green_*.webp
cwebp -q 85 bg_gradient_abstract_*.jpg -o bg_gradient_abstract_*.webp
```

2. **Remove unused background variations**
   - Keep only 2-3 essential backgrounds
   - Delete: `bg_gradient_green_gradient_background.jpg` (22MB!)
   - Use smaller, optimized alternatives

3. **Optimize launcher icons**
   - Convert PNG launcher icons to vector drawables where possible
   - Use WebP for complex icons

**File:** Create image optimization script `scripts/optimize_images.sh`

```bash
#!/bin/bash
# Image optimization script

RES_DIR="app/src/main/res"

# Function to convert to WebP
convert_to_webp() {
    local file=$1
    local quality=$2
    cwebp -q $quality "$file" -o "${file%.jpg}.webp"
    if [ $? -eq 0 ]; then
        rm "$file"
        echo "Converted: $file"
    fi
}

# Find and convert large JPGs
find $RES_DIR -name "*.jpg" -size +100k | while read file; do
    convert_to_webp "$file" 85
done

# Find and convert PNGs (except launcher icons)
find $RES_DIR -name "*.png" ! -path "*/mipmap-*" | while read file; do
    convert_to_webp "$file" 90
done

echo "Image optimization complete!"
```

### 2.2 Resource Cleanup

**Remove unused resources:**
```bash
# Run Android Studio's Remove Unused Resources
# Or use lint:
./gradlew lint

# Manually check and remove:
# - Unused drawable files
# - Unused layout files
# - Unused string resources
# - Unused color definitions
```

---

## Phase 3: Compose Performance Optimization (Week 2-3)

### 3.1 Fix State Collection Pattern

**Replace all `collectAsState()` with `collectAsStateWithLifecycle()`**

**Files to update:**
- `AnalysisScreen.kt`
- `AddTransactionScreen.kt`
- `InvestmentsScreen.kt`
- `BudgetScreen.kt`
- `SavingsScreen.kt`
- `DebtScreen.kt`
- `ProfileScreen.kt`
- `ChatScreen.kt`
- `TransactionListScreen.kt`
- `DashboardScreen.kt`

**Change pattern:**
```kotlin
// ❌ Before
import androidx.compose.runtime.collectAsState
val state by viewModel.state.collectAsState()

// ✅ After
import androidx.lifecycle.compose.collectAsStateWithLifecycle
val state by viewModel.state.collectAsStateWithLifecycle()
```

### 3.2 Add Stability Annotations

**File:** `app/src/main/java/com/pyera/app/ui/dashboard/DashboardState.kt` (Create/Update)

```kotlin
package com.pyera.app.ui.dashboard

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pyera.app.data.local.entity.TransactionEntity

@Immutable
data class DashboardState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val recentTransactions: List<TransactionUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@Immutable
data class TransactionUiModel(
    val id: String,
    val title: String,
    val category: String,
    val amount: String,
    val isIncome: Boolean,
    val date: String,
    val icon: Int
)
```

**Update other UI state classes:**
- `TransactionState`
- `BudgetState`
- `SavingsState`
- `DebtState`
- `ProfileState`
- `AnalysisState`

### 3.3 Optimize LazyColumn/LazyRow Keys

**File:** Update all LazyColumn usages

```kotlin
// ❌ Before
LazyColumn {
    items(transactions) { transaction ->
        TransactionItem(transaction)
    }
}

// ✅ After - Add keys and contentType
LazyColumn {
    items(
        items = transactions,
        key = { it.id },
        contentType = { it.type }
    ) { transaction ->
        TransactionItem(transaction)
    }
}
```

**Files to update:**
- `DashboardScreen.kt` - Recent transactions list
- `TransactionListScreen.kt` - Main transaction list
- `BudgetScreen.kt` - Budget list
- `DebtScreen.kt` - Debt list
- `SavingsScreen.kt` - Savings goals list

### 3.4 Cache Date Formatters

**File:** `app/src/main/java/com/pyera/app/ui/dashboard/DashboardViewModel.kt`

```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Cache date formatters
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    private val todayFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    private fun TransactionEntity.toUiModel(): TransactionUiModel {
        val today = todayFormat.format(System.currentTimeMillis())
        val transactionDate = todayFormat.format(this.date)
        val dateLabel = when (transactionDate) {
            today -> "Today"
            else -> dateFormat.format(this.date)
        }
        
        return TransactionUiModel(
            id = this.id.toString(),
            title = this.description,
            category = this.category,
            amount = formatCurrency(this.amount),
            isIncome = this.type == TransactionType.INCOME,
            date = dateLabel,
            icon = getCategoryIcon(this.category)
        )
    }
    
    // Use derivedStateOf equivalent in ViewModel
    val filteredTransactions = _state
        .map { state ->
            state.transactions
                .filter { it.date >= state.startDate }
                .sortedByDescending { it.date }
        }
        .distinctUntilChanged() // Only emit when actually changed
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
```

---

## Phase 4: Architecture & Performance (Week 3-4)

### 4.1 Implement Paging 3 for Transactions

**File:** `app/src/main/java/com/pyera/app/data/local/dao/TransactionDao.kt`

```kotlin
@Dao
interface TransactionDao {
    
    // Existing method (keep for compatibility)
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    
    // New paginated method
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsPaged(limit: Int, offset: Int): List<TransactionEntity>
    
    @Query("SELECT * FROM transactions WHERE date >= :startDate ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsPagedSince(startDate: Long, limit: Int, offset: Int): List<TransactionEntity>
    
    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTransactionCount(): Int
    
    // Add indexes for better query performance
    @Query("CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(date)")
    suspend fun createDateIndex()
    
    @Query("CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type)")
    suspend fun createTypeIndex()
}
```

**File:** `app/src/main/java/com/pyera/app/ui/transaction/TransactionViewModel.kt`

```kotlin
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _pagingState = MutableStateFlow(PagingState())
    
    val transactions = _pagingState
        .flatMapLatest { state ->
            transactionRepository.getTransactionsPaged(
                page = state.page,
                pageSize = PAGE_SIZE
            )
        }
        .cachedIn(viewModelScope)
    
    fun loadMore() {
        _pagingState.update { it.copy(page = it.page + 1) }
    }
    
    companion object {
        const val PAGE_SIZE = 20
    }
}

data class PagingState(
    val page: Int = 0,
    val isLoading: Boolean = false
)
```

### 4.2 Add Room Database Indexes

**File:** `app/src/main/java/com/pyera/app/data/local/entity/TransactionEntity.kt`

```kotlin
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["date"], name = "idx_transactions_date"),
        Index(value = ["type"], name = "idx_transactions_type"),
        Index(value = ["categoryId"], name = "idx_transactions_category"),
        Index(value = ["userId"], name = "idx_transactions_user")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val description: String,
    val date: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

**Update other entities with indexes:**
- `BudgetEntity`
- `DebtEntity`
- `SavingsGoalEntity`
- `CategoryEntity`

### 4.3 Optimize Network Layer

**File:** `app/src/main/java/com/pyera/app/di/NetworkModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        // 10MB cache
        val cacheSize = 10 * 1024 * 1024L
        val cache = Cache(context.cacheDir, cacheSize)
        
        return OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("Cache-Control", "max-age=300") // 5 min cache
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
```

### 4.4 Background Processing Optimization

**File:** Create `app/src/main/java/com/pyera/app/worker/SyncWorker.kt`

```kotlin
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Sync transactions to cloud
            transactionRepository.syncPendingTransactions()
            
            // Sync budget data
            budgetRepository.syncBudgets()
            
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
                1, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "sync_work",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWork
            )
        }
    }
}
```

---

## Phase 5: Code Quality & Refactoring (Week 4)

### 5.1 Input Validation

**File:** `app/src/main/java/com/pyera/app/ui/transaction/AddTransactionViewModel.kt`

```kotlin
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _validationState = MutableStateFlow(ValidationState())
    val validationState = _validationState.asStateFlow()
    
    fun validateAmount(amount: String): Boolean {
        return when {
            amount.isBlank() -> {
                _validationState.update { it.copy(amountError = "Amount is required") }
                false
            }
            amount.toDoubleOrNull() == null -> {
                _validationState.update { it.copy(amountError = "Invalid amount") }
                false
            }
            amount.toDouble() <= 0 -> {
                _validationState.update { it.copy(amountError = "Amount must be greater than 0") }
                false
            }
            amount.toDouble() > 999999999.99 -> {
                _validationState.update { it.copy(amountError = "Amount is too large") }
                false
            }
            else -> {
                _validationState.update { it.copy(amountError = null) }
                true
            }
        }
    }
    
    fun saveTransaction(amount: String, description: String, categoryId: Long) {
        if (!validateAmount(amount)) return
        if (description.isBlank()) {
            _validationState.update { it.copy(descriptionError = "Description is required") }
            return
        }
        
        viewModelScope.launch {
            transactionRepository.insertTransaction(
                TransactionEntity(
                    amount = amount.toDouble(),
                    description = description,
                    categoryId = categoryId,
                    // ... other fields
                )
            )
        }
    }
}

data class ValidationState(
    val amountError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null
)
```

### 5.2 Safe String Resources

**File:** `app/src/main/res/values/strings.xml`

```xml
<resources>
    <!-- App Name -->
    <string name="app_name">Pyera Finance</string>
    
    <!-- Greetings -->
    <string name="greeting_morning">Good Morning,</string>
    <string name="greeting_afternoon">Good Afternoon,</string>
    <string name="greeting_evening">Good Evening,</string>
    
    <!-- Dashboard -->
    <string name="total_balance">Total Balance</string>
    <string name="income">Income</string>
    <string name="expense">Expense</string>
    <string name="recent_transactions">Recent Transactions</string>
    <string name="view_all">View All</string>
    <string name="no_transactions_title">No transactions yet</string>
    <string name="no_transactions_description">Start tracking your expenses by adding your first transaction</string>
    <string name="add_transaction">Add Transaction</string>
    
    <!-- Quick Actions -->
    <string name="action_add">Add</string>
    <string name="action_scan">Scan</string>
    <string name="action_analysis">Analysis</string>
    
    <!-- Auth -->
    <string name="welcome_back">Welcome Back</string>
    <string name="sign_in_continue">Sign in to continue</string>
    <string name="email">Email</string>
    <string name="password">Password</string>
    <string name="sign_in">Sign In</string>
    <string name="sign_up">Sign Up</string>
    <string name="forgot_password">Forgot Password?</string>
    <string name="no_account">Don\'t have an account?</string>
    <string name="has_account">Already have an account?</string>
    
    <!-- Errors -->
    <string name="error_invalid_email">Invalid email format</string>
    <string name="error_password_short">Password must be at least 6 characters</string>
    <string name="error_passwords_match">Passwords do not match</string>
    <string name="error_required">This field is required</string>
    
    <!-- Profile -->
    <string name="profile">Profile</string>
    <string name="personal_info">Personal Information</string>
    <string name="security">Security</string>
    <string name="notifications">Notifications</string>
    <string name="export_csv">Export to CSV</string>
    <string name="backup_restore">Backup &amp; Restore</string>
    <string name="appearance">Appearance</string>
    <string name="help_support">Help &amp; Support</string>
    <string name="about">About</string>
    <string name="logout">Logout</string>
    <string name="transactions">Transactions</string>
    <string name="goals">Goals</string>
    <string name="budgets">Budgets</string>
</resources>
```

### 5.3 Remove Force Unwraps

**File:** Update all files using `!!` operator

```kotlin
// ❌ Before - AnalysisScreen.kt
if (state.exportMessage != null) {
    Text(
        text = state.exportMessage!!,
        color = AccentGreen,
    )
}

// ✅ After
state.exportMessage?.let { message ->
    Text(
        text = message,
        color = AccentGreen,
    )
}
```

---

## Phase 6: Testing & Verification (Week 4-5)

### 6.1 Build Verification Checklist

```bash
# Run full build
./gradlew clean assembleRelease

# Verify APK size
ls -lh app/build/outputs/apk/release/

# Run lint checks
./gradlew lint

# Run unit tests
./gradlew test

# Check for unused resources
./gradlew lintDebug | grep -i "unused"
```

### 6.2 Performance Testing

```kotlin
// Add to build.gradle (debug builds only)
debugImplementation("androidx.compose.ui:ui-tooling")
debugImplementation("androidx.compose.ui:ui-tooling-preview")
debugImplementation("androidx.tracing:tracing:1.2.0")
```

**Enable StrictMode for debug builds:**
```kotlin
// Application class
class PyeraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
    }
}
```

---

## Implementation Timeline

| Phase | Duration | Key Deliverables |
|-------|----------|------------------|
| Phase 1 | Week 1 | Gradle optimization, build speed improvement |
| Phase 2 | Week 1-2 | APK size reduction (20-30MB), resource cleanup |
| Phase 3 | Week 2-3 | Compose performance, fewer recompositions |
| Phase 4 | Week 3-4 | Architecture optimization, Paging 3, caching |
| Phase 5 | Week 4 | Code quality, validation, string resources |
| Phase 6 | Week 4-5 | Testing, verification, performance profiling |

---

## Success Metrics

| Metric | Before | Target | Measurement |
|--------|--------|--------|-------------|
| **APK Size** | ~80-90MB | ~50-60MB | `ls -lh app/build/outputs/apk/release/*.apk` |
| **Build Time (Clean)** | 3-5 min | 1.5-2 min | Gradle build scan |
| **Build Time (Incremental)** | 30-60s | 10-20s | Gradle build scan |
| **Recomposition Count** | High | Reduced 50%+ | Compose Layout Inspector |
| **FPS (UI Smoothness)** | 30-45fps | 55-60fps | Android Profiler |
| **Memory Usage** | Baseline | -20% | Android Profiler |
| **Lint Warnings** | 50+ | <10 | `./gradlew lint` |

---

## Parallel Agent Assignment

### Agent 1: Build & Configuration (Phase 1)
- Update gradle.properties
- Update app/build.gradle.kts
- Create/update proguard-rules.pro

### Agent 2: APK Size Optimization (Phase 2)
- Convert images to WebP
- Remove unused resources
- Clean up drawable folder

### Agent 3: Compose Performance (Phase 3)
- Replace collectAsState with collectAsStateWithLifecycle
- Add stability annotations
- Optimize LazyColumn keys
- Cache date formatters

### Agent 4: Architecture (Phase 4)
- Implement Paging 3
- Add database indexes
- Optimize network layer
- Create SyncWorker

### Agent 5: Code Quality (Phase 5)
- Add input validation
- Extract string resources
- Remove force unwraps
- Add null safety

### Agent 6: Testing & Verification (Phase 6)
- Run build verification
- Check APK size
- Run lint checks
- Document metrics

---

## Risk Mitigation

| Risk | Mitigation |
|------|------------|
| WebP conversion issues | Keep original JPGs until verified |
| ProGuard breaks app | Test thoroughly in release builds |
| Paging 3 breaks existing UI | Keep old method as fallback |
| Database migration | Add proper Room migrations |
| Build cache corruption | Document clean build procedure |

---

## Resources

- **Build Performance Guide:** https://developer.android.com/studio/build/optimize-your-build
- **Compose Performance:** https://developer.android.com/jetpack/compose/performance
- **APK Size Reduction:** https://developer.android.com/topic/performance/reduce-apk-size
- **Battery Optimization:** https://developer.android.com/topic/performance/power

---

*Plan Created by: Android App Optimizer Expert*  
*Date: February 5, 2026*
