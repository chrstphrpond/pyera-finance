# Pyera Design System

A comprehensive design system documentation for the Pyera finance application.

---

## Color Tokens

### Brand Colors

| Token            | Hex       | Usage                    |
| ---------------- | --------- | ------------------------ |
| `Inchworm`       | `#8CE700` | Primary brand lime green |
| `Orange`         | `#FE7733` | Accent orange            |
| `PaleViolet`     | `#B2A1FF` | Secondary violet         |
| `BrightSnow`     | `#FFFFFF` | Pure white               |
| `AmericanSilver` | `#D1D1D1` | Light gray               |

### Surface Colors

| Token             | Hex       | Usage             |
| ----------------- | --------- | ----------------- |
| `DeepBackground`  | `#080C0B` | App background    |
| `CardBackground`  | `#131918` | Cards, surfaces   |
| `SurfaceDark`     | `#0E1312` | Darker variant    |
| `SurfaceElevated` | `#1A2220` | Elevated surfaces |

### Accent Colors

| Token            | Hex       | Usage                    |
| ---------------- | --------- | ------------------------ |
| `AccentGreen`    | `#8CE700` | Primary accent           |
| `AccentGreenDim` | `#5A9400` | Inactive/disabled states |

### Text Colors

| Token           | Value            | Usage           |
| --------------- | ---------------- | --------------- |
| `TextPrimary`   | `BrightSnow`     | Primary text    |
| `TextSecondary` | `AmericanSilver` | Secondary text  |
| `TextTertiary`  | `#5A6462`        | Muted/hint text |

### Semantic Colors

| Token            | Hex           | Usage              |
| ---------------- | ------------- | ------------------ |
| `ColorSuccess`   | `AccentGreen` | Success states     |
| `ColorWarning`   | `Orange`      | Warning states     |
| `ColorError`     | `#EF4444`     | Error states       |
| `ColorInfo`      | `PaleViolet`  | Info states        |
| `PositiveChange` | `#4ADE80`     | Positive % changes |
| `NegativeChange` | `#EF4444`     | Negative % changes |

### Container Colors

| Token              | Hex       | Usage               |
| ------------------ | --------- | ------------------- |
| `SuccessContainer` | `#1B2A18` | Success backgrounds |
| `WarningContainer` | `#2A1F14` | Warning backgrounds |
| `ErrorContainer`   | `#2A1414` | Error backgrounds   |
| `InfoContainer`    | `#1A1A2A` | Info backgrounds    |

### Border Colors

| Token          | Hex         | Usage                  |
| -------------- | ----------- | ---------------------- |
| `CardBorder`   | `#1E2826`   | Card/component borders |
| `GlassOverlay` | `#1AFFFFFF` | 10% white glass effect |

---

## Typography

### Font Family

**Juturu** - Custom font with 9 weight variants:

- Thin (100)
- ExtraLight (200)
- Light (300)
- Regular (400)
- Medium (500)
- SemiBold (600)
- Bold (700)
- ExtraBold (800)
- Black (900)

### Type Scale

| Style            | Size | Weight   | Letter Spacing |
| ---------------- | ---- | -------- | -------------- |
| `displayLarge`   | 48sp | Bold     | -0.02sp        |
| `displayMedium`  | 40sp | Bold     | -0.02sp        |
| `displaySmall`   | 36sp | SemiBold | -0.01sp        |
| `headlineLarge`  | 32sp | SemiBold | -0.01sp        |
| `headlineMedium` | 24sp | SemiBold | -0.01sp        |
| `headlineSmall`  | 18sp | SemiBold | 0sp            |
| `titleLarge`     | 22sp | SemiBold | 0sp            |
| `titleMedium`    | 16sp | Medium   | 0.15sp         |
| `titleSmall`     | 14sp | Medium   | 0.1sp          |
| `bodyLarge`      | 16sp | Normal   | 0sp            |
| `bodyMedium`     | 14sp | Normal   | 0sp            |
| `bodySmall`      | 12sp | Normal   | 0sp            |
| `labelLarge`     | 14sp | Medium   | 0.1sp          |
| `labelMedium`    | 12sp | Medium   | 0.3sp          |
| `labelSmall`     | 11sp | Medium   | 0.5sp          |

---

## Theme Configuration

### Default Mode

- **Dark theme by default** (`darkTheme = true`)
- Dynamic color **disabled** to maintain brand identity
- Light scheme exists as fallback

### Material3 Color Scheme Mapping

```
primary         → AccentGreen
secondary       → PaleViolet
tertiary        → Orange
background      → DeepBackground
surface         → CardBackground
surfaceVariant  → SurfaceElevated
error           → ColorError
outline         → CardBorder
```

---

## Spacing (Recommended)

> **Note:** No spacing tokens currently defined. Recommend creating `Spacing.kt`:

```kotlin
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}
```

---

## Findings & Recommendations

### ⚠️ Issues Found

| Issue             | Location                          | Recommendation              |
| ----------------- | --------------------------------- | --------------------------- |
| Hardcoded colors  | `TransactionViewModel.kt:241-244` | Extract to Color.kt         |
| No spacing tokens | -                                 | Create Spacing.kt           |
| Legacy aliases    | `Color.kt:50-55`                  | Migrate usages, then remove |

### ✅ What's Working Well

- Semantic color naming
- Complete typography scale
- Dark-first approach
- Material3 integration
- Glass overlay effect token
