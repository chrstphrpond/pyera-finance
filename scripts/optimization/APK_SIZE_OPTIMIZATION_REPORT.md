# Pyera Finance APK Size Optimization - Phase 2 Report

**Date:** 2026-02-05  
**Current APK Size:** 69.07 MB (debug)  
**Target:** Reduce size through resource optimization  
**Status:** ✅ Analysis Complete, Scripts Created

---

## Executive Summary

The Pyera Finance APK has significant size bloat due to unoptimized image resources. **A single 22MB JPG file accounts for 31.6% of the entire APK size.** This phase focuses on identifying and documenting optimization opportunities.

### Key Findings
- 📦 **Total drawable resources:** 29.59 MB
- 🖼️ **Large JPG files:** 7 files (>100KB each)
- 🔴 **Critical issue:** 1 file at 21.82 MB
- 🗑️ **Potentially unused:** 4 files (~7.46 MB)

---

## Detailed Analysis

### 1. Drawable Resources Breakdown

| Folder | File Count | Total Size |
|--------|------------|------------|
| drawable/ | 16 files | 29.59 MB |
| drawable-xxhdpi/ | 0 files | 0 MB |
| drawable-xxxhdpi/ | 0 files | 0 MB |

### 2. Large Image Files (>100KB)

| Rank | File | Size | Type | Priority |
|------|------|------|------|----------|
| 1 | `bg_gradient_green_gradient_background.jpg` | 21.82 MB | Background | 🔴 CRITICAL |
| 2 | `bg_gradient_photo_green_backgrounds_green_abstract_backgrounds.jpg` | 2.77 MB | Background | 🟡 High |
| 3 | `bg_gradient_photo_green_backgrounds_green_abstract_backgrounds_1.jpg` | 2.48 MB | Background | 🟡 High |
| 4 | `bg_gradient_abstract_colorful_gradient_background_design_as_banner_ads_presentation_concept.jpg` | 1.64 MB | Background | 🟡 High |
| 5 | `bg_gradient_abstract_pui_91_background_wallpaper.jpg` | 0.57 MB | Background | 🟢 Medium |
| 6 | `bg_abstract_green_waves.jpg` | 0.18 MB | Background | 🟢 Medium |
| 7 | `bg_auth_green_flow.jpg` | 0.11 MB | Background | 🟢 Medium |

**Total large JPG size:** ~29.57 MB

### 3. File Usage Analysis

#### In Use (DO NOT DELETE)
- ✅ `bg_gradient_green_gradient_background.jpg` - Splash screen background
- ✅ `bg_auth_green_flow.jpg` - Login/Register screens
- ✅ `bg_abstract_green_waves.jpg` - Welcome/Onboarding screens

#### Potentially Unused (VERIFY BEFORE DELETE)
- ❓ `bg_gradient_photo_green_backgrounds_green_abstract_backgrounds.jpg` - No code references
- ❓ `bg_gradient_photo_green_backgrounds_green_abstract_backgrounds_1.jpg` - No code references
- ❓ `bg_gradient_abstract_colorful_gradient_background_design_as_banner_ads_presentation_concept.jpg` - No code references
- ❓ `bg_gradient_abstract_pui_91_background_wallpaper.jpg` - No code references

---

## Optimization Scripts Created

### 1. `scripts/optimize_images.sh` (Bash)
- Cross-platform compatibility (macOS, Linux)
- Converts JPG to WebP using cwebp
- Calculates size savings
- Provides detailed progress output

### 2. `scripts/optimize_images.ps1` (PowerShell)
- Windows-native support
- Same functionality as Bash version
- Color-coded output for better readability

---

## Build Configuration Status

### Current Configuration (`app/build.gradle.kts`)

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true        // ✅ Enabled
        isShrinkResources = true      // ✅ Enabled
        proguardFiles(...)
    }
}
```

### Recommended Addition

```kotlin
defaultConfig {
    resConfigs("en")  // Only include English resources
    // ... other config
}
```

**Impact:** Removes unused language resources from libraries (Firebase, Play Services, etc.)

---

## Projected Size Savings

| Optimization | Current | After WebP | Savings | % Reduction |
|-------------|---------|------------|---------|-------------|
| 22MB splash image | 21.82 MB | ~4-6 MB | ~15-17 MB | ~75% |
| 4 unused JPGs (delete) | 7.46 MB | 0 MB | 7.46 MB | 100% |
| Other JPGs to WebP | 0.40 MB | ~0.2 MB | ~0.2 MB | ~50% |
| Mipmap to WebP | 0.07 MB | ~0.05 MB | ~0.02 MB | ~30% |
| **TOTAL** | **69.07 MB** | **~46-48 MB** | **~22-24 MB** | **~32-35%** |

---

## Action Items

### Immediate (Phase 2B)

1. [ ] Install WebP tools (`cwebp`)
   - macOS: `brew install webp`
   - Ubuntu: `sudo apt-get install webp`
   - Windows: Download from Google

2. [ ] Run optimization script
   ```bash
   ./scripts/optimize_images.sh
   ```

3. [ ] Update code references for converted images
   - Change `.jpg` to `.webp` in XML and Kotlin files

4. [ ] Verify splash screen works correctly

5. [ ] Delete original JPG files after verification

### Secondary (Phase 2C)

6. [ ] Verify unused resources (double-check grep results)
7. [ ] Delete confirmed unused JPG files
8. [ ] Convert mipmap PNG icons to WebP
9. [ ] Add `resConfigs("en")` to build.gradle.kts
10. [ ] Build release APK and verify size reduction

---

## Files Created

| File | Purpose |
|------|---------|
| `scripts/optimize_images.sh` | Bash script for image optimization |
| `scripts/optimize_images.ps1` | PowerShell script for Windows |
| `scripts/UNUSED_RESOURCES.md` | Documentation of potentially unused resources |
| `scripts/APK_SIZE_OPTIMIZATION_REPORT.md` | This report |

---

## Next Steps

After completing Phase 2 (Resource Optimization), proceed to:

- **Phase 3:** Dependency Analysis & Optimization
- **Phase 4:** Code Obfuscation & Shrinking Review
- **Phase 5:** APK Split Configuration (ABI splits)

---

## Appendix: Verification Commands

```bash
# Check APK size
ls -lh app/build/outputs/apk/release/*.apk

# Analyze APK contents
unzip -l app-release.apk | sort -n

# Find large files in APK
unzip -l app-release.apk | awk '{print $1, $NF}' | sort -n | tail -20

# Check resource usage
grep -r "R.drawable" app/src/main/java --include="*.kt"
grep -r "@drawable/" app/src/main/res --include="*.xml"
```

---

**Report Generated:** 2026-02-05  
**By:** APK Size Optimization Agent
