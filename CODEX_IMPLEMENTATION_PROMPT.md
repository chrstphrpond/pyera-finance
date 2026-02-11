# GPT Codex Implementation Prompt

## 🎯 Mission

Implement the Pyera Finance Gap Closure Plan from `docs/IMPLEMENTATION_PLAN_GAP_CLOSURE.md`. This is a comprehensive implementation task requiring careful attention to the existing codebase patterns and architecture.

**START BY LOADING THE SKILL:**
```
@skill pyera-finance
```

---

## 📁 Project Structure Overview

```
app/src/main/java/com/pyera/app/
├── data/
│   ├── local/
│   │   ├── dao/              # Database DAOs
│   │   ├── entity/           # Room entities
│   │   └── PyeraDatabase.kt
│   ├── repository/           # Repository implementations
│   ├── security/             # Security utilities
│   └── preferences/          # SharedPreferences
├── domain/
│   ├── model/                # Domain models
│   ├── repository/           # Repository interfaces
│   ├── smart/                # AI/ML use cases
│   └── analysis/             # Analysis utilities
├── ui/
│   ├── components/           # Reusable UI components
│   ├── transaction/          # Transaction screens
│   ├── budget/               # Budget screens
│   ├── analysis/             # Analysis screens
│   ├── insights/             # Insights screens
│   └── theme/                # Design system
├── di/                       # Hilt modules
└── worker/                   # WorkManager workers
```

---

## 📋 Implementation Checklist

### Phase 1: Technical Debt (MUST COMPLETE FIRST)

#### 1.1 Certificate Pinning
**Files to modify:**
- `app/src/main/java/com/pyera/app/data/repository/ChatRepositoryImpl.kt`

**Requirements:**
- Read certificate pins from `BuildConfig.CERT_PIN_1` and `BuildConfig.CERT_PIN_2`
- Only enable pinning if `BuildConfig.ENABLE_CERT_PINNING` is true
- Add pins to OkHttpClient using `CertificatePinner`
- Target domain: `api.moonshot.cn`

**Pattern to follow:**
```kotlin
private val certificatePinner by lazy {
    if (BuildConfig.ENABLE_CERT_PINNING && BuildConfig.CERT_PIN_1.isNotBlank()) {
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
```

#### 1.2 Screenshot Protection
**Files to modify:**
- `app/src/main/java/com/pyera/app/MainActivity.kt`

**Requirements:**
- Add `FLAG_SECURE` to window in `onCreate()`
- Only apply in release builds (`!BuildConfig.DEBUG`)
- Import `android.view.WindowManager`

#### 1.3 Database Encryption Migration
**Files to modify:**
- `app/src/main/java/com/pyera/app/di/DatabaseModule.kt`
- `app/src/main/java/com/pyera/app/data/local/PyeraDatabase.kt`

**Requirements:**
- Use existing `SecurePassphraseManager` to get passphrase
- Use `SupportFactory` from SQLCipher
- Change database name to "pyera_database_encrypted.db"
- Add migration from version 3 to 4
- Keep existing migrations (MIGRATION_1_2, MIGRATION_2_3)

**Key classes:**
- `net.sqlcipher.database.SupportFactory`
- `SecurePassphraseManager.getOrCreatePassphrase()`

#### 1.4 Input Validation
**Files to modify:**
- `app/src/main/java/com/pyera/app/util/ValidationUtils.kt` (enhance)
- `app/src/main/java/com/pyera/app/ui/transaction/AddTransactionViewModel.kt`
- `app/src/main/java/com/pyera/app/ui/budget/BudgetViewModel.kt`

**Requirements:**
- Add `validateTransaction()` method
- Add `validateBudget()` method
- Validate amounts (positive, max limit)
- Validate descriptions (max length, no HTML)
- Validate category selection
- Return `ValidationResult` sealed class

### Phase 2: Natural Language Entry

#### 2.1 NaturalLanguageParser Service
**New file:** `app/src/main/java/com/pyera/app/domain/nlp/NaturalLanguageParser.kt`

**Requirements:**
- Use Gemini AI (`GenerativeModel`) for parsing
- Inject `CategoryRepository` to get available categories
- Create `ParsedTransaction` data class
- Implement `parse(input: String): Result<ParsedTransaction>`
- Build prompt with categories list
- Parse JSON response
- Return failure if confidence < 0.7
- Handle errors gracefully

