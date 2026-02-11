# Pyera Finance Android App - UI Audit Report

**Date:** February 10, 2026  
**Auditor:** AI Code Reviewer  
**App Version:** Current codebase analysis

---

## Executive Summary

The Pyera Finance app uses a **"Nocturne Ember" design system** - a dark-themed UI with warm ember accents. The app has a generally consistent design language but exhibits some inconsistencies between older and newer screens, legacy color usage, and varying component patterns.

### Overall Grade: **B+**
- ✅ Strong color system and typography hierarchy
- ✅ Good use of Material 3 components
- ⚠️ Inconsistent card components across screens
- ⚠️ Legacy color aliases still in use
- ⚠️ Inconsistent padding/spacing values

---

## 1. Design System Overview

### 1.1 Color Palette

| Role | Color Name | Hex Value | Usage |
|------|------------|-----------|-------|
| **Primary Accent** | PrimaryAccent | `#FFF29D65` | Buttons, highlights, accent elements |
| **Background Primary** | BackgroundPrimary | `#FF0E1217` | Main screen backgrounds |
| **Background Secondary** | BackgroundSecondary | `#FF121824` | Card backgrounds |
| **Surface Primary** | SurfacePrimary | `#FF131923` | Cards, elevated surfaces |
| **Surface Secondary** | SurfaceSecondary | `#FF18202C` | Input fields, secondary cards |
| **Surface Elevated** | SurfaceElevated | `#FF1F2836` | Dialogs, elevated cards |
| **Text Primary** | TextPrimary | `#FFF6F1E7` | Main text, headings |
| **Text Secondary** | TextSecondary | `#FFC9C2B6` | Subtitles, secondary text |
| **Text Tertiary** | TextTertiary | `#FFAAA397` | Hints, disabled text |
| **Text Muted** | TextMuted | `#FF7F7A71` | Placeholder text |
| **Border** | ColorBorder | `#FF242C38` | Card borders, dividers |
| **Success** | ColorSuccess | `#FF4CCB7C` | Income, positive values |
| **Error** | ColorError | `#FFF07A6A` | Expenses, errors, warnings |
| **Warning** | ColorWarning | `#FFF2C15C` | Debt, alerts |

### 1.2 Card Accent Colors (Jewel Tones)
- CardAccentMint: `#FF7FD1B9` (Stats, success indicators)
- CardAccentPink: `#FFF3A1B5` (Budgets, feminine categories)
- CardAccentBlue: `#FF7AA7E9` (Transactions, neutral)
- CardAccentOrange: `#FFF5B36D` (Insights, warm highlights)
- CardAccentPurple: `#FFB7A1F5` (Scan, special features)
- CardAccentTeal: `#FF4FB6B0` (Analysis, cool highlights)

### 1.3 Typography System

| Style | Font Family | Size | Weight | Usage |
|-------|-------------|------|--------|-------|
| **Display Large** | Ibrand | 52sp | Normal | Hero numbers |
| **Display Medium** | Ibrand | 40sp | Normal | Large balances |
| **Display Small** | Ibrand | 32sp | Normal | Dashboard totals |
| **Headline Large** | Ibrand | 28sp | Normal | Screen titles |
| **Headline Medium** | Ibrand | 24sp | Normal | Section headers |
| **Headline Small** | Ibrand | 20sp | Normal | Card titles |
| **Title Large** | Outfit | 20sp | SemiBold | Card headers |
| **Title Medium** | Outfit | 16sp | Medium | Subsection titles |
| **Title Small** | Outfit | 14sp | Medium | Small headers |
| **Body Large** | Outfit | 15sp | Normal | Primary content |
| **Body Medium** | Outfit | 13sp | Normal | Secondary content |
| **Body Small** | Outfit | 12sp | Normal | Captions |
| **Label Large** | Outfit | 13sp | Medium | Button text |
| **Label Medium** | Outfit | 11sp | Medium | Tags, chips |
| **Label Small** | Outfit | 10sp | Medium | Overlines |

### 1.4 Spacing System

| Token | Value | Usage |
|-------|-------|-------|
| XXSmall | 2.dp | Minimal gaps |
| XSmall | 4.dp | Tight spacing |
| Small | 8.dp | Default item spacing |
| Medium | 12.dp | Component padding |
| Large | 16.dp | Section spacing |
| XLarge | 20.dp | Card padding |
| XXLarge | 24.dp | Screen padding |
| XXXLarge | 32.dp | Major section gaps |
| Huge | 48.dp | Hero spacing |

