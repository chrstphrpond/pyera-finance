# Pyera Finance - Unused Resources Analysis

**Date:** 2026-02-05  
**Analysis Version:** 1.0  
**Current APK Size:** 69.07 MB (debug)  
**Drawable Resources Size:** 29.59 MB

---

## 🔴 CRITICAL SIZE ISSUES

### Single Large File Alert
| File | Size | Status |
|------|------|--------|
| `bg_gradient_green_gradient_background.jpg` | **21.82 MB** | ⚠️ IN USE (Splash Screen) |

**Impact:** This single file accounts for **31.6% of the entire APK size**!

---

## 📊 Resource Usage Analysis

### Currently Used Resources

| Resource | Size | Used In | Priority |
|----------|------|---------|----------|
| `bg_gradient_green_gradient_background.jpg` | 21.82 MB | splash_background.xml, themes.xml | 🔴 CRITICAL |
| `bg_auth_green_flow.jpg` | 0.11 MB | LoginScreen.kt, RegisterScreen.kt | 🟡 Medium |
| `bg_abstract_green_waves.jpg` | 0.18 MB | WelcomeScreen.kt, OnboardingScreen.kt | 🟡 Medium |
| `ic_logo_full` (XML) | ~2KB | LoginScreen.kt | 🟢 Low |

### Potentially Unused Resources

The following resources were **NOT found** in any code references:

| File | Size | Location | Recommendation |
|------|------|----------|----------------|
| `bg_gradient_photo_green_backgrounds_green_abstract_backgrounds.jpg` | 2.77 MB | drawable/ | 🔴 Remove if unused |
| `bg_gradient_photo_green_backgrounds_green_abstract_backgrounds_1.jpg` | 2.48 MB | drawable/ | 🔴 Remove if unused |
| `bg_gradient_abstract_colorful_gradient_background_design_as_banner_ads_presentation_concept.jpg` | 1.64 MB | drawable/ | 🔴 Remove if unused |
| `bg_gradient_abstract_pui_91_background_wallpaper.jpg` | 0.57 MB | drawable/ | 🟡 Review & Remove |

**Total potentially removable:** ~7.46 MB (10.8% of APK size)

---

## 📁 Mipmap Icons Analysis

All mipmap icons are currently in PNG format. Consider converting to WebP for additional savings.

| Density | PNG Files | Total Size | WebP Status |
|---------|-----------|------------|-------------|
| mipmap-mdpi | 3 | ~6.85 KB | Not converted |
| mipmap-hdpi | 3 | ~6.85 KB | Not converted |
| mipmap-xhdpi | 3 | ~8.07 KB | Not converted |
| mipmap-xxhdpi | 3 | ~15.46 KB | Not converted |
| mipmap-xxxhdpi | 3 | ~30.69 KB | Not converted |
| mipmap-anydpi-v26 | 2 XML | ~0.52 KB | N/A (adaptive) |

**Estimated WebP savings:** ~20-30% (approximately 10-15KB total)

---

## 🎯 Optimization Recommendations

### Immediate Actions (High Impact)

1. **Convert Critical 22MB JPG to WebP**
   - File: `bg_gradient_green_gradient_background.jpg`
   - Expected savings: 60-80% (13-17 MB reduction)
   - Impact: Massive APK size reduction

2. **Remove Unused Background Images**
   - Remove 4 unused JPG files
   - Immediate savings: ~7.46 MB

3. **Verify Resource References**
   ```bash
   # Double-check with grep before deleting:
   grep -r "bg_gradient_photo_green_backgrounds" app/src/
   grep -r "bg_gradient_abstract_colorful" app/src/
   grep -r "bg_gradient_abstract_pui" app/src/
   ```

### Secondary Actions (Medium Impact)

4. **Convert Remaining JPGs to WebP**
   - `bg_auth_green_flow.jpg` (0.11 MB)
   - `bg_abstract_green_waves.jpg` (0.18 MB)
   - Expected savings: 30-50% per file

5. **Convert Mipmap Icons to WebP**
   - Convert all PNG launcher icons
   - Expected savings: ~10-15KB

### Build Configuration

6. **Resource Shrinking Already Enabled**
   - ✅ `isShrinkResources = true` in release build
   - Consider adding `resConfigs("en")` for language filtering

---

## 🧮 Size Impact Projection

| Action | Estimated Savings | Cumulative |
|--------|------------------|------------|
| Convert 22MB splash to WebP | ~15 MB | 54 MB |
| Remove 4 unused JPGs | ~7.5 MB | 46.5 MB |
| Convert remaining JPGs | ~0.1 MB | 46.4 MB |
| Convert mipmap icons | ~0.01 MB | 46.4 MB |

**Projected final APK size:** ~46-50 MB (**~28-33% reduction**)

---

## ⚠️ Important Notes

1. **Splash Screen Image:** The 22MB background is CRITICAL - do not delete without creating WebP replacement first
2. **Vector Drawables:** All XML drawables are properly optimized
3. **Test After Changes:** Always test splash screen and auth flows after optimization
4. **Version Control:** Commit before bulk deletions

---

## 🔧 Quick Commands

```bash
# Run optimization script
./scripts/optimize_images.sh

# Verify file usage before deletion
grep -r "filename_without_extension" app/src/

# Build release APK to verify size
./gradlew assembleRelease
```
