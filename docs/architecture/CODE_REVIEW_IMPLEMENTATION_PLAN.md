# Pyera Finance App - Code Review Implementation Plan

**Date:** February 5, 2026  
**Status:** Critical Build Fixes Required  
**Estimated Effort:** 3-4 hours

---

## Executive Summary

The Pyera Finance App is a comprehensive Android finance application built with Jetpack Compose, Hilt DI, Room, and Firebase. The code review identified several critical compilation errors that prevent the app from building, along with architectural inconsistencies in repository interfaces.

---

## Critical Issues Summary

| Priority | Issue | Location | Impact |
|----------|-------|----------|--------|
| 🔴 HIGH | R.font references point to non-existent constants | Type.kt | Build failure |
| 🔴 HIGH | Missing getTransactionsBetweenDates method | TransactionDao | Build failure |
| 🔴 HIGH | Missing getBudgetsForPeriod method call | ProfileViewModel | Build failure |
| 🔴 HIGH | Variable scope issue | CreateBudgetScreen.kt | Build failure |
| 🟡 MEDIUM | Namespace/ApplicationId mismatch | build.gradle.kts | Potential issues |
| 🟡 MEDIUM | BudgetItem type mapping issue | BudgetViewModel | Runtime error |

---

## Phase 1: Critical Build Fixes

### 1.1 Fix Font Resource References

**File:** `app/src/main/java/com/pyera/app/ui/theme/Type.kt`

**Problem:** The file references `R.font.ibrand_regular` and `R.font.outfit_variable`, but Android generates R.font constants from XML filenames, not font filenames.

**Current XML files:**
- `ibrand.xml` → generates `R.font.ibrand`
- `outfit.xml` → generates `R.font.outfit`

**Fix:**
```kotlin
// Change from:
Font(R.font.ibrand_regular, FontWeight.Normal)
Font(R.font.outfit_variable, FontWeight.Thin)

// To:
Font(R.font.ibrand, FontWeight.Normal)
Font(R.font.outfit, FontWeight.Thin)
```

---

### 1.2 Add Missing DAO Method

**File:** `app/src/main/java/com/pyera/app/data/local/dao/TransactionDao.kt`

**Problem:** `BudgetRepositoryImpl` calls `getTransactionsBetweenDates()` but the method doesn't exist in TransactionDao.

**Fix:**
```kotlin
@Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>
```

---

### 1.3 Fix ProfileViewModel Repository Call

**File:** `app/src/main/java/com/pyera/app/ui/profile/ProfileViewModel.kt`

**Problem:** Calls `getBudgetsForPeriod(_currentPeriod.value)` but repository only has `getBudgetsByPeriod(period: BudgetPeriod, userId: String)`.

**Fix Options:**

**Option A - Add new method to BudgetRepository (Recommended):**
```kotlin
// In BudgetRepository interface:
fun getBudgetsForPeriod(period: String): Flow<List<BudgetEntity>>

// In BudgetRepositoryImpl:
override fun getBudgetsForPeriod(period: String): Flow<List<BudgetEntity>> {
    val userId = authRepository.currentUser?.uid ?: ""
    // Parse period string and determine BudgetPeriod
    return budgetDao.getActiveBudgetsForUser(userId) // or implement proper logic
}
```

**Option B - Fix ProfileViewModel to use existing method:**
```kotlin
// Remove the budgets from combine, or call a different method
```

---

### 1.4 Fix CreateBudgetScreen Scope Issue

**File:** `app/src/main/java/com/pyera/app/ui/budget/CreateBudgetScreen.kt`

**Problem:** `expenseCategories` is defined in the main composable but referenced in `CategorySelectionSection` child composable.

**Fix:** Pass `expenseCategories` as a parameter:
```kotlin
@Composable
private fun CategorySelectionSection(
    categories: List<CategoryEntity>,
    expenseCategories: List<CategoryEntity>,  // Add this
    selectedCategory: CategoryEntity?,
    onCategorySelected: (CategoryEntity) -> Unit
)
```

---

### 1.5 Fix Namespace/ApplicationId Alignment

**File:** `app/build.gradle.kts`

**Problem:** `namespace = "com.crit.pyera"` but `applicationId = "com.pyera.app"`