**Dependencies to inject:**
```kotlin
@Inject constructor(
    private val generativeModel: GenerativeModel,
    private val categoryRepository: CategoryRepository
)
```

**Prompt template:**
```
Parse this transaction: "$input"

Available categories:
[category list]

Return ONLY a JSON object with this structure:
{
    "description": "merchant or description",
    "amount": 123.45,
    "categoryId": 1,
    "type": "EXPENSE" or "INCOME",
    "date": "2026-02-11" or null for today,
    "confidence": 0.95
}
```

#### 2.2 NaturalLanguageViewModel
**New file:** `app/src/main/java/com/pyera/app/ui/transaction/NaturalLanguageViewModel.kt`

**Requirements:**
- Inject `NaturalLanguageParser` and `TransactionRepository`
- Create `parse(input, onResult)` method
- Create `saveParsedTransaction(parsed, accountId)` method
- Return `Flow<Result<Long>>` for save operation
- Handle errors and emit results

#### 2.3 NaturalLanguageInput Composable
**New file:** `app/src/main/java/com/pyera/app/ui/transaction/NaturalLanguageInput.kt`

**Requirements:**
- Create `NaturalLanguageTransactionInput` composable
- Use `OutlinedTextField` with mic icon
- Show loading indicator when processing
- Show example text below input
- Call parser on send action
- Handle success/error callbacks
- Use Material 3 components
- Match app theme (use Color.kt values)

