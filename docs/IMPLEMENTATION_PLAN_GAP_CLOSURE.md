# Pyera Finance - Gap Closure Implementation Plan

**Date:** February 11, 2026  
**Objective:** Address remaining feature gaps and technical debt  
**Estimated Duration:** 6-8 weeks  

---

## 📋 Executive Summary

This plan addresses the remaining feature gaps and technical debt in the Pyera Finance app after excluding Home Screen Widgets, Multi-Currency, Social Features, and Bank Integration.

| Category | Items | Effort | Priority |
|----------|-------|--------|----------|
| AI Features | 2 | 6-7 weeks | High |
| Data Insights | 2 | 5-6 weeks | Medium |
| Technical Debt | 5 | 2-3 weeks | High |

**Total Estimated Effort:** 8-10 weeks (can be parallelized to 6-8 weeks)

---

## 🎯 Scope

### Included
- Natural Language Transaction Entry (AI)
- Anomaly Detection (AI)
- Spending Comparison (Data Insights)
- Cash Flow Forecasting (Data Insights)
- Certificate Pinning (Security)
- Screenshot Protection (Security)
- Database Encryption Migration (Security)
- Comprehensive Input Validation (Quality)
- WebP Image Conversion (Performance)
- Paging 3 UI Implementation (Performance)

### Excluded (Per Requirements)
- Home Screen Widgets
- Multi-Currency Support
- Family Sharing
- Expense Splitting
- Open Banking Integration

---

## 📦 Phase 1: Technical Debt & Security Hardening (Week 1)

### 1.1 Certificate Pinning Implementation

**File:** `app/src/main/java/com/pyera/app/data/repository/ChatRepositoryImpl.kt`

**Implementation:**
```kotlin
// Update certificate pinner with actual pins from local.properties
private val certificatePinner by lazy {
    if (BuildConfig.ENABLE_CERT_PINNING) {
        CertificatePinner.Builder()
            .add("api.moonshot.cn", "sha256/${BuildConfig.CERT_PIN_1}")
            .apply {
                if (BuildConfig.CERT_PIN_2.isNotBlank()) {
                    add("api.moonshot.cn", "sha256/${BuildConfig.CERT_PIN_2}")
                }
            }
            .build()
    } else null
}

private val client = OkHttpClient.Builder()
    .apply {
        certificatePinner?.let { certificatePinner(it) }
    }
    // ... rest of config
```

**Setup Instructions for `local.properties`:**
```properties
# Get pins using: openssl s_client -servername api.moonshot.cn -connect api.moonshot.cn:443
PYERA_CERT_PIN_1=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=
PYERA_CERT_PIN_2=BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=
```

**Tasks:**
- [ ] Generate SHA-256 pins for api.moonshot.cn
- [ ] Update `ChatRepositoryImpl.kt` to use BuildConfig pins
- [ ] Add backup pin from intermediate certificate
- [ ] Test with Charles Proxy to verify pinning works

---

### 1.2 Screenshot Protection (FLAG_SECURE)

**File:** `app/src/main/java/com/pyera/app/MainActivity.kt`

**Implementation:**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Prevent screenshots and screen recording in production
    if (!BuildConfig.DEBUG) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }
    
    // ... rest of onCreate
}
```

**Tasks:**
- [ ] Add `WindowManager` import
- [ ] Add FLAG_SECURE flag for release builds
- [ ] Test screenshot prevention on physical device
- [ ] Verify screen recording shows black screen

---

### 1.3 Database Migration for SQLCipher

**Files:** `app/src/main/java/com/pyera/app/data/local/PyeraDatabase.kt`, `di/DatabaseModule.kt`

**Migration Strategy:**
```kotlin
// In DatabaseModule.kt
@Provides
@Singleton
fun provideDatabase(
    @ApplicationContext context: Context,
    securePassphraseManager: SecurePassphraseManager
): PyeraDatabase {
    val passphrase = securePassphraseManager.getOrCreatePassphrase()
    val factory = SupportFactory(passphrase)
    
    return Room.databaseBuilder(
        context,
        PyeraDatabase::class.java,
        "pyera_database_encrypted.db" // New database name
    )
    .openHelperFactory(factory)
    .addMigrations(MIGRATION_3_4_ENCRYPTED) // Custom migration
    .build()
}