**Component-specific:**
- CardPadding: 20.dp
- ScreenPadding: 20.dp
- DialogPadding: 24.dp
- ListItemSpacing: 8.dp
- SectionSpacing: 24.dp

### 1.5 Border Radius System

| Token | Value | Usage |
|-------|-------|-------|
| sm | 6.dp | Small buttons, chips |
| md | 10.dp | List items |
| lg | 14.dp | Action cards |
| xl | 18.dp | Standard cards |
| xxl | 22.dp | Large cards |
| xxxl | 26.dp | Hero cards |
| Button | 14.dp | All buttons |
| Card | 18.dp | Default cards |
| Input | 12.dp | Text fields |
| Dialog | 20.dp | Dialogs, bottom sheets |
| Chip | 10.dp | Filter chips |
| BottomBar | 28.dp | Navigation bar |

---

## 2. Screen-by-Screen Analysis

### 2.1 Welcome Screen
**File:** `ui/welcome/WelcomeScreen.kt`

| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ✅ Good | Uses `AccentGreen` (legacy), `DeepBackground`, `TextSecondary` |
| Typography | ⚠️ Inconsistent | Uses hardcoded font sizes (32sp, 28sp, 16sp) |
| Layout | ✅ Good | Clean vertical layout with feature cards |
| Spacing | ⚠️ Inconsistent | Uses hardcoded dp values (24.dp, 16.dp, 12.dp) |
| Components | ✅ Good | Custom FeatureCard with emoji icons |

**Issues:**
- Uses legacy `AccentGreen` instead of `PrimaryAccent`
- Hardcoded typography values instead of MaterialTheme
- Background uses `pyeraBackground(forceDark = true)`

---

### 2.2 Authentication Screens

#### LoginScreen (`ui/auth/LoginScreen.kt`)
| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses both legacy (`DarkGreen`, `SurfaceDark`) and new colors |
| Typography | ✅ Good | Uses string resources and MaterialTheme |
| Layout | ✅ Good | Well-structured form card |
| Components | ✅ Good | Uses PyeraAuthTextField, PyeraAuthButton |

**Issues:**
- Background gradient uses legacy color names
- Form card uses hardcoded `Color.White` instead of theme-aware colors

#### RegisterScreen (`ui/auth/RegisterScreen.kt`)
| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Same issues as LoginScreen |
| Validation UI | ✅ Good | Good error handling with password strength indicator |
| Layout | ✅ Good | Consistent with LoginScreen |

---

### 2.3 Dashboard Screen
**File:** `ui/dashboard/DashboardScreen.kt`

| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ✅ Excellent | Uses new color system (`PrimaryAccent`, `SurfaceSecondary`, card accents) |
| Typography | ✅ Good | Uses MaterialTheme.typography with some custom modifications |
| Layout | ✅ Excellent | Well-organized with clear hierarchy |
| Spacing | ✅ Good | Uses Spacing constants where available |
| Components | ✅ Excellent | Uses ModernCard, custom components |

**Key Components:**
- `MainBalanceCard`: Large hero card with gradient
- `QuickStatsRowModern`: Three stat cards with different accent colors
- `QuickActionsGrid`: Grid of action buttons
- `RecentTransactionsSectionModern`: Transaction list with empty state

**Patterns:**
- Uses `ModernCard` consistently
- Uses `Radius.xxl` (22.dp) for main card
- Uses accent color borders (`accentColor.copy(alpha = 0.2f)`)
- Uses gradient backgrounds on cards

---

### 2.4 Transaction Screens

#### TransactionListScreen (`ui/transaction/TransactionListScreen.kt`)
| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses `NeonYellow` (legacy) in swipe refresh |
| Layout | ✅ Good | Clean list with date headers |
| Components | ✅ Good | Uses SearchBar, FilterChips |
| Empty States | ✅ Good | Multiple empty states for different scenarios |

#### AddTransactionScreen (`ui/transaction/AddTransactionScreen.kt`)
| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses `AccentGreen` (legacy) extensively |
| Typography | ⚠️ Inconsistent | Hardcoded font sizes (32.sp) |
| Layout | ✅ Good | Clear form layout with sections |
| Components | ⚠️ Mixed | Mix of Material3 and custom components |

