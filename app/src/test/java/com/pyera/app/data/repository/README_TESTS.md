# Pyera Finance Repository Tests

This directory contains comprehensive unit tests for all repository implementations in the Pyera Finance Android app.

## Test Files

### 1. AccountRepositoryImplTest.kt
Tests for account management operations including:
- `createAccount()` - Success cases, validation (blank name, duplicates), default account setting, error handling
- `transferBetweenAccounts()` - Valid transfers, same account validation, amount validation, insufficient balance, account not found
- `deleteAccount()` - Success when no transactions, failure when transactions exist, error handling
- `updateAccount()` - Success, duplicate name validation
- `getAllAccounts()` - Flow emissions
- `getAccountById()` - Account retrieval
- `getTotalBalance()` - Balance calculation
- `recalculateBalance()` - Balance recalculation from transactions
- `validateAccountName()` - Name validation
- `canDeleteAccount()` - Deletion eligibility

### 2. TransactionRepositoryImplTest.kt
Tests for transaction operations including:
- `insertTransaction()` - Insert calls, exception propagation
- `updateTransaction()` - Update calls, exception propagation  
- `deleteTransaction()` - Delete calls, exception propagation
- `getAllTransactions()` - Flow emissions, empty list handling
- `getTransactionsByAccount()` - Account filtering
- `getTransactionsForExport()` - Export data retrieval
- `syncPendingTransactions()` - Firebase sync success/failure, batch operations, field mapping

### 3. BudgetRepositoryImplTest.kt
Tests for budget management including:
- `createBudget()` - Creation with timestamp
- `updateBudget()` - Update with timestamp
- `deleteBudget()` / `deleteBudgetById()` - Deletion operations
- Query operations - `getBudgetById`, `getAllBudgetsForUser`, `getActiveBudgetsForUser`, `getBudgetsByPeriod`
- Budget with spending - `getBudgetsWithSpending`, `getBudgetWithSpendingById`, `getBudgetsByStatus`
- Summary operations - `getBudgetSummary`, `getActiveBudgetCount`, `getOverBudgetCount`
- `calculatePeriodDates()` - Period calculations (DAILY, WEEKLY, MONTHLY, YEARLY)
- Budget management - `deactivateBudget`, `activateBudget`, `setBudgetForCategory`
- Spending tracking - `hasActiveBudget`, `getCategorySpendingProgress`
- `syncBudgets()` - Firebase sync operations

### 4. CategoryRepositoryImplTest.kt
Tests for category CRUD operations:
- `getAllCategories()` - Flow emissions, empty list, updates
- `insertCategory()` - Insert with different types, exception handling
- `deleteCategory()` - Deletion operations, exception handling
- `getCategoryByName()` - Lookup success/failure, case sensitivity
- Edge cases - Long names, unicode, zero ID handling

### 5. DebtRepositoryImplTest.kt
Tests for debt tracking:
- `getAllDebts()` - Flow emissions, updates
- `addDebt()` - Insert with types (PAYABLE, RECEIVABLE), exception handling
- `updateDebt()` - Update operations, paid status changes
- `deleteDebt()` - Deletion operations
- Debt tracking calculations - Total payable vs receivable
- Edge cases - Large amounts, past due dates, negative amounts

### 6. RecurringTransactionRepositoryImplTest.kt
Tests for recurring transaction processing:
- `getAllRecurring()` / `getAllRecurringOnce()` - List retrieval
- `getActiveRecurring()` - Active only filtering
- `getDueRecurring()` - Due date filtering
- `getRecurringById()` / `getRecurringByIdFlow()` - Single item retrieval
- `addRecurring()` - Creation with different frequencies
- `updateRecurring()` - Updates
- `deleteRecurring()` / `deleteRecurringById()` - Deletion
- `toggleActiveStatus()` - Activation/deactivation
- `processDueRecurring()` - Transaction creation, next due date calculation, end date handling
- `getRecurringCount()` / `getActiveRecurringCount()` - Counting
- Edge cases - All frequencies, large amounts, null end dates

## Testing Framework

### Dependencies Used
- **MockK** (1.13.8) - Mocking framework for Kotlin
- **kotlinx-coroutines-test** - Coroutine testing utilities
- **Turbine** (1.1.0) - Flow testing library
- **JUnit 4** - Test runner

### Test Patterns
All tests follow the **AAA pattern**:
1. **Arrange** - Set up mocks and test data
2. **Act** - Execute the function under test
3. **Assert** - Verify results and mock interactions

### Key Testing Approaches

#### MockK Usage
```kotlin
@MockK
private lateinit var dao: SomeDao

@Before
fun setup() {
    MockKAnnotations.init(this, relaxed = true)
}
```

#### Coroutine Testing
```kotlin
@Test
fun `test function`() = runTest {
    // Test code
}
```

#### Flow Testing with Turbine
```kotlin
repository.getFlow().test {
    val result = awaitItem()
    assertEquals(expected, result)
    awaitComplete()
}
```

#### Mock Verification
```kotlin
coVerify { dao.insert(any()) }
coVerify(exactly = 0) { dao.delete(any()) }
```

## Running Tests

### Run all tests
```bash
./gradlew test
```

### Run specific test class
```bash
./gradlew test --tests "AccountRepositoryImplTest"
```

### Run with coverage
```bash
./gradlew testDebugUnitTestCoverage
```

## Test Coverage Areas

### Success Cases
- Normal CRUD operations
- Valid data handling
- Flow emissions
- Firebase sync operations

### Failure Cases
- Database exceptions
- Validation failures
- Firebase errors
- Network issues (for sync)

### Edge Cases
- Empty/null data
- Boundary values
- Unicode/special characters
- Large numbers
- Concurrent operations

### Transaction Atomicity
- Rollback on failure
- Partial success handling
- Database transaction boundaries
