# WebP Image Conversion Guide for Pyera Finance

This guide explains how to convert large JPG background images to WebP format to significantly reduce APK size.

## 📊 Current Image Analysis

| File | Size (JPG) | Priority | Used In |
|------|------------|----------|---------|
| `bg_gradient_green_gradient_background.jpg` | **21.82 MB** | 🔴 CRITICAL | Splash Screen (themes.xml, splash_background.xml) |
| `bg_gradient_photo_green_backgrounds_green_abstract_backgrounds.jpg` | 2.77 MB | 🟡 Medium | Unused (can be deleted) |
| `bg_gradient_photo_green_backgrounds_green_abstract_backgrounds_1.jpg` | 2.48 MB | 🟡 Medium | Unused (can be deleted) |
| `bg_gradient_abstract_colorful_gradient_background_design_as_banner_ads_presentation_concept.jpg` | 1.64 MB | 🟡 Medium | Unused (can be deleted) |
| `bg_gradient_abstract_pui_91_background_wallpaper.jpg` | 0.57 MB | 🟢 Low | Unused (can be deleted) |
| `bg_abstract_green_waves.jpg` | 0.18 MB | 🟡 Medium | WelcomeScreen, OnboardingScreen |
| `bg_auth_green_flow.jpg` | 0.11 MB | 🟡 Medium | LoginScreen, RegisterScreen |

**Total JPG Size: ~29.57 MB**

## 🎯 Projected Savings

| Conversion Scenario | Estimated Size | Savings |
|---------------------|----------------|---------|
| Current (JPG only) | ~29.6 MB | - |
| After WebP conversion (85% quality) | ~7-10 MB | **~20-22 MB (70%)** |
| Delete unused + WebP conversion | ~5-7 MB | **~24-25 MB (80%)** |

## 🚀 Quick Start - Three Options

### Option 1: PowerShell Script (Recommended for Windows)

```powershell
# Run the conversion script
.\scripts\convert_to_webp.ps1

# With custom quality (default is 85)
.\scripts\convert_to_webp.ps1 -Quality 90

# Just analyze without converting
.\scripts\convert_to_webp.ps1 -AnalyzeOnly
```

**Prerequisites:**
1. Download libwebp from https://developers.google.com/speed/webp/download
2. Extract and add `bin/` folder to your system PATH
3. Restart PowerShell

### Option 2: Android Studio Built-in Converter

1. Right-click on any JPG file in Android Studio
2. Select **Convert to WebP...**
3. Choose quality (recommend 85% for backgrounds)
4. Click OK
5. Android Studio will convert and show size comparison

### Option 3: Online Converters (No installation)

1. **Squoosh** (Google) - https://squoosh.app/
   - Drag and drop images
   - Compare before/after in real-time
   - Download WebP versions

2. **Convertio** - https://convertio.co/jpg-webp/
   - Upload multiple files
   - Download converted WebP files

3. **CloudConvert** - https://cloudconvert.com/jpg-to-webp
   - Batch conversion support

## 📝 Step-by-Step Conversion Process

### Step 1: Convert Critical Images

Priority 1 (Must convert):
- [ ] `bg_gradient_green_gradient_background.jpg` → `.webp` (saves ~15-17 MB)

Priority 2 (Should convert):
- [ ] `bg_auth_green_flow.jpg` → `.webp`
- [ ] `bg_abstract_green_waves.jpg` → `.webp`

### Step 2: Delete Unused Images

These files appear to be unused (check before deleting):
- `bg_gradient_photo_green_backgrounds_green_abstract_backgrounds.jpg`
- `bg_gradient_photo_green_backgrounds_green_abstract_backgrounds_1.jpg`
- `bg_gradient_abstract_colorful_gradient_background_design_as_banner_ads_presentation_concept.jpg`
- `bg_gradient_abstract_pui_91_background_wallpaper.jpg`