**Issues:**
- Uses `AccentGreen` instead of `PrimaryAccent`
- Uses `SurfaceDark` (legacy) in some places
- Hardcoded typography values
- Inconsistent button styling (some use AccentGreen directly)

---

### 2.5 Budget Screens

#### BudgetScreen (`ui/budget/BudgetScreen.kt`)
| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses `AccentGreen`, `CardBackground` (legacy) |
| Components | ⚠️ Mixed | Uses `PyeraCard` but also Material Card |
| Layout | ✅ Good | Clean list with progress indicators |

#### BudgetListScreen (`ui/budget/BudgetListScreen.kt`)
| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses legacy colors |
| Layout | ✅ Good | Good summary cards at top |
| Components | ⚠️ Mixed | Uses custom FilterChip implementation |

#### BudgetDetailScreen (`ui/budget/BudgetDetailScreen.kt`)
| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses legacy aliases |
| Layout | ✅ Good | Well-organized detail view |
| Components | ✅ Good | Uses PyeraCard consistently |

---

### 2.6 Account Screens

#### AccountsScreen (`ui/account/AccountsScreen.kt`)
| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses `AccentGreen`, `SurfaceDark`, `SurfaceElevated` |
| Layout | ✅ Good | Clean list with FAB |
| Components | ⚠️ Mixed | Mix of Material Card and custom styles |

#### AccountDetailScreen (`ui/account/AccountDetailScreen.kt`)
| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses legacy colors |
| Layout | ✅ Good | Good header card with transaction list |

#### AddAccountScreen (`ui/account/AddAccountScreen.kt`)
| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses legacy colors |
| Components | ✅ Good | Good icon and color pickers |

#### TransferScreen (`ui/account/TransferScreen.kt`)
| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses legacy colors |
| Layout | ✅ Good | Clear form layout |

---

### 2.7 Debt Screen
**File:** `ui/debt/DebtScreen.kt`

| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ✅ Good | Uses semantic colors (`ColorSuccess`, `ColorError`) |
| Layout | ✅ Excellent | Well-designed with tabs and summary |
| Components | ✅ Good | Uses `PyeraCard`, custom tab row |
| Animations | ✅ Good | Celebration animation when marking paid |

**Patterns:**
- Uses `SuccessContainer` and `ErrorContainer` for card backgrounds
- Custom tab row with indicator dots
- Good use of semantic colors for debt type

---

### 2.8 Savings Screen
**File:** `ui/savings/SavingsScreen.kt`

| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses `AccentGreen`, `CardBackground` (legacy) |
| Layout | ✅ Good | Clean list with progress bars |
| Components | ✅ Good | Uses `PyeraCard` |

---

### 2.9 Investments Screen
**File:** `ui/investments/InvestmentsScreen.kt`

| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses legacy colors and `PositiveChange`/`NegativeChange` |
| Layout | ✅ Good | Portfolio summary with asset list |
| Components | ✅ Good | Uses `PyeraCard` |

---

### 2.10 Bills Screen
**File:** `ui/bills/BillsScreen.kt`

| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses legacy colors |
| Layout | ✅ Good | Simple list with action buttons |
| Components | ✅ Good | Uses `PyeraCard` |

---

### 2.11 Analysis Screen
**File:** `ui/analysis/AnalysisScreen.kt`

| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses `GlassOverlay` (legacy) |
| Typography | ⚠️ Inconsistent | Hardcoded font sizes (24.sp, 28.sp) |
| Layout | ⚠️ Basic | Very basic layout, needs enhancement |
| Components | ⚠️ Basic | Uses basic Card components |

---

### 2.12 Insights Screen
**File:** `ui/insights/InsightsScreen.kt`

| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses `NeonYellow` (legacy) |
| Layout | ✅ Good | Well-organized with multiple sections |
| Components | ✅ Good | Uses Spacing constants |
| Architecture | ✅ Good | Modular section components |

---

### 2.13 Chat Screen
**File:** `ui/chat/ChatScreen.kt`

| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ✅ Good | Uses `AccentGreen`, `CardBackground` appropriately |
| Layout | ✅ Good | Standard chat layout |
| Components | ✅ Good | Message bubbles with proper styling |

---

