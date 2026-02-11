# Pyera Finance - Code Style Guide

This document outlines the coding standards and best practices for the Pyera Finance Android app.

## Table of Contents

- [General Principles](#general-principles)
- [Naming Conventions](#naming-conventions)
- [Formatting](#formatting)
- [Function Guidelines](#function-guidelines)
- [Compose Best Practices](#compose-best-practices)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)

## General Principles

- **Readability over cleverness**: Write code that is easy to understand
- **Consistency**: Follow existing patterns in the codebase
- **KISS (Keep It Simple, Stupid)**: Avoid unnecessary complexity
- **DRY (Don't Repeat Yourself)**: Extract reusable components
- **Single Responsibility**: Each class/function should do one thing well

## Naming Conventions

### Files

- **Kotlin files**: PascalCase (e.g., `TransactionRepository.kt`)
- **Resource files**: snake_case (e.g., `ic_arrow_back.xml`, `bg_card_rounded.xml`)

### Classes & Interfaces

- **Classes**: PascalCase, noun or noun phrase (e.g., `TransactionRepository`, `DashboardViewModel`)
- **Interfaces**: PascalCase, adjective or noun (e.g., `TransactionDao`, `Authenticatable`)
- **Data classes**: PascalCase, describe the data (e.g., `TransactionEntity`, `UserPreferences`)
- **Enum classes**: PascalCase, entries in UPPER_SNAKE_CASE (e.g., `TransactionType.INCOME`)

### Functions

- **Regular functions**: camelCase, verb or verb phrase (e.g., `getTransactions()`, `calculateTotal()`)
- **Composable functions**: PascalCase, noun describing the UI (e.g., `TransactionCard()`, `DashboardScreen()`)
- **Private helper functions**: camelCase with leading underscore optional (e.g., `_sortTransactions()`)
- **Test functions**: camelCase, descriptive sentence (e.g., `getTransactionsReturnsEmptyListWhenNoData()`)

### Variables

- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT`)
- **Top-level/immutable vals**: camelCase (e.g., `defaultCurrency`, `supportedLocales`)
- **Mutable variables**: camelCase (e.g., `currentBalance`, `isLoading`)
- **Private properties**: camelCase, may use leading underscore if needed
- **Boolean properties**: Prefix with `is`, `has`, `should`, `can` (e.g., `isLoading`, `hasError`)

### Compose-specific Naming

- **State holders**: `*State` or `*UiState` (e.g., `DashboardUiState`, `TransactionFormState`)
- **Events**: `*Event` (e.g., `DashboardEvent`, `TransactionEvent`)
- **ViewModels**: `*ViewModel` (e.g., `DashboardViewModel`)

## Formatting

### Indentation & Spacing

- **Indentation**: 4 spaces (no tabs)
- **Line length**: Maximum 120 characters
- **Blank lines**:
  - 1 line between class members
  - 1 line between function declarations
  - 2 lines between top-level declarations

### Imports

- Use explicit imports (no wildcard imports `*`)
- Group imports:
  1. Kotlin/Java standard library
  2. Android framework
  3. Third-party libraries
  4. Project imports
- Remove unused imports

### Braces

```kotlin
// If statements - always use braces
if (condition) {
    doSomething()
}

// When expressions
when (value) {
    is Type1 -> handleType1()
    is Type2 -> handleType2()
    else -> handleDefault()
}

// Functions
fun doSomething() {
    // implementation
}
```

### Trailing Commas

Enable trailing commas for better diffs:

```kotlin
// Good
@Composable
fun TransactionCard(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)

// Function calls
loadData(
    userId = userId,
    forceRefresh = true,
)
```

## Function Guidelines

### Function Length

- **Maximum 60 lines** per function
- **Aim for 20-30 lines** on average
- **Extract into smaller functions** if too long

### Parameter Lists

- **Maximum 6 parameters** for regular functions
- **Maximum 7 parameters** for constructors
- Use data classes or builders for many parameters
- Composable functions are exempted (decorated with `@Composable`)

```kotlin
// Good - use data class
fun updateTransaction(update: TransactionUpdate)

// Instead of
fun updateTransaction(
    id: String,
    amount: Double,
    category: String,
    date: Date,
    note: String?,
    isRecurring: Boolean,
)
```

### Return Statements

- **Maximum 2 return statements** per function
- Prefer single exit point when possible
- Use early returns for guard clauses

```kotlin
// Good - early return for guard clause
fun processTransaction(transaction: Transaction?): Result {
    if (transaction == null) return Result.Error("Null transaction")
    
    // Process transaction
    return Result.Success(processed)
}
```

### Scope Functions

Use appropriate scope functions:

| Function | Use when |
|----------|----------|
| `let` | Null checks, transform result |
| `run` | Object configuration and result |
| `with` | Multiple operations on object |
| `apply` | Object configuration, no result |
| `also` | Additional actions, side effects |

```kotlin
// Good - use let for null checks
val name = user?.let { it.firstName + " " + it.lastName } ?: "Unknown"

// Good - use apply for configuration
val client = OkHttpClient.Builder().apply {
    connectTimeout(30, TimeUnit.SECONDS)
    readTimeout(30, TimeUnit.SECONDS)
}.build()
```

## Compose Best Practices

### State Hoisting

- **Hoist state** to the lowest common ancestor
- Pass events up, pass state down
- Use ViewModel for screen-level state

```kotlin
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    DashboardContent(
        state = uiState,
        onRefresh = viewModel::refresh,
        onTransactionClick = viewModel::onTransactionClick,
    )
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onRefresh: () -> Unit,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // UI implementation
}
```

### Modifier Parameter

- **Always include Modifier parameter** in Composables
- Place as first optional parameter with default value
- Pass Modifier down to first child composable

```kotlin
@Composable
fun TransactionCard(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier, // Standard position
) {
    Card(
        modifier = modifier.clickable(onClick = onClick), // Pass down
    ) {
        // Content
    }
}
```

### Side Effects

Use appropriate side effect handlers:

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is Event.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }
    
    // Handle lifecycle-aware effects
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Skip if equal
    SideEffect {
        analytics.trackScreen("MyScreen")
    }
}
```

### Preview Annotations

```kotlin
@Preview(showBackground = true)
@Composable
private fun TransactionCardPreview() {
    PyeraTheme {
        TransactionCard(
            transaction = Transaction.sample(),
            onClick = {},
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TransactionCardDarkPreview() {
    PyeraTheme {
        TransactionCard(
            transaction = Transaction.sample(),
            onClick = {},
        )
    }
}
```

## Testing Guidelines

### Test Naming

Use descriptive sentence format:

```kotlin
@Test
fun `getTransactions returns empty list when no data`() { }

@Test
fun `getTransactions returns sorted list by date descending`() { }

@Test
fun `getTransactions throws exception when database error`() { }
```

### Test Structure (AAA Pattern)

```kotlin
@Test
fun `calculateTotal returns sum of all amounts`() = runTest {
    // Arrange
    val transactions = listOf(
        Transaction(amount = 100.0),
        Transaction(amount = 200.0),
        Transaction(amount = 300.0),
    )
    
    // Act
    val result = calculator.calculateTotal(transactions)
    
    // Assert
    assertEquals(600.0, result, 0.01)
}
```

### Flow Testing

```kotlin
@Test
fun `uiState emits loading then success`() = runTest {
    // Given
    val expectedStates = listOf(
        DashboardUiState.Loading,
        DashboardUiState.Success(transactions),
    )
    
    // When & Then
    viewModel.uiState.test {
        viewModel.loadData()
        
        assertEquals(DashboardUiState.Loading, awaitItem())
        assertEquals(DashboardUiState.Success(transactions), awaitItem())
        
        cancelAndIgnoreRemainingEvents()
    }
}
```

### Compose Testing

```kotlin
@HiltAndroidTest
class DashboardScreenTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun dashboard_displaysTotalBalance() {
        composeTestRule.setContent {
            PyeraTheme {
                DashboardScreen()
            }
        }
        
        composeTestRule
            .onNodeWithText("Total Balance")
            .assertIsDisplayed()
    }
}
```

## Documentation

### KDoc Comments

Document public APIs:

```kotlin
/**
 * Calculates the total balance for the given account.
 *
 * @param accountId The unique identifier of the account
 * @param includePending Whether to include pending transactions
 * @return The calculated balance as a [BigDecimal] with 2 decimal places
 * @throws AccountNotFoundException if the account doesn't exist
 * @throws IllegalStateException if the account is locked
 */
suspend fun calculateBalance(
    accountId: String,
    includePending: Boolean = false,
): BigDecimal
```

### Code Comments

- Explain **why**, not **what**
- Keep comments up-to-date with code changes
- Use `// TODO: ` for temporary workarounds
- Use `// FIXME: ` for known issues
- Use `// STOPSHIP: ` for critical issues before release

```kotlin
// Good - explains why
// Retry with exponential backoff to avoid overwhelming the server
delay(calculateBackoff(attempt))

// Bad - explains what (obvious from code)
// Increment the counter
counter++
```

## Static Analysis

This project uses:

- **Detekt**: Static analysis for Kotlin (configured in `detekt-config.yml`)
- **KtLint**: Code formatting (configured in project build.gradle.kts)
- **Android Lint**: Android-specific checks

Run checks locally:

```bash
# Run Detekt
./gradlew detekt

# Run KtLint check
./gradlew ktlintCheck

# Run KtLint format
./gradlew ktlintFormat

# Run all checks
./gradlew check
```

Pre-commit hooks will run these checks automatically. Commits will be blocked if any check fails.

---

*Last updated: February 2026*