**Parameters:**
```kotlin
fun NaturalLanguageTransactionInput(
    onParsed: (ParsedTransaction) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

#### 2.4 Integration
**File:** `app/src/main/java/com/pyera/app/ui/transaction/AddTransactionScreen.kt`

**Requirements:**
- Add tab row with "Form" and "Natural Language" tabs
- Show form in tab 0
- Show `NaturalLanguageTransactionInput` in tab 1
- On successful parse, either:
  - Auto-save and navigate back, OR
  - Populate form fields and switch to Form tab
- Show snackbar on error

### Phase 3: Anomaly Detection

#### 3.1 AnomalyDetector Service
**New file:** `app/src/main/java/com/pyera/app/domain/anomaly/AnomalyDetector.kt`

**Requirements:**
- Create `Anomaly` data class with id, type, severity, message, suggestedAction
- Create `AnomalyType` enum (UNUSUAL_AMOUNT, DUPLICATE_CHARGE, NEW_MERCHANT, RAPID_SPENDING)
- Create `Severity` enum (LOW, MEDIUM, HIGH, CRITICAL)
- Inject `TransactionRepository`
- Implement `detectAnomalies(transaction): List<Anomaly>`

**Detection algorithms:**
1. **Unusual Amount:** Calculate Z-score from category history (3 months)
   - Z > 3: HIGH severity
   - Z > 2: MEDIUM severity
2. **Duplicate Charge:** Check for same amount + similar description within 24 hours
3. **New Merchant:** Check if merchant name exists in history
4. **Rapid Spending:** Check for 3+ transactions in 1 hour with total > 5000

#### 3.2 AnomalyAlert Composable
**New file:** `app/src/main/java/com/pyera/app/ui/components/AnomalyAlert.kt`

**Requirements:**
- Create `AnomalyAlertCard` composable
- Use `Card` with border color based on severity
- Show warning/info icon based on severity
- Show anomaly type as title
- Show message
- Show suggested action as text button
- Support dismiss and action callbacks

**Colors by severity:**
- CRITICAL/HIGH: ColorError
- MEDIUM: ColorWarning
- LOW: ColorInfo

#### 3.3 Integration
**Files:**
- `app/src/main/java/com/pyera/app/ui/transaction/AddTransactionViewModel.kt`
- `app/src/main/java/com/pyera/app/ui/transaction/TransactionListScreen.kt`

**Requirements:**
- Run anomaly detection after successful transaction creation
- Store detected anomalies (add `AnomalyEntity` if needed)
- Show anomaly alerts in transaction list
- Add "Mark as Expected" action to dismiss
- Add "Review" action to open transaction detail

### Phase 4: Spending Comparison

#### 4.1 ComparisonViewModel
**New file:** `app/src/main/java/com/pyera/app/ui/analysis/ComparisonViewModel.kt`

**Requirements:**
- Create `ComparisonState` data class
- Support periods: This Month, Last Month, Custom
- Calculate total spending comparison
- Calculate per-category comparisons
- Show percentage change with up/down arrows
- Use `TransactionRepository` for queries

#### 4.2 SpendingComparisonScreen
**New file:** `app/src/main/java/com/pyera/app/ui/analysis/SpendingComparisonScreen.kt`

**Requirements:**
- Create `SpendingComparisonScreen` composable
- Add period selector (chips or dropdown)
- Show total comparison card (current vs previous)
- Show category comparison list
- Each item: icon, name, current amount, % change indicator
- Green for decrease (good), Red for increase (bad)
- Use existing `PyeraCard` component

#### 4.3 Integration
**File:** `app/src/main/java/com/pyera/app/ui/navigation/Screen.kt`

**Requirements:**
- Add new route: `object SpendingComparison : Screen("analysis/comparison")`
- Add to navigation graph in MainScreen.kt
- Add entry point from Analysis or Insights screen

### Phase 5: Cash Flow Forecasting

#### 5.1 CashFlowForecaster Service
**New file:** `app/src/main/java/com/pyera/app/domain/forecast/CashFlowForecaster.kt`

**Requirements:**
- Create `CashFlowForecast` data class
- Inject `RecurringTransactionRepository` and `TransactionRepository`
- Implement `generateForecast(days): List<CashFlowForecast>`
- Start with current balance
- Add recurring income/expenses based on frequency
- Calculate confidence (decreases over time)
- Support DAILY, WEEKLY, BIWEEKLY, MONTHLY frequencies

#### 5.2 CashFlowViewModel
**New file:** `app/src/main/java/com/pyera/app/ui/analysis/CashFlowViewModel.kt`

**Requirements:**
- Support periods: 30, 60, 90 days
- Generate forecast on init and period change
- Expose forecasts as StateFlow
- Detect negative balance projections
- Show warning if overdraft projected

#### 5.3 CashFlowScreen
**New file:** `app/src/main/java/com/pyera/app/ui/analysis/CashFlowScreen.kt`

**Requirements:**
- Create `CashFlowScreen` composable
- Add period selector chips (30/60/90 days)
- Show line chart of projected balance over time
- Show income/expense breakdown
- Show alert card if negative balance projected
- Use Vico charts (already in dependencies)

**Vico chart example:**
```kotlin
// Use com.patrykandpatrick.vico.compose
Chart(
    chart = lineChart(),
    model = entryModelOf(*forecasts.map { it.projectedBalance }.toFloatArray()),
    // ... other params
)
```

### Phase 6: Performance Optimization

#### 6.1 WebP Conversion
**Script:** `scripts/convert_to_webp.ps1` (enhance if needed)

**Requirements:**
- Find all .jpg/.png in `app/src/main/res/drawable/`
- Convert to WebP with quality 85
- Remove original files after successful conversion
- Skip launcher icons (mipmap folders)
- Keep vector drawables as-is

**Manual fallback if script fails:**
- Use Android Studio's "Convert to WebP" feature
- Right-click on images → Convert to WebP

#### 6.2 Paging 3 Implementation
**New files:**
- `app/src/main/java/com/pyera/app/data/local/paging/TransactionPagingSource.kt`

**Files to modify:**
- `app/src/main/java/com/pyera/app/ui/transaction/TransactionViewModel.kt`
- `app/src/main/java/com/pyera/app/ui/transaction/TransactionListScreen.kt`

**Requirements:**
- Create `TransactionPagingSource` extending `PagingSource<Int, TransactionEntity>`
- Use existing paginated DAO methods
- Return `LoadResult.Page` with prev/next keys
- In ViewModel: create `Pager` with `PagingConfig(pageSize = 20)`
- In Screen: use `collectAsLazyPagingItems()`
- Use `items()` extension from `androidx.paging.compose`
- Show loading/footer states

**Dependencies:** Check if `androidx.paging:paging-compose` is in build.gradle, add if missing.

---

## 🎨 UI/UX Guidelines

### Colors
Use theme colors from `app/src/main/java/com/pyera/app/ui/theme/Color.kt`:
- Primary: `ColorTokens.Primary500`
- Success: `ColorSuccess`
- Error: `ColorError`
- Warning: `ColorWarning`
- Background: `BackgroundPrimary`
- Surface: `SurfacePrimary`
- Text: `TextPrimary`, `TextSecondary`

### Typography
Use Material 3 typography:
- `MaterialTheme.typography.headlineLarge` for screen titles
- `MaterialTheme.typography.titleLarge` for card titles
- `MaterialTheme.typography.bodyLarge` for primary text
- `MaterialTheme.typography.bodyMedium` for secondary text
- `MaterialTheme.typography.labelLarge` for buttons

### Components
- Use `PyeraCard` for cards (already exists)
- Use `PyeraButton` for buttons (already exists)
- Use `PyeraTextField` for inputs (already exists)
- Follow existing spacing (16.dp screen padding)

---

## 🔧 Technical Patterns

### Dependency Injection (Hilt)
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel()

@Singleton
class MyService @Inject constructor(
    private val dependency: Dependency
)
```