### 2.14 Profile Screen
**File:** `ui/profile/ProfileScreen.kt`

| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses `NeonYellow` (legacy) |
| Layout | ✅ Good | Well-organized settings sections |
| Components | ✅ Good | Uses `PyeraCard`, `PyeraButton` |
| Spacing | ✅ Good | Uses Spacing constants |

---

### 2.15 Settings Screens

#### ThemeSettingsScreen (`ui/settings/ThemeSettingsScreen.kt`)
| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses `NeonYellow` (legacy) |
| Layout | ✅ Good | Clean theme selector with preview |
| Components | ✅ Good | Uses Material3 components |

#### SecuritySettingsScreen (`ui/security/SecuritySettingsScreen.kt`)
| Aspect | Status | Notes |
|--------|--------|-------|
| Color Usage | ⚠️ Mixed | Uses `NeonYellow` (legacy) |
| Layout | ✅ Good | Well-organized toggle items |
| Components | ✅ Good | Uses Material3 Switch |

---

## 3. Component Library Analysis

### 3.1 Card Components

| Component | Location | Usage Count | Status |
|-----------|----------|-------------|--------|
| `ModernCard` | `components/ModernCards.kt` | High in Dashboard | ✅ **RECOMMENDED** - Newest, most flexible |
| `PyeraCard` | `components/PyeraCards.kt` | Medium | ✅ Good - Consistent but older |
| `Card` (Material3) | Native | Medium | ⚠️ Avoid - Use themed wrappers |
| `AccentCard` | `components/ModernCards.kt` | Low | ✅ Good - For highlighted content |
| `GlassCard` | `components/ModernCards.kt` | Low | ✅ Good - For overlay effects |
| `StatCard` | `components/ModernCards.kt` | Low | ✅ Good - For dashboard stats |

**Recommendation:** Standardize on `ModernCard` for all new screens.

### 3.2 Button Components

| Component | Location | Usage | Status |
|-----------|----------|-------|--------|
| `PyeraButton` | `components/PyeraButton.kt` | Medium | ✅ **RECOMMENDED** - Proper theming |
| `PyeraAuthButton` | `components/PyeraAuthButton.kt` | Auth screens | ✅ Good - For auth flows |
| `Button` (Material3) | Native | High | ⚠️ Avoid - Inconsistent styling |
| `TextButton` | Native | Medium | ⚠️ OK - But should use themed version |
| `OutlinedButton` | Native | Low | ⚠️ Avoid - Use themed version |

### 3.3 Input Components

| Component | Location | Usage | Status |
|-----------|----------|-------|--------|
| `PyeraAuthTextField` | `components/PyeraAuthTextField.kt` | Auth screens | ✅ Good - For auth flows |
| `PyeraTextField` | `components/PyeraTextField.kt` | Low | ⚠️ Unused? |
| `OutlinedTextField` | Native | High | ⚠️ Inconsistent styling across screens |
| `BasicTextField` | Native | Medium | ⚠️ Inconsistent styling |

---

## 4. Identified Issues

### 4.1 Critical Issues

1. **Legacy Color Usage**
   - Many screens still use `AccentGreen`, `NeonYellow`, `DarkGreen`, `SurfaceDark` etc.
   - These are legacy aliases that should be migrated to new names
   - **Impact:** Inconsistent theming, potential issues with light mode

2. **Inconsistent Card Components**
   - Three different card implementations in use
   - Different corner radii, borders, and elevation values
   - **Impact:** Visual inconsistency across screens

### 4.2 Major Issues

3. **Hardcoded Typography**
   - Many screens use hardcoded font sizes (24.sp, 32.sp) instead of MaterialTheme
   - Inconsistent text styling across screens
   - **Impact:** Difficult to maintain, inconsistent hierarchy

4. **Inconsistent Padding/Spacing**
   - Mix of hardcoded values (16.dp, 24.dp) and Spacing constants
   - Some screens use Spacing.ScreenPadding, others don't
   - **Impact:** Uneven visual rhythm

5. **Mixed Button Styling**
   - Some buttons use `AccentGreen`, others use `PrimaryAccent`
   - Different corner radius values
   - **Impact:** Inconsistent CTAs

### 4.3 Minor Issues

6. **Hardcoded Colors**
   - `Color.White` used directly in some auth screens
   - `Color.Black` used in some places
   - **Impact:** Breaks theming for light mode