// Migration from unencrypted to encrypted
val MIGRATION_3_4_ENCRYPTED = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // SQLCipher handles encryption transparently
        // No schema changes needed, just re-key
    }
}
```

**Tasks:**
- [ ] Create migration script from unencrypted to encrypted DB
- [ ] Test migration on device with existing data
- [ ] Add fallback for migration failures
- [ ] Document encryption in changelog

---

### 1.4 Comprehensive Input Validation

**File:** `app/src/main/java/com/pyera/app/util/ValidationUtils.kt`

**Implementation:**
```kotlin
object ValidationUtils {
    
    // Existing validations...
    
    fun validateTransaction(
        amount: String,
        description: String,
        categoryId: Long?
    ): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Amount validation
        when {
            amount.isBlank() -> errors.add("Amount is required")
            amount.toDoubleOrNull() == null -> errors.add("Invalid amount format")
            amount.toDouble() <= 0 -> errors.add("Amount must be greater than 0")
            amount.toDouble() > 999_999_999.99 -> errors.add("Amount exceeds maximum")
        }
        
        // Description validation
        when {
            description.isBlank() -> errors.add("Description is required")
            description.length > 500 -> errors.add("Description too long (max 500 chars)")
            description.contains(Regex("[<>\"']")) -> errors.add("Invalid characters in description")
        }
        
        // Category validation
        if (categoryId == null || categoryId <= 0) {
            errors.add("Category is required")
        }
        