### StateFlow Pattern
```kotlin
private val _state = MutableStateFlow(MyState())
val state: StateFlow<MyState> = _state.asStateFlow()

fun updateSomething() {
    viewModelScope.launch {
        _state.update { it.copy(loading = true) }
        // ... async work
        _state.update { it.copy(loading = false, data = result) }
    }
}
```

### Repository Pattern
```kotlin
// Domain interface
interface MyRepository {
    suspend fun getData(): Flow<List<Data>>
}

// Data implementation
class MyRepositoryImpl @Inject constructor(
    private val dao: MyDao
) : MyRepository {
    override suspend fun getData(): Flow<List<Data>> = dao.getAll()
}
```

### Error Handling
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

// Usage
try {
    val data = repository.fetch()
    Result.Success(data)
} catch (e: Exception) {
    Result.Error(e.message ?: "Unknown error")
}
```

---

## ✅ Testing Checklist

Before marking each feature complete:

- [ ] Feature compiles without errors
- [ ] Feature works on emulator
- [ ] Feature handles edge cases (empty input, network errors)
- [ ] Feature follows app theme
- [ ] Feature is accessible (content descriptions)
- [ ] Feature doesn't break existing functionality

---

## 🚀 Execution Order

1. **ALWAYS START WITH:** Phase 1 (Technical Debt)
   - Certificate Pinning
   - Screenshot Protection
   - Database Encryption
   - Input Validation

2. **THEN IMPLEMENT:** Phase 2 (Natural Language)
   - NaturalLanguageParser
   - NaturalLanguageViewModel
   - NaturalLanguageInput
   - Integration

3. **THEN:** Phase 3 (Anomaly Detection)
   - AnomalyDetector
   - AnomalyAlert
   - Integration

4. **THEN:** Phase 4 (Spending Comparison)
   - ComparisonViewModel
   - SpendingComparisonScreen
   - Navigation

5. **THEN:** Phase 5 (Cash Flow)
   - CashFlowForecaster
   - CashFlowViewModel
   - CashFlowScreen

6. **FINALLY:** Phase 6 (Performance)
   - WebP Conversion
   - Paging 3

---

## 📚 Reference Files

### Existing Similar Implementations
- `ChatRepositoryImpl.kt` - AI integration pattern
- `PredictiveBudgetUseCase.kt` - AI use case pattern
- `BudgetViewModel.kt` - ViewModel pattern
- `AddTransactionScreen.kt` - Screen with tabs pattern
- `InsightsScreen.kt` - Analysis screen pattern
- `SecurityChecker.kt` - Security utility pattern
- `ValidationUtils.kt` - Validation pattern

### Key Dependencies
```kotlin
// AI
implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

// Paging
implementation("androidx.paging:paging-runtime-ktx:3.2.1")
implementation("androidx.paging:paging-compose:3.2.1")

// Charts
implementation("com.patrykandpatrick.vico:compose:1.14.0")

// Security
implementation("net.zetetic:android-database-sqlcipher:4.5.4")
```

---

## ⚠️ Common Pitfalls

1. **Don't forget imports** - especially Compose-related imports
2. **Use existing theme colors** - don't hardcode colors
3. **Handle nulls safely** - use `?.let` or Elvis operator
4. **Check for existing utilities** - don't duplicate `ValidationUtils` logic
5. **Follow existing patterns** - look at similar files first
6. **Test with data** - empty states, loading states, error states
7. **Use proper coroutine dispatchers** - IO for database/network, Main for UI

---

## 📝 Notes

- The app uses **Material 3** (Material You) - use `androidx.compose.material3`
- The app uses **Hilt** for dependency injection
- The app uses **Room** for database
- The app uses **Coroutines + Flow** for async operations
- The app supports **dark/light theme** - test both
- The app targets **Android API 34**, minimum **API 26**

---

**END OF PROMPT**

Begin implementation by reading the skill file and then proceeding with Phase 1.
