# Pyera UI/UX Implementation Plan

## Audit Summary
- **Total Issues Found**: 44+
- **High Priority**: 17 issues
- **Medium Priority**: 17 issues
- **Low Priority**: 10 issues

## Phase 1: Critical Fixes (Navigation, Colors, Auth)

### Agent 1: Navigation & Theme Fixes
**Files to modify:**
- `Screen.kt` - Restructure navigation (max 5 items)
- `PyeraBottomBar.kt` - Fix bottom nav icons and labels
- `Color.kt` - Add missing semantic colors
- `Theme.kt` - Ensure consistent color application
- `MainActivity.kt` - Fix auth flow

**Changes:**
1. Bottom Nav: Reduce to 5 items (Home, Transactions, Budget, Debt, Profile)
2. Move Analysis to top-level navigation or FAB menu
3. Fix duplicate List icons - use distinct icons
4. Add all semantic colors (success, error, warning backgrounds)
5. Restore proper auth start destination

## Phase 2: Dashboard Redesign

### Agent 2: Dashboard & Components
**Files to modify:**
- `DashboardScreen.kt` - Complete redesign
- `PyeraCards.kt` - Enhance card component
- `PyeraBottomBar.kt` - Polish navigation

**Design Changes:**
1. **Balance Card:**
   - Better gradient background
   - Currency formatting (₱)
   - Animated number display
   - Visual separation for income/expense

2. **Quick Actions:**
   - Fix Add button color (NeonYellow)
   - Add more quick actions (Scan Receipt, Transfer)
   - Better spacing

3. **Recent Transactions:**
   - Show actual recent items (not just empty state)
   - Better empty state with illustration
   - "View All" link

4. **Bottom Navigation:**
   - Better active state indicator
   - Consistent icon sizing

## Phase 3: Auth Screens Redesign

### Agent 3: Login & Register
**Files to modify:**
- `LoginScreen.kt` - Complete redesign
- `RegisterScreen.kt` - Complete redesign
- `AuthViewModel.kt` - Add password visibility toggle

**Design Changes:**
1. **Login Screen:**
   - Add app logo/branding
   - Password visibility toggle
   - "Forgot Password?" link
   - Social login placeholders
   - Better visual hierarchy
   - Keyboard handling improvements

2. **Register Screen:**
   - Similar styling to login
   - Password strength indicator
   - Terms & conditions checkbox
   - Better validation feedback

## Phase 4: Transaction & Debt Enhancement

### Agent 4: Transaction Screens
**Files to modify:**
- `AddTransactionScreen.kt` - Add date picker, improve layout
- `TransactionListScreen.kt` - Add search, filters
- `TransactionViewModel.kt` - Add refresh capability

**Changes:**
1. **Add Transaction:**
   - Date picker dialog
   - Better category grid (larger touch targets)
   - Save confirmation
   - Receipt scan integration

2. **Transaction List:**
   - Search bar
   - Filter chips (Date, Category, Amount)
   - Sort options
   - Pull-to-refresh
   - Swipe to delete
   - Empty state with CTA

### Agent 5: Debt Screen
**Files to modify:**
- `DebtScreen.kt` - Visual improvements
- `DebtViewModel.kt` - Add confirmation dialogs

**Changes:**
1. Better card layout (amount more prominent)
2. Color coding for debt type
3. Confirmation dialog before delete
4. Empty state illustration
5. Better tab styling

## Phase 5: Profile Screen Implementation

### Agent 6: Profile Screen
**Files to create/modify:**
- `ProfileScreen.kt` - New file
- `MainScreen.kt` - Add navigation

**Features:**
1. User profile header (avatar, name, email)
2. Settings menu items:
   - Account Settings
   - Notifications
   - Currency Settings
   - Export Data
   - Help & Support
   - About
   - Logout
3. App version info
4. Pyera branding

## Phase 6: Global Interactions

### Agent 7: Global UI Components
**Files to modify:**
- `MainScreen.kt` - Add SnackbarHost
- Create `ui/components/Dialogs.kt` - Confirmation dialogs
- Create `ui/components/EmptyStates.kt` - Reusable empty states
- Create `ui/components/LoadingStates.kt` - Skeleton loaders
- TransactionListScreen.kt - Add pull-to-refresh
- DebtScreen.kt - Add pull-to-refresh

**Components:**
1. **Snackbar:** Global SnackbarHost for success/error messages
2. **Confirmation Dialog:** Reusable delete confirmation
3. **Empty State:** Illustration + CTA button
4. **Pull-to-Refresh:** On all list screens
5. **Loading Skeleton:** Shimmer effect for loading states

## Implementation Order

```
Phase 1: Navigation & Theme (Foundation)
    ↓
Phase 2: Dashboard (Main screen users see)
    ↓
Phase 3: Auth (First impression)
    ↓
Phase 4 & 5: Transaction, Debt, Profile (Parallel)
    ↓
Phase 6: Global Components (Integration)
    ↓
Final: Build verification
```

## Color Palette Reference

```kotlin
// Primary
NeonYellow = Color(0xFFD4FF00)
DarkGreen = Color(0xFF0A0E0D)
SurfaceDark = Color(0xFF1A1F1D)
SurfaceElevated = Color(0xFF242927)

// Semantic
ColorSuccess = Color(0xFF9FD356)
ColorSuccessContainer = Color(0xFF1B2A12)  // New
ColorWarning = Color(0xFFFFB800)
ColorWarningContainer = Color(0xFF2A1F00)  // New
ColorError = Color(0xFFFF4D4D)
ColorErrorContainer = Color(0xFF2A0A0A)    // New
ColorInfo = Color(0xFF4D9FFF)
```

## Icon Mapping

```kotlin
// Bottom Navigation (5 items)
Home = Icons.Default.Home
Transactions = Icons.AutoMirrored.Filled.List  // Fixed
Budget = Icons.Default.PieChart  // Use outlined variant when unselected
Debt = Icons.Default.AccountBalance  // New distinct icon
Profile = Icons.Default.Person  // Changed from AccountCircle

// Analysis - Move to FAB menu or top bar
Analysis = Icons.Default.BarChart
```

## Success Metrics

- [ ] All hardcoded colors removed
- [ ] Bottom nav has max 5 items with distinct icons
- [ ] Auth screens have proper branding
- [ ] All list screens have pull-to-refresh
- [ ] All delete actions have confirmation
- [ ] Empty states have illustrations
- [ ] Profile screen fully implemented
- [ ] Build compiles without errors
