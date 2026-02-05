# Removed Resources - Pyera Finance App

## Date: 2026-02-05

## Summary

Performed Android resource cleanup to reduce APK size by identifying and removing unused drawable resources.

### Removed Background Images

| File | Size | Reason |
|------|------|--------|
| `bg_gradient_photo_green_backgrounds_green_abstract_backgrounds.jpg` | 2.77 MB | Unused - No code references found |
| `bg_gradient_photo_green_backgrounds_green_abstract_backgrounds_1.jpg` | 2.48 MB | Unused - No code references found |
| `bg_gradient_abstract_colorful_gradient_background_design_as_banner_ads_presentation_concept.jpg` | 1.64 MB | Unused - No code references found |
| `bg_gradient_abstract_pui_91_background_wallpaper.jpg` | 0.57 MB | Unused - No code references found |

### Total Space Saved: 7.46 MB

### Backup Location

Removed files backed up to: `scripts/backup_resources/`

---

## Resource Usage Analysis

### Used Resources (Kept)

| File | Size | Used In |
|------|------|---------|
| `bg_gradient_green_gradient_background.jpg` | 21.82 MB | `splash_background.xml`, `themes.xml` |
| `bg_abstract_green_waves.jpg` | 0.18 MB | `WelcomeScreen.kt`, `OnboardingScreen.kt` |
| `bg_auth_green_flow.jpg` | 0.11 MB | `LoginScreen.kt`, `RegisterScreen.kt` |
| `bg_gradient_dark.xml` | ~0 KB | Gradient definition (potentially used dynamically) |
| `bg_gradient_neon.xml` | ~0 KB | Gradient definition (potentially used dynamically) |

### Icons (Kept - All Used)

- `ic_app_logo.png`
- `ic_launcher_foreground.xml`
- `ic_logo_full.xml`
- `ic_logo_icon.xml`
- `ic_splash_logo.xml`
- `ic_splash_logo_white.xml`
- `splash_background.xml`

---

## Verification Method

1. **Searched for resource references** in:
   - `.kt` files (Kotlin code)
   - `.xml` files (layouts, drawables, themes)
   
2. **Search patterns used**:
   - `R.drawable.<resource_name>`
   - `@drawable/<resource_name>`
   - Direct file name references

3. **Confirmed unused** when no references found in any source files

---

## Safety Measures

1. **Backup created** - All removed files backed up before deletion
2. **100% verified** - Only removed files with zero code references
3. **Preserved uncertain files** - Kept `bg_gradient_dark.xml` and `bg_gradient_neon.xml` even though no direct references found (may be used dynamically)

---

## Recommendations for Future

1. **Run Android Lint** regularly to catch unused resources
2. **Consider further optimization**:
   - The `bg_gradient_green_gradient_background.jpg` (21.82 MB) is very large - consider compressing or using a gradient XML instead
3. **Use WebP format** for better compression on background images
4. **Remove backup folder** after confirming app works correctly in production