        return if (errors.isEmpty()) ValidationResult.Success 
               else ValidationResult.Error(errors.joinToString(", "))
    }
    
    fun validateBudget(
        amount: Double,
        categoryId: Int?,
        alertThreshold: Int
    ): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            amount <= 0 -> errors.add("Budget amount must be greater than 0")
            amount > 999_999_999.99 -> errors.add("Budget amount too large")
        }
        
        if (categoryId == null) errors.add("Category is required")
        if (alertThreshold !in 50..95) errors.add("Alert threshold must be between 50% and 95%")
        
        return if (errors.isEmpty()) ValidationResult.Success
               else ValidationResult.Error(errors.joinToString(", "))
    }
}
```

**Tasks:**
- [ ] Expand `ValidationUtils` with comprehensive validators
- [ ] Integrate validation into all ViewModels
- [ ] Add validation to `AddTransactionViewModel`
- [ ] Add validation to `BudgetViewModel`
- [ ] Add validation to `AuthViewModel` (registration)

---

## 🤖 Phase 2: AI Features - Natural Language Entry (Weeks 2-4)

### 2.1 Natural Language Transaction Entry

**New File:** `app/src/main/java/com/pyera/app/domain/nlp/NaturalLanguageParser.kt`

**Implementation:**
```kotlin
package com.pyera.app.domain.nlp

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.pyera.app.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NaturalLanguageParser @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val categoryRepository: CategoryRepository
) {
    
    data class ParsedTransaction(
        val description: String,
        val amount: Double?,
        val categoryId: Long?,
        val type: TransactionType,
        val date: Long?, // Unix timestamp or null for today
        val confidence: Float
    )
    
    suspend fun parse(input: String): Result<ParsedTransaction> = withContext(Dispatchers.IO) {
        try {
            val categories = categoryRepository.getAllCategories().first()
            val categoryList = categories.joinToString("\n") { "${it.id}: ${it.name}" }
            
            val prompt = buildString {
                appendLine("Parse this transaction: \"$input\"")
                appendLine()
                appendLine("Available categories:")
                appendLine(categoryList)
                appendLine()
                appendLine("Return ONLY a JSON object with this structure:")
                appendLine("""{
                    "description": "merchant or description",
                    "amount": 123.45,
                    "categoryId": 1,
                    "type": "EXPENSE" or "INCOME",
                    "date": "2026-02-11" or null for today,
                    "confidence": 0.95
                }""")
                appendLine()
                appendLine("Rules:")
                appendLine("- Amount should be positive number")
                appendLine("- Type is EXPENSE unless explicitly income-related")
                appendLine("- Date format: YYYY-MM-DD or null")
                appendLine("- Confidence is 0.0-1.0 based on parsing certainty")
            }
            
            val response = generativeModel.generateContent(content { text(prompt) })
            val jsonText = response.text?.trim() ?: return@withContext Result.failure(
                Exception("Empty response from AI")
            )
            
            // Parse JSON response
            val parsed = parseJsonResponse(jsonText)
            
            if (parsed.confidence < 0.7f) {
                Result.failure(Exception("Low confidence parsing: ${parsed.confidence}"))
            } else {
                Result.success(parsed)
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseJsonResponse(json: String): ParsedTransaction {
        // Use Gson to parse
        val gson = Gson()
        val jsonObject = gson.fromJson(json, JsonObject::class.java)
        
        return ParsedTransaction(
            description = jsonObject.get("description")?.asString ?: "",
            amount = jsonObject.get("amount")?.asDouble,
            categoryId = jsonObject.get("categoryId")?.asLong,
            type = if (jsonObject.get("type")?.asString == "INCOME") 
                      TransactionType.INCOME else TransactionType.EXPENSE,
            date = jsonObject.get("date")?.asString?.let { parseDate(it) },
            confidence = jsonObject.get("confidence")?.asFloat ?: 0f
        )
    }
    
    private fun parseDate(dateStr: String): Long? {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse(dateStr)?.time
        } catch (e: Exception) {
            null
        }
    }
}
```

**New File:** `app/src/main/java/com/pyera/app/ui/transaction/NaturalLanguageInput.kt`

**Implementation:**
```kotlin
@Composable
fun NaturalLanguageTransactionInput(
    onParsed: (ParsedTransaction) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var input by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val parser = hiltViewModel<NaturalLanguageViewModel>()
    
    Column(modifier = modifier) {
        // Input field with microphone icon
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g., Coffee at Starbucks 250") },
            leadingIcon = { Icon(Icons.Default.Mic, "Voice input") },
            trailingIcon = {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    IconButton(
                        onClick = { 
                            isProcessing = true
                            parser.parse(input) { result ->
                                isProcessing = false
                                result.onSuccess(onParsed).onFailure { 
                                    onError(it.message ?: "Parse failed") 
                                }
                            }
                        },
                        enabled = input.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, "Parse")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = { /* Trigger parse */ }
            )
        )
        
        // Example chips
        Text(
            "Try: \"Salary 50000 yesterday\" or \"Grab ride 150\"",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
```

**New File:** `app/src/main/java/com/pyera/app/ui/transaction/NaturalLanguageViewModel.kt`

**Implementation:**
```kotlin
@HiltViewModel
class NaturalLanguageViewModel @Inject constructor(
    private val parser: NaturalLanguageParser,
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    fun parse(
        input: String,
        onResult: (Result<ParsedTransaction>) -> Unit
    ) {
        viewModelScope.launch {
            val result = parser.parse(input)
            onResult(result)
        }
    }
    
    fun saveParsedTransaction(
        parsed: ParsedTransaction,
        accountId: Long?
    ): Flow<Result<Long>> = flow {
        try {
            val transaction = TransactionEntity(
                amount = parsed.amount ?: 0.0,
                description = parsed.description,
                type = parsed.type,
                categoryId = parsed.categoryId ?: 0,
                accountId = accountId,
                date = parsed.date ?: System.currentTimeMillis()
            )
            
            val id = transactionRepository.insertTransaction(transaction)
            emit(Result.success(id))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
```

**Integration in AddTransactionScreen:**

```kotlin
// Add tab selector at top of AddTransactionScreen
var selectedTab by remember { mutableIntStateOf(0) }
val tabs = listOf("Form", "Natural Language")

TabRow(selectedTabIndex = selectedTab) {
    tabs.forEachIndexed { index, title ->
        Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = { Text(title) }
        )
    }
}

when (selectedTab) {
    0 -> TransactionForm(...) // Existing form
    1 -> NaturalLanguageTransactionInput(
        onParsed = { parsed ->
            // Populate form or auto-save
            viewModel.saveFromParsed(parsed)
        },
        onError = { error ->
            snackbarHostState.showSnackbar(error)
        }
    )
}
```

**Tasks:**
- [ ] Create `NaturalLanguageParser` service
- [ ] Create `NaturalLanguageInput` composable
- [ ] Create `NaturalLanguageViewModel`
- [ ] Integrate into `AddTransactionScreen` as tab
- [ ] Add voice-to-text integration
- [ ] Test with various inputs
- [ ] Add fallback to manual form for low confidence

---

### 2.2 Anomaly Detection

**New File:** `app/src/main/java/com/pyera/app/domain/anomaly/AnomalyDetector.kt`

**Implementation:**
```kotlin
@Singleton
class AnomalyDetector @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    data class Anomaly(
        val transactionId: Long,
        val type: AnomalyType,
        val severity: Severity,
        val message: String,
        val suggestedAction: String
    )
    
    enum class AnomalyType {
        UNUSUAL_AMOUNT,      // Amount significantly higher than normal
        OFF_PATTERN_TIMING,  // Transaction at unusual time
        NEW_MERCHANT,        // First time at this merchant
        DUPLICATE_CHARGE,    // Similar transaction recently
        RAPID_SPENDING       // Multiple transactions in short time
    }
    
    enum class Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    suspend fun detectAnomalies(
        transaction: TransactionEntity
    ): List<Anomaly> = withContext(Dispatchers.Default) {
        val anomalies = mutableListOf<Anomaly>()
        
        // Check unusual amount
        detectUnusualAmount(transaction)?.let { anomalies.add(it) }
        
        // Check duplicate
        detectDuplicate(transaction)?.let { anomalies.add(it) }
        
        // Check new merchant
        detectNewMerchant(transaction)?.let { anomalies.add(it) }
        
        // Check rapid spending
        detectRapidSpending(transaction)?.let { anomalies.add(it) }
        
        anomalies
    }
    
    private suspend fun detectUnusualAmount(
        transaction: TransactionEntity
    ): Anomaly? {
        // Get historical stats for this category
        val stats = transactionRepository.getCategoryStats(
            categoryId = transaction.categoryId,
            months = 3
        )
        
        val mean = stats.averageAmount
        val stdDev = stats.standardDeviation
        
        // Z-score calculation
        val zScore = (transaction.amount - mean) / stdDev
        
        return when {
            zScore > 3 -> Anomaly(
                transactionId = transaction.id,
                type = AnomalyType.UNUSUAL_AMOUNT,
                severity = Severity.HIGH,
                message = "Amount is ${zScore.toInt()}x higher than your average ${stats.categoryName} expense",
                suggestedAction = "Verify this charge is correct"
            )
            zScore > 2 -> Anomaly(
                transactionId = transaction.id,
                type = AnomalyType.UNUSUAL_AMOUNT,
                severity = Severity.MEDIUM,
                message = "Amount is unusually high for ${stats.categoryName}",
                suggestedAction = "Review transaction details"
            )
            else -> null
        }
    }
    
    private suspend fun detectDuplicate(
        transaction: TransactionEntity
    ): Anomaly? {
        val recentTransactions = transactionRepository
            .getRecentTransactions(hours = 24)
            .first()
        
        val duplicate = recentTransactions.find { recent ->
            recent.id != transaction.id &&
            kotlin.math.abs(recent.amount - transaction.amount) < 0.01 &&
            recent.description.contains(transaction.description, ignoreCase = true)
        }
        
        return if (duplicate != null) {
            Anomaly(
                transactionId = transaction.id,
                type = AnomalyType.DUPLICATE_CHARGE,
                severity = Severity.HIGH,
                message = "Similar transaction found from ${formatTime(duplicate.date)}",
                suggestedAction = "Check if this is a duplicate charge"
            )
        } else null
    }
    
    private suspend fun detectNewMerchant(
        transaction: TransactionEntity
    ): Anomaly? {
        val hasHistory = transactionRepository
            .hasMerchantHistory(transaction.description)
        
        return if (!hasHistory) {
            Anomaly(
                transactionId = transaction.id,
                type = AnomalyType.NEW_MERCHANT,
                severity = Severity.LOW,
                message = "First transaction at ${transaction.description}",
                suggestedAction = "Verify merchant name is correct"
            )
        } else null
    }
    
    private suspend fun detectRapidSpending(
        transaction: TransactionEntity
    ): Anomaly? {
        val lastHour = transactionRepository
            .getTransactionsInTimeWindow(minutes = 60)
            .first()
        
        val totalSpent = lastHour
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        
        return if (lastHour.size >= 3 && totalSpent > 5000) {
            Anomaly(
                transactionId = transaction.id,
                type = AnomalyType.RAPID_SPENDING,
                severity = Severity.MEDIUM,
                message = "${lastHour.size} transactions (₱$totalSpent) in the last hour",
                suggestedAction = "Review recent spending activity"
            )
        } else null
    }
}
```

**New File:** `app/src/main/java/com/pyera/app/ui/components/AnomalyAlert.kt`

**Implementation:**
```kotlin
@Composable
fun AnomalyAlertCard(
    anomaly: Anomaly,
    onDismiss: () -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (anomaly.severity) {
        Severity.CRITICAL -> ColorError
        Severity.HIGH -> ColorError.copy(alpha = 0.8f)
        Severity.MEDIUM -> ColorWarning
        Severity.LOW -> ColorInfo
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (anomaly.severity) {
                        Severity.CRITICAL, Severity.HIGH -> Icons.Default.Warning
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = color
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = anomaly.type.name.replace("_", " "),
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Dismiss")
                }
            }
            
            Text(
                text = anomaly.message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            TextButton(onClick = onAction) {
                Text(anomaly.suggestedAction)
            }
        }
    }
}
```

**Integration:** Run anomaly detection after transaction creation and show alert in `TransactionListScreen`.

**Tasks:**
- [ ] Create `AnomalyDetector` with statistical analysis
- [ ] Create `AnomalyAlert` UI component
- [ ] Add anomaly storage in database
- [ ] Integrate detection in `AddTransactionViewModel`
- [ ] Show anomaly alerts in transaction list
- [ ] Add "Mark as Expected" to improve model
- [ ] Add notification for critical anomalies

---

## 📊 Phase 3: Data Insights (Weeks 4-6)

### 3.1 Spending Comparison

**New File:** `app/src/main/java/com/pyera/app/ui/analysis/SpendingComparisonScreen.kt`

**Implementation:**
```kotlin
@Composable
fun SpendingComparisonScreen(
    viewModel: ComparisonViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Period selector
        PeriodSelector(
            selectedPeriod = state.period,
            onPeriodSelected = viewModel::setPeriod
        )
        
        // Comparison cards
        ComparisonCard(
            title = "Total Spending",
            currentAmount = state.currentTotal,
            previousAmount = state.previousTotal,
            currency = "₱"
        )
        
        // Category breakdown
        Text(
            "Category Comparison",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        LazyColumn {
            items(state.categoryComparisons) { comparison ->
                CategoryComparisonItem(comparison)
            }
        }
    }
}

data class CategoryComparison(
    val categoryName: String,
    val categoryIcon: String,
    val currentAmount: Double,
    val previousAmount: Double,
    val percentageChange: Double
)

@Composable
fun CategoryComparisonItem(comparison: CategoryComparison) {
    val isIncrease = comparison.percentageChange > 0
    val changeColor = if (isIncrease) ColorError else ColorSuccess
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(comparison.categoryIcon, fontSize = 24.sp)
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(comparison.categoryName, style = MaterialTheme.typography.bodyLarge)
            Text(
                "vs last period",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "₱${String.format("%,.2f", comparison.currentAmount)}",
                style = MaterialTheme.typography.bodyLarge
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isIncrease) 
                        Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = changeColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "${String.format("%.1f", kotlin.math.abs(comparison.percentageChange))}%",
                    color = changeColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
```

**Tasks:**
- [ ] Create `ComparisonViewModel`
- [ ] Create `SpendingComparisonScreen`
- [ ] Add period selection (This Month vs Last Month vs Custom)
- [ ] Implement comparison calculations
- [ ] Add chart visualization
- [ ] Integrate into navigation

---

### 3.2 Cash Flow Forecasting

**New File:** `app/src/main/java/com/pyera/app/domain/forecast/CashFlowForecaster.kt`

**Implementation:**
```kotlin
@Singleton
class CashFlowForecaster @Inject constructor(
    private val recurringRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository
) {
    data class CashFlowForecast(
        val date: Long,
        val projectedBalance: Double,
        val projectedIncome: Double,
        val projectedExpenses: Double,
        val confidence: Float // 0.0-1.0 based on data quality
    )
    
    suspend fun generateForecast(
        days: Int = 90
    ): List<CashFlowForecast> = withContext(Dispatchers.Default) {
        val forecasts = mutableListOf<CashFlowForecast>()
        val calendar = Calendar.getInstance()
        
        // Get current balance
        var runningBalance = transactionRepository.getCurrentBalance()
        
        // Get recurring transactions
        val recurring = recurringRepository.getActiveRecurringTransactions().first()
        
        // Generate daily forecasts
        repeat(days) { dayOffset ->
            calendar.add(Calendar.DAY_OF_YEAR, if (dayOffset == 0) 0 else 1)
            val date = calendar.timeInMillis
            
            // Calculate projected income/expenses for this day
            val dayIncome = recurring
                .filter { it.type == TransactionType.INCOME && isDueOnDate(it, calendar) }
                .sumOf { it.amount }
            
            val dayExpenses = recurring
                .filter { it.type == TransactionType.EXPENSE && isDueOnDate(it, calendar) }
                .sumOf { it.amount }
            
            runningBalance += dayIncome - dayExpenses
            
            forecasts.add(CashFlowForecast(
                date = date,
                projectedBalance = runningBalance,
                projectedIncome = dayIncome,
                projectedExpenses = dayExpenses,
                confidence = calculateConfidence(dayOffset)
            ))
        }
        
        forecasts
    }
    
    private fun isDueOnDate(
        recurring: RecurringTransactionEntity,
        calendar: Calendar
    ): Boolean {
        // Implementation based on frequency
        return when (recurring.frequency) {
            RecurringFrequency.DAILY -> true
            RecurringFrequency.WEEKLY -> {
                val dueCal = Calendar.getInstance().apply {
                    timeInMillis = recurring.nextDueDate
                }
                calendar.get(Calendar.DAY_OF_WEEK) == dueCal.get(Calendar.DAY_OF_WEEK)
            }
            RecurringFrequency.MONTHLY -> {
                val dueCal = Calendar.getInstance().apply {
                    timeInMillis = recurring.nextDueDate
                }
                calendar.get(Calendar.DAY_OF_MONTH) == dueCal.get(Calendar.DAY_OF_MONTH)
            }
            else -> false
        }
    }
    
    private fun calculateConfidence(daysAhead: Int): Float {
        // Confidence decreases as we project further
        return kotlin.math.max(0.3f, 1.0f - (daysAhead / 90f) * 0.7f)
    }
}
```

**New File:** `app/src/main/java/com/pyera/app/ui/analysis/CashFlowScreen.kt`

**Implementation:**
```kotlin
@Composable
fun CashFlowScreen(
    viewModel: CashFlowViewModel = hiltViewModel()
) {
    val forecasts by viewModel.forecasts.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Period selector (30/60/90 days)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(30, 60, 90).forEach { days ->
                FilterChip(
                    selected = selectedPeriod == days,
                    onClick = { viewModel.setPeriod(days) },
                    label = { Text("$days days") }
                )
            }
        }
        
        // Chart
        CashFlowChart(forecasts)
        
        // Warning card if negative balance projected
        forecasts.firstOrNull { it.projectedBalance < 0 }?.let { firstNegative ->
            AlertCard(
                title = "⚠️ Potential Overdraft",
                message = "Projected negative balance on ${formatDate(firstNegative.date)}",
                severity = AlertSeverity.WARNING
            )
        }
    }
}
```

**Tasks:**
- [ ] Create `CashFlowForecaster` service
- [ ] Create `CashFlowScreen`
- [ ] Create `CashFlowViewModel`
- [ ] Implement forecast chart
- [ ] Add low balance alerts
- [ ] Integrate into Analysis/Insights navigation

---

## 🔧 Phase 4: Performance Optimization (Week 1-6 parallel)

### 4.1 WebP Image Conversion

**Script:** `scripts/convert_to_webp.ps1` (already exists, needs execution)

```powershell
# Verify and run existing script
Get-ChildItem app\src\main\res\drawable\*.jpg | ForEach-Object {
    cwebp -q 85 $_.FullName -o ($_.BaseName + ".webp")
    if ($LASTEXITCODE -eq 0) {
        Remove-Item $_.FullName
        Write-Host "Converted: $($_.Name)"
    }
}
```

**Tasks:**
- [ ] Install cwebp tool if not available
- [ ] Backup original images
- [ ] Run conversion script
- [ ] Verify app builds and images display correctly
- [ ] Measure APK size reduction

---

### 4.2 Paging 3 UI Implementation

**Current:** DAOs have paginated queries but UI may not use them

**Update:** `app/src/main/java/com/pyera/app/ui/transaction/TransactionListScreen.kt`

**Implementation:**
```kotlin
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    
    private val pager = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { repository.getTransactionPagingSource() }
    ).flow.cachedIn(viewModelScope)
    
    val transactions: Flow<PagingData<TransactionEntity>> = pager
}

// In TransactionListScreen
val lazyPagingItems = viewModel.transactions.collectAsLazyPagingItems()

LazyColumn {
    items(
        items = lazyPagingItems,
        key = { it.id }
    ) { transaction ->
        transaction?.let {
            TransactionListItem(transaction = it)
        }
    }
    
    // Loading footer
    lazyPagingItems.apply {
        when {
            loadState.refresh is LoadState.Loading -> {
                item { FullScreenLoading() }
            }
            loadState.append is LoadState.Loading -> {
                item { LoadingFooter() }
            }
        }
    }
}
```

**Tasks:**
- [ ] Create `TransactionPagingSource`
- [ ] Update `TransactionViewModel` to use Paging 3
- [ ] Update `TransactionListScreen` with lazy paging
- [ ] Add loading states and error handling
- [ ] Test with large transaction lists

---

## 📅 Implementation Timeline

```
Week 1: Technical Debt Foundation
├── Day 1-2: Certificate Pinning + Screenshot Protection
├── Day 3-4: Database Migration + Encryption
└── Day 5-7: Input Validation + WebP Conversion

Week 2-3: Natural Language Entry
├── Day 1-3: NaturalLanguageParser service
├── Day 4-5: UI components + ViewModel
└── Day 6-7: Integration + Testing

Week 4: Anomaly Detection
├── Day 1-2: AnomalyDetector algorithms
├── Day 3-4: UI components
└── Day 5-7: Integration + Notifications

Week 5: Spending Comparison
├── Day 1-2: Comparison calculations
├── Day 3-4: UI screens
└── Day 5-7: Testing + Polish

Week 6: Cash Flow Forecasting
├── Day 1-2: Forecaster service
├── Day 3-4: Chart visualization
└── Day 5-7: Integration + Testing

Week 7-8: Paging 3 + Final Polish
├── Day 1-4: Paging 3 implementation
└── Day 5-14: Testing, bug fixes, optimization
```

---

## ✅ Success Criteria

| Item | Metric |
|------|--------|
| Certificate Pinning | MITM test with proxy fails |
| Natural Language Entry | 80%+ parse success rate on test inputs |
| Anomaly Detection | Detects 90%+ of synthetic anomalies |
| Input Validation | Zero crashes from invalid input |
| Paging 3 | Smooth scrolling with 1000+ transactions |
| APK Size | < 35 MB after WebP conversion |

---

## 🎯 Parallel Workstreams

| Workstream | Owner | Duration |
|------------|-------|----------|
| Security Hardening | Security Lead | Week 1 |
| Natural Language | AI/ML Engineer | Weeks 2-3 |
| Anomaly Detection | Backend Engineer | Week 4 |
| Data Insights | Frontend Engineer | Weeks 5-6 |
| Performance | Platform Engineer | Weeks 7-8 |