**⚠️ Verify unused files:**
```bash
# Search for references in the codebase
grep -r "bg_gradient_photo_green_backgrounds" app/src/
grep -r "bg_gradient_abstract_colorful" app/src/
grep -r "bg_gradient_abstract_pui" app/src/
```

### Step 3: Update References (Usually NOT needed)

Good news! **Android handles file extensions automatically.** 

When you reference `R.drawable.bg_gradient_green_gradient_background`, Android will automatically use:
- `bg_gradient_green_gradient_background.webp` if it exists
- `bg_gradient_green_gradient_background.jpg` as fallback

**No code changes required!**

The following files use background images (no changes needed):
- `app/src/main/res/values-v31/themes.xml` - Splash screen background
- `app/src/main/res/drawable/splash_background.xml` - Splash background drawable
- `app/src/main/java/com/pyera/app/ui/auth/LoginScreen.kt` - Login background
- `app/src/main/java/com/pyera/app/ui/auth/RegisterScreen.kt` - Register background
- `app/src/main/java/com/pyera/app/ui/welcome/WelcomeScreen.kt` - Welcome background
- `app/src/main/java/com/pyera/app/ui/onboarding/OnboardingScreen.kt` - Onboarding background

### Step 4: Clean and Rebuild

After conversion:
```bash
# Clean build
.\gradlew clean

# Build APK
.\gradlew assembleDebug

# Compare APK sizes
```

## 🔧 Command Line Conversion (Cross-platform)

### Using cwebp (libwebp)

```bash
# Single file
cwebp -q 85 bg_gradient_green_gradient_background.jpg -o bg_gradient_green_gradient_background.webp

# Batch conversion (all JPGs)
for file in *.jpg; do cwebp -q 85 "$file" -o "${file%.jpg}.webp"; done
```

### Using ImageMagick

```bash
# Single file
convert bg_gradient_green_gradient_background.jpg -quality 85 bg_gradient_green_gradient_background.webp

# Batch conversion
mogrify -format webp -quality 85 *.jpg
```

### Using FFmpeg

```bash
# Single file
ffmpeg -i bg_gradient_green_gradient_background.jpg -q:v 85 bg_gradient_green_gradient_background.webp
```

## 📋 Quality Settings Guide

| Quality | Use Case | Size Reduction |
|---------|----------|----------------|
| 90-95% | Critical images, visible text | 40-50% |
| 80-85% | Backgrounds, photos (Recommended) | 60-70% |
| 70-75% | Thumbnails, previews | 70-80% |
| 60% or less | Icons, small graphics | 80%+ |

**Recommendation for Pyera:** Use **85%** quality for background images.

## ✅ Post-Conversion Checklist

- [ ] Convert `bg_gradient_green_gradient_background.jpg` to WebP
- [ ] Convert `bg_auth_green_flow.jpg` to WebP
- [ ] Convert `bg_abstract_green_waves.jpg` to WebP
- [ ] Delete unused JPG files (after verification)
- [ ] Clean build (`./gradlew clean`)
- [ ] Test app launch (splash screen)
- [ ] Test Login screen
- [ ] Test Register screen
- [ ] Test Welcome screen
- [ ] Test Onboarding screen
- [ ] Verify APK size reduction
- [ ] Commit changes

## 🐛 Troubleshooting

### Issue: WebP images not loading
- **Solution:** Ensure minSdkVersion supports WebP (API 14+ for simple WebP, API 18+ for transparency)
- Check: `app/build.gradle.kts` → `minSdk = 24` ✅ (Pyera is OK)

### Issue: cwebp command not found
- **Solution:** Add libwebp bin folder to PATH or use full path
- Alternative: Use Android Studio converter or online tools

### Issue: Conversion quality too low
- **Solution:** Increase quality parameter (`-q 90` instead of `-q 85`)

## 📚 References

- [WebP Official Site](https://developers.google.com/speed/webp)
- [Android WebP Guide](https://developer.android.com/studio/write/convert-webp)
- [Squoosh App](https://squoosh.app/)
- [WebP vs JPG Comparison](https://developers.google.com/speed/webp/docs/webp_study)
