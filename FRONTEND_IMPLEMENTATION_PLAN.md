# Pyera Finance - Frontend Expert Implementation Plan

## Project Overview
**Pyera Finance** is a modern Android personal finance app built with:
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material 3
- **Architecture:** MVVM with Clean Architecture
- **Backend:** Firebase (Auth, Firestore)
- **Local DB:** Room

## Current State Analysis

### Critical Build Errors (20+ errors)
| File | Error | Priority |
|------|-------|----------|
| `Type.kt` | Unresolved R reference | 🔴 HIGH |
| `BudgetRepositoryImpl.kt` | Missing `flow` import | 🔴 HIGH |
| `ChatRepositoryImpl.kt` | BuildConfig unresolved | 🔴 HIGH |
| `BudgetProgressCard.kt` | Missing imports (clickable, Arrangement) | 🔴 HIGH |
| `BudgetScreen.kt` | State issues, type mismatches | 🔴 HIGH |
| `CreateBudgetScreen.kt` | Unresolved expenseCategories | 🔴 HIGH |
| `BudgetListScreen.kt` | ErrorContainer unresolved | 🔴 HIGH |
| `LoginScreen.kt` | Unresolved R reference | 🔴 HIGH |
| `RegisterScreen.kt` | Unresolved R reference | 🔴 HIGH |
| `WelcomeScreen.kt` | Unresolved R reference | 🔴 HIGH |
| `OnboardingScreen.kt` | Unresolved R reference | 🔴 HIGH |
| `ProfileViewModel.kt` | Combine type inference issues | 🔴 HIGH |

---

## Parallel Agent Tasks

### Agent 1: Font & Resource Fixes
**Scope:** Fix R reference issues in Type.kt and verify font resources

**Files:**
- `app/src/main/java/com/pyera/app/ui/theme/Type.kt`
- `app/src/main/res/font/` (verify font XML files exist)

**Tasks:**
1. Check if font XML files exist (ibrand.xml, outfit.xml)
2. If missing, create proper font XML files in res/font/
3. Verify R imports are correct
4. Check that namespace is properly imported

**Success Criteria:**
- [ ] Type.kt compiles without R reference errors
- [ ] Font XML files exist and are properly configured

---

### Agent 2: Repository Fixes
**Scope:** Fix missing imports and BuildConfig issues in repositories

**Files:**
- `app/src/main/java/com/pyera/app/data/repository/BudgetRepositoryImpl.kt`
- `app/src/main/java/com/pyera/app/data/repository/ChatRepositoryImpl.kt`

**Tasks:**
1. Add missing `import kotlinx.coroutines.flow.flow` to BudgetRepositoryImpl.kt (line 66)
2. Fix BuildConfig reference in ChatRepositoryImpl.kt
3. Verify all repository method signatures match interface definitions

**Code Fix for BudgetRepositoryImpl.kt:**
```kotlin
// Add this import at the top
import kotlinx.coroutines.flow.flow
```

**Success Criteria:**
- [ ] BudgetRepositoryImpl.kt compiles without errors
- [ ] ChatRepositoryImpl.kt compiles without BuildConfig errors

---

### Agent 3: Budget UI Fixes
**Scope:** Fix all Budget-related UI screens

**Files:**
- `app/src/main/java/com/pyera/app/ui/budget/BudgetScreen.kt`
- `app/src/main/java/com/pyera/app/ui/budget/BudgetListScreen.kt`
- `app/src/main/java/com/pyera/app/ui/budget/BudgetProgressCard.kt`
- `app/src/main/java/com/pyera/app/ui/budget/CreateBudgetScreen.kt`
- `app/src/main/java/com/pyera/app/ui/components/Dialogs.kt` (for ErrorContainer)

**Tasks:**
1. **BudgetProgressCard.kt:** Add missing imports:
   - `import androidx.compose.foundation.clickable`
   - `import androidx.compose.foundation.layout.Arrangement`
2. **BudgetScreen.kt:** Fix state references and type mismatches
3. **CreateBudgetScreen.kt:** Fix expenseCategories scope issue - pass as parameter
4. **BudgetListScreen.kt:** Create or import ErrorContainer component
5. Create `ErrorContainer` component in `ui/components/Dialogs.kt` if missing

**Code Fix for CreateBudgetScreen.kt:**
```kotlin
// Change CategorySelectionSection call to pass expenseCategories
CategorySelectionSection(
    categories = categories,
    expenseCategories = expenseCategories,  // Add this parameter
    selectedCategory = selectedCategory,
    onCategorySelected = { selectedCategory = it }
)
```

**Success Criteria:**
- [ ] All Budget screens compile without errors
- [ ] ErrorContainer component exists and works
- [ ] CreateBudgetScreen has proper parameter passing

---