**Fix:**
```kotlin
namespace = "com.pyera.app"  // Align with applicationId
```

---

## Phase 2: Code Quality Fixes

### 2.1 Fix BudgetItem Mapping

**File:** `app/src/main/java/com/pyera/app/ui/budget/BudgetViewModel.kt`

**Problem:** The mapping in state creation references `it.category` which may not exist in `BudgetWithSpending`.

**Fix:** Verify `BudgetWithSpending` structure and fix mapping:
```kotlin
items = budgetsList.map { budgetWithSpending ->
    BudgetItem(
        category = budgetWithSpending.category,  // Verify this field exists
        budgetAmount = budgetWithSpending.budget.amount,
        spentAmount = budgetWithSpending.spent
    )
}
```

---

### 2.2 Clean Up Imports

**File:** `app/src/main/java/com/pyera/app/ui/auth/RegisterScreen.kt`

**Problem:** Duplicate import `import androidx.compose.animation.shrinkVertically`

**Fix:** Remove duplicate import.

---

### 2.3 Verify ErrorContainer Import

**File:** `app/src/main/java/com/pyera/app/ui/budget/BudgetListScreen.kt`

**Problem:** Import of `ErrorContainer` may be incorrect.

**Fix:** Verify correct import path or create the component.

---

## Phase 3: Repository Improvements (Optional)

### 3.1 Standardize Error Handling

Current pattern is inconsistent across repositories. Consider standardizing on `Result<T>` wrapper for all suspend functions.

### 3.2 Optimize BudgetRepositoryImpl

The `getCategorySpendingProgress` method fetches all transactions then filters in memory. Consider adding a DAO query:

```kotlin
@Query("""
    SELECT COALESCE(SUM(amount), 0.0) FROM transactions 
    WHERE categoryId = :categoryId 
    AND type = 'EXPENSE'
    AND date >= :startDate 
    AND date <= :endDate
""")
fun getTotalExpensesForCategory(
    categoryId: Int, 
    startDate: Long, 
    endDate: Long
): Flow<Double>
```

---

## Build Verification Checklist

After applying fixes, verify:

- [ ] `./gradlew clean` succeeds
- [ ] `./gradlew assembleDebug` succeeds
- [ ] All R references resolve correctly
- [ ] No unresolved repository method calls
- [ ] No type mismatches in ViewModels
- [ ] App launches without crashes

---

## GitHub Repository Setup

### Repository Structure
```
pyera-finance/
├── .gitignore
├── README.md
├── LICENSE
├── app/
│   ├── build.gradle.kts
│   └── src/...
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
```

### Initial Commit Message
```
Initial commit: Pyera Finance App

Features:
- Complete finance tracking with income/expense management
- Budget creation and monitoring with visual progress indicators
- Debt tracking (I Owe / Owed to Me)
- Savings goals with progress tracking
- Bill reminders and recurring payments
- Investment portfolio tracking
- AI-powered receipt scanning
- Smart financial insights
- Biometric authentication
- Firebase Authentication (Email/Password, Google Sign-In)
- Local data persistence with Room
- Jetpack Compose UI with Material 3
- Dark theme with neon green accent

Tech Stack:
- Kotlin
- Jetpack Compose
- Hilt DI
- Room Database
- Firebase (Auth, Firestore)
- Coroutines & Flow
- Material 3
```

---

## Files Modified Summary

| File | Changes |
|------|---------|
| Type.kt | Fix R.font references |
| TransactionDao.kt | Add getTransactionsBetweenDates |
| BudgetRepository.kt | Add getBudgetsForPeriod |
| BudgetRepositoryImpl.kt | Implement getBudgetsForPeriod |
| ProfileViewModel.kt | Update repository call |
| CreateBudgetScreen.kt | Fix scope issue |
| build.gradle.kts | Fix namespace |
| BudgetViewModel.kt | Fix BudgetItem mapping |
| RegisterScreen.kt | Remove duplicate import |

---

## Next Steps After Build Fixes

1. **Testing:** Run instrumented tests on all screens
2. **UI Polish:** Address UI/UX audit recommendations
3. **Documentation:** Add KDoc to public methods
4. **CI/CD:** Set up GitHub Actions for automated builds

---

*End of Implementation Plan*