7. **Inconsistent Empty States**
   - Different empty state patterns across screens
   - Some use custom components, others inline
   - **Impact:** Inconsistent UX

8. **Legacy Background Modifier**
   - `pyeraBackground()` uses gradient that doesn't match theme
   - **Impact:** Subtle visual inconsistency

---

## 5. Accessibility Analysis

### 5.1 Contrast Ratios

| Combination | Ratio | WCAG AA | Status |
|-------------|-------|---------|--------|
| TextPrimary on BackgroundPrimary | ~15:1 | ✅ Pass | Good |
| TextSecondary on BackgroundPrimary | ~10:1 | ✅ Pass | Good |
| PrimaryAccent on BackgroundPrimary | ~8:1 | ✅ Pass | Good |
| ColorSuccess on BackgroundPrimary | ~7:1 | ✅ Pass | Good |
| ColorError on BackgroundPrimary | ~7:1 | ✅ Pass | Good |
| TextMuted on BackgroundPrimary | ~4.5:1 | ✅ Pass | Borderline |

**Overall:** Color contrast is generally good for the dark theme.

### 5.2 Accessibility Issues

1. **Touch Targets**
   - Some icon buttons may be smaller than 48x48dp
   - Filter chips in some screens are small

2. **Content Descriptions**
   - Generally good usage of `contentDescription`
   - Some decorative icons could use `null` description

3. **Text Scaling**
   - Uses sp units consistently
   - Some layouts may break with very large text

---

## 6. Recommendations

### 6.1 Immediate Actions (High Priority)

1. **Create Migration Guide**
   ```kotlin
   // OLD (Legacy) → NEW
   AccentGreen → PrimaryAccent
   NeonYellow → PrimaryAccent
   DarkGreen → BackgroundPrimary
   SurfaceDark → SurfaceSecondary
   CardBackground → SurfaceSecondary
   BrightSnow → TextPrimary
   AmericanSilver → TextSecondary
   ```

2. **Standardize on ModernCard**
   - Update all screens to use `ModernCard` instead of `PyeraCard` or Material `Card`
   - Define clear usage guidelines for each card variant

3. **Fix Typography**
   - Replace all hardcoded font sizes with MaterialTheme.typography
   - Create custom text styles if needed for specific use cases

### 6.2 Short-term Actions (Medium Priority)

4. **Standardize Spacing**
   - Audit all screens for hardcoded padding values
   - Replace with Spacing constants

5. **Fix Button Styling**
   - Ensure all buttons use `PyeraButton` or themed variants
   - Consistent corner radius (14.dp)

6. **Update Background Modifier**
   - Fix `pyeraBackground()` to use theme colors properly
   - Support both dark and light themes

### 6.3 Long-term Actions (Low Priority)

7. **Component Library**
   - Document all components with usage examples
   - Create a component showcase screen

8. **Empty States**
   - Standardize empty state patterns
   - Create reusable EmptyState components

9. **Animations**
   - Add consistent enter/exit animations
   - Standardize transition durations

---

## 7. Screen Checklist

| Screen | Colors | Typography | Spacing | Components | Overall |
|--------|--------|------------|---------|------------|---------|
| WelcomeScreen | ⚠️ | ⚠️ | ⚠️ | ✅ | C+ |
| LoginScreen | ⚠️ | ✅ | ✅ | ✅ | B |
| RegisterScreen | ⚠️ | ✅ | ✅ | ✅ | B |
| DashboardScreen | ✅ | ✅ | ✅ | ✅ | A |
| TransactionListScreen | ⚠️ | ✅ | ✅ | ✅ | B |
| AddTransactionScreen | ⚠️ | ⚠️ | ✅ | ⚠️ | C+ |
| BudgetScreen | ⚠️ | ✅ | ✅ | ⚠️ | B- |
| BudgetListScreen | ⚠️ | ✅ | ✅ | ⚠️ | B- |
| BudgetDetailScreen | ⚠️ | ✅ | ✅ | ✅ | B |
| AccountsScreen | ⚠️ | ✅ | ✅ | ⚠️ | B- |
| AccountDetailScreen | ⚠️ | ✅ | ✅ | ⚠️ | B- |
| AddAccountScreen | ⚠️ | ✅ | ✅ | ✅ | B |
| TransferScreen | ⚠️ | ✅ | ✅ | ✅ | B |
| DebtScreen | ✅ | ✅ | ✅ | ✅ | A |
| SavingsScreen | ⚠️ | ✅ | ✅ | ✅ | B |
| InvestmentsScreen | ⚠️ | ✅ | ✅ | ✅ | B |
| BillsScreen | ⚠️ | ✅ | ✅ | ✅ | B |
| AnalysisScreen | ⚠️ | ⚠️ | ⚠️ | ⚠️ | D+ |
| InsightsScreen | ⚠️ | ✅ | ✅ | ✅ | B |
| ChatScreen | ✅ | ✅ | ✅ | ✅ | A |
| ProfileScreen | ⚠️ | ✅ | ✅ | ✅ | B |
| ThemeSettingsScreen | ⚠️ | ✅ | ✅ | ✅ | B |
| SecuritySettingsScreen | ⚠️ | ✅ | ✅ | ✅ | B |