### Agent 4: Auth & Welcome Screen Fixes
**Scope:** Fix R reference issues in Auth and Welcome screens

**Files:**
- `app/src/main/java/com/pyera/app/ui/auth/LoginScreen.kt`
- `app/src/main/java/com/pyera/app/ui/auth/RegisterScreen.kt`
- `app/src/main/java/com/pyera/app/ui/welcome/WelcomeScreen.kt`
- `app/src/main/java/com/pyera/app/ui/onboarding/OnboardingScreen.kt`
- `app/src/main/java/com/pyera/app/data/repository/GoogleAuthHelper.kt`

**Tasks:**
1. Check if R import exists: `import com.pyera.app.R`
2. Verify drawable resources exist that are referenced (R.drawable.*)
3. Check if GoogleAuthHelper has proper R import
4. Verify all string resources exist

**Common Fix Pattern:**
```kotlin
// Ensure this import exists at the top of each file
import com.pyera.app.R
```

**Success Criteria:**
- [ ] LoginScreen.kt compiles
- [ ] RegisterScreen.kt compiles
- [ ] WelcomeScreen.kt compiles
- [ ] OnboardingScreen.kt compiles
- [ ] GoogleAuthHelper.kt compiles

---

### Agent 5: Profile & Global Component Fixes
**Scope:** Fix ProfileViewModel and create missing components

**Files:**
- `app/src/main/java/com/pyera/app/ui/profile/ProfileViewModel.kt`
- `app/src/main/java/com/pyera/app/ui/components/Dialogs.kt`
- `app/src/main/java/com/pyera/app/ui/components/EmptyStates.kt`
- `app/src/main/java/com/pyera/app/ui/components/LoadingStates.kt`

**Tasks:**
1. **ProfileViewModel.kt:** Fix combine type inference by explicitly specifying types
2. Create `ErrorContainer` component if missing
3. Create `EmptyStates` component if missing
4. Create `LoadingStates` component if missing

**Code Fix for ProfileViewModel.kt:**
```kotlin
// Fix the combine by specifying explicit types
val state: StateFlow<ProfileState> = combine(
    transactionRepository.getAllTransactions(),
    savingsRepository.getAllSavingsGoals(),
    budgetRepository.getActiveBudgetsForUser(authRepository.currentUser?.uid ?: ""),
    _notificationsEnabled
) { transactions: List<Transaction>, savingsGoals: List<SavingsGoal>, activeBudgets: List<BudgetEntity>, notifications: Boolean ->
    // ... rest of the mapping
}.stateIn(...)
```

**ErrorContainer Component:**
```kotlin
@Composable
fun ErrorContainer(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = ColorError,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = ColorError,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            PyeraPrimaryButton(
                text = "Retry",
                onClick = onRetry
            )
        }
    }
}
```

**Success Criteria:**
- [ ] ProfileViewModel.kt compiles
- [ ] ErrorContainer component created
- [ ] EmptyStates component created
- [ ] LoadingStates component created

---

## Implementation Order

```
Phase 1: Critical Fixes (Parallel Execution)
├── Agent 1: Font & Resource Fixes
├── Agent 2: Repository Fixes
├── Agent 3: Budget UI Fixes
├── Agent 4: Auth & Welcome Fixes
└── Agent 5: Profile & Global Components

Phase 2: Build Verification
└── Run ./gradlew assembleDebug

Phase 3: UI/UX Implementation (Next Sprint)
└── Refer to ui-ux-implementation-plan.md
```

---

## Build Verification Checklist

After all agents complete:
- [ ] Run `./gradlew clean`
- [ ] Run `./gradlew assembleDebug`
- [ ] Verify no compilation errors
- [ ] Verify all R references resolve
- [ ] Verify all imports are correct
- [ ] Check for any remaining unresolved references

---

## Color Palette Reference

```kotlin
// Primary Colors
val NeonYellow = Color(0xFFD4FF00)
val DarkGreen = Color(0xFF0A0E0D)
val SurfaceDark = Color(0xFF1A1F1D)
val SurfaceElevated = Color(0xFF242927)

// Semantic Colors
val ColorSuccess = Color(0xFF9FD356)
val ColorSuccessContainer = Color(0xFF1B2A12)
val ColorWarning = Color(0xFFFFB800)
val ColorWarningContainer = Color(0xFF2A1F00)
val ColorError = Color(0xFFFF4D4D)
val ColorErrorContainer = Color(0xFF2A0A0A)
val ColorInfo = Color(0xFF4D9FFF)
```

---

## Resources

- **Project Root:** `/Users/macbookairm2/Dev/pyera-finance`
- **App Source:** `app/src/main/java/com/pyera/app/`
- **Resources:** `app/src/main/res/`
- **Build File:** `app/build.gradle.kts`

---

*Generated by Frontend Expert Android Agent*
*Date: February 5, 2026*