---

## 8. Appendix

### 8.1 Files Analyzed

- `ui/theme/Color.kt` - 132 lines
- `ui/theme/Type.kt` - 144 lines
- `ui/theme/Spacing.kt` - 50 lines
- `ui/theme/Theme.kt` - 134 lines
- `ui/dashboard/DashboardScreen.kt` - 747 lines
- `ui/transaction/TransactionListScreen.kt` - 390 lines
- `ui/transaction/AddTransactionScreen.kt` - 1000+ lines
- `ui/budget/BudgetScreen.kt` - 287 lines
- `ui/budget/BudgetListScreen.kt` - 626 lines
- `ui/budget/BudgetDetailScreen.kt` - 578 lines
- `ui/account/AccountsScreen.kt` - 333 lines
- `ui/account/AccountDetailScreen.kt` - 425 lines
- `ui/account/AddAccountScreen.kt` - 458 lines
- `ui/account/TransferScreen.kt` - 696 lines
- `ui/debt/DebtScreen.kt` - 998 lines
- `ui/savings/SavingsScreen.kt` - 377 lines
- `ui/investments/InvestmentsScreen.kt` - 348 lines
- `ui/bills/BillsScreen.kt` - 266 lines
- `ui/analysis/AnalysisScreen.kt` - 132 lines
- `ui/insights/InsightsScreen.kt` - 678 lines
- `ui/chat/ChatScreen.kt` - 214 lines
- `ui/profile/ProfileScreen.kt` - 401 lines
- `ui/auth/LoginScreen.kt` - 624 lines
- `ui/auth/RegisterScreen.kt` - 499 lines
- `ui/settings/ThemeSettingsScreen.kt` - 320 lines
- `ui/security/SecuritySettingsScreen.kt` - 402 lines
- `ui/welcome/WelcomeScreen.kt` - 303 lines
- `ui/components/ModernCards.kt` - 342 lines
- `ui/components/PyeraCards.kt` - 114 lines
- `ui/components/PyeraButton.kt` - 93 lines
- `ui/util/Modifiers.kt` - 74 lines

### 8.2 Legacy Color Aliases to Migrate

```kotlin
// In Color.kt, lines 105-131:
val NeonYellow = PrimaryAccent
val NeonYellowDark = PrimaryAccentDark
val NeonYellowLight = PrimaryAccentLight
val DarkGreen = BackgroundPrimary
val SurfaceDark = SurfacePrimary
val CardBackground = SurfaceSecondary
val DeepBackground = BackgroundPrimary
val AccentGreen = PrimaryAccent
val AccentGreenDim = PrimaryAccentDark
val BrightSnow = TextPrimary
val AmericanSilver = TextSecondary
val Gunmetal = SurfaceSecondary
val Orange = CardAccentOrange
val PaleViolet = CardAccentPurple
val CardBorder = ColorBorder
val GlassOverlay = OverlayHover
val GreenGlow = PrimaryAccent.copy(alpha = 0.2f)
val GreenGlowSubtle = PrimaryAccent.copy(alpha = 0.1f)
val CardGradientTop = SurfaceElevated
val CardGradientBottom = SurfacePrimary
val PositiveChange = ColorSuccess
val NegativeChange = ColorError
val SuccessContainer = ColorSuccessContainer
val WarningContainer = ColorWarningContainer
val ErrorContainer = ColorErrorContainer
```

---

**End of Report**
