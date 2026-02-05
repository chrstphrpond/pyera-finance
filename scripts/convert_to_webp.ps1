# WebP Conversion Script for Pyera Finance App
# =====================================================
# This script converts JPG background images to WebP format
# to reduce APK size significantly.
#
# REQUIREMENTS:
#   - libwebp (https://developers.google.com/speed/webp/download)
#   - Add cwebp to your system PATH
#
# USAGE:
#   .\convert_to_webp.ps1                    # Convert with default quality (85)
#   .\convert_to_webp.ps1 -Quality 90        # Convert with custom quality
#   .\convert_to_webp.ps1 -AnalyzeOnly       # Just show file sizes without converting
# =====================================================

param(
    [int]$Quality = 85,
    [switch]$AnalyzeOnly,
    [switch]$DeleteOriginals
)

$ErrorActionPreference = "Stop"

# Color output helpers
function Write-Success($msg) { Write-Host "✓ $msg" -ForegroundColor Green }
function Write-Info($msg) { Write-Host "ℹ $msg" -ForegroundColor Cyan }
function Write-Warning($msg) { Write-Host "⚠ $msg" -ForegroundColor Yellow }
function Write-Error($msg) { Write-Host "✗ $msg" -ForegroundColor Red }

# File list - Priority order for conversion
$priorityFiles = @(
    "bg_gradient_green_gradient_background.jpg",      # ~22 MB - CRITICAL
    "bg_gradient_photo_green_backgrounds_green_abstract_backgrounds.jpg",    # ~2.8 MB
    "bg_gradient_photo_green_backgrounds_green_abstract_backgrounds_1.jpg",  # ~2.5 MB
    "bg_gradient_abstract_colorful_gradient_background_design_as_banner_ads_presentation_concept.jpg", # ~1.6 MB
    "bg_gradient_abstract_pui_91_background_wallpaper.jpg",  # ~0.6 MB
    "bg_auth_green_flow.jpg",                           # ~0.1 MB - Used in Login/Register
    "bg_abstract_green_waves.jpg"                       # ~0.2 MB - Used in Welcome/Onboarding
)

$drawableDir = "app/src/main/res/drawable"
$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")

Write-Host ""
Write-Host "========================================" -ForegroundColor Blue
Write-Host "  Pyera Finance - WebP Conversion Tool" -ForegroundColor Blue
Write-Host "========================================" -ForegroundColor Blue
Write-Host ""

# Check if cwebp is available
$cwebpAvailable = $null -ne (Get-Command cwebp -ErrorAction SilentlyContinue)
if (-not $cwebpAvailable) {
    Write-Warning "cwebp not found in PATH"
    Write-Host ""
    Write-Host "To install WebP tools:"
    Write-Host "  1. Download from: https://developers.google.com/speed/webp/download"
    Write-Host "  2. Extract and add bin/ folder to your PATH"
    Write-Host "  3. Or use the online converter: https://squoosh.app/"
    Write-Host ""
}

# Ensure we're in the project root
Set-Location $projectRoot

# Get all JPG files
$allJpgFiles = Get-ChildItem -Path $drawableDir -Filter "*.jpg" -Recurse | Sort-Object Length -Descending

Write-Host "Found $($allJpgFiles.Count) JPG files in drawable folders:" -ForegroundColor Cyan
Write-Host ""

# Display file information
$totalOriginalSize = 0
$fileData = @()

foreach ($file in $allJpgFiles) {
    $sizeMB = [math]::Round($file.Length / 1MB, 2)
    $sizeKB = [math]::Round($file.Length / 1KB, 2)
    $totalOriginalSize += $file.Length
    
    $priority = if ($priorityFiles -contains $file.Name) { "HIGH" } else { "LOW" }
    $priorityColor = if ($priority -eq "HIGH") { "Red" } else { "Gray" }
    
    $webPPath = [System.IO.Path]::ChangeExtension($file.FullName, ".webp")
    $webPExists = Test-Path $webPPath
    
    $status = if ($webPExists) { "[WebP EXISTS]" } else { "" }
    
    Write-Host "  [$priority] " -ForegroundColor $priorityColor -NoNewline
    Write-Host "$($file.Name)" -NoNewline
    Write-Host " - $sizeMB MB" -ForegroundColor Yellow
    
    $fileData += [PSCustomObject]@{
        File = $file
        SizeMB = $sizeMB
        SizeKB = $sizeKB
        Priority = $priority
        WebPExists = $webPExists
    }
}

Write-Host ""
Write-Host "Total JPG size: $([math]::Round($totalOriginalSize / 1MB, 2)) MB" -ForegroundColor Yellow
Write-Host ""

if ($AnalyzeOnly) {
    Write-Host "Analysis complete. Run without -AnalyzeOnly to convert." -ForegroundColor Green
    exit 0
}

# Ask for confirmation
if (-not $DeleteOriginals) {
    Write-Host "Conversion Settings:" -ForegroundColor Cyan
    Write-Host "  Quality: $Quality%"
    Write-Host "  Delete originals after conversion: No"
    Write-Host ""
    $confirm = Read-Host "Proceed with conversion? (y/N)"
    if ($confirm -ne 'y' -and $confirm -ne 'Y') {
        Write-Host "Conversion cancelled." -ForegroundColor Yellow
        exit 0
    }
}

# Perform conversions
Write-Host ""
Write-Host "Starting conversion..." -ForegroundColor Cyan
Write-Host ""

$convertedCount = 0
$failedCount = 0
$skippedCount = 0
$totalSavings = 0

foreach ($item in $fileData) {
    $file = $item.File
    $outputFile = [System.IO.Path]::ChangeExtension($file.FullName, ".webp")
    
    # Skip if WebP already exists
    if ($item.WebPExists -and -not $Force) {
        Write-Warning "Skipping $($file.Name) (WebP already exists)"
        $skippedCount++
        continue
    }
    
    Write-Host "Converting: $($file.Name) ..." -NoNewline
    
    if (-not $cwebpAvailable) {
        Write-Host " SKIPPED (cwebp not found)" -ForegroundColor Yellow
        $skippedCount++
        continue
    }
    
    try {
        # Run cwebp conversion
        $result = & cwebp -q $Quality "$($file.FullName)" -o "$outputFile" 2>&1
        
        if ($LASTEXITCODE -eq 0 -and (Test-Path $outputFile)) {
            $newSize = (Get-Item $outputFile).Length
            $originalSize = $file.Length
            $savings = $originalSize - $newSize
            $savingsPercent = [math]::Round(($savings / $originalSize) * 100, 1)
            $totalSavings += $savings
            
            $originalMB = [math]::Round($originalSize / 1MB, 2)
            $newMB = [math]::Round($newSize / 1MB, 2)
            $savingsMB = [math]::Round($savings / 1MB, 2)
            
            Write-Host " OK" -ForegroundColor Green
            Write-Host "    Original: $originalMB MB → WebP: $newMB MB (Saved: $savingsMB MB / $savingsPercent%)" -ForegroundColor Gray
            
            $convertedCount++
            
            # Optionally delete original
            if ($DeleteOriginals) {
                Remove-Item $file.FullName -Force
                Write-Host "    Deleted original JPG" -ForegroundColor DarkGray
            }
        } else {
            Write-Host " FAILED" -ForegroundColor Red
            $failedCount++
        }
    } catch {
        Write-Host " ERROR: $_" -ForegroundColor Red
        $failedCount++
    }
}

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Blue
Write-Host "  Conversion Summary" -ForegroundColor Blue
Write-Host "========================================" -ForegroundColor Blue
Write-Host ""
Write-Host "Converted: $convertedCount files" -ForegroundColor Green
Write-Host "Skipped:   $skippedCount files" -ForegroundColor Yellow
Write-Host "Failed:    $failedCount files" -ForegroundColor Red
Write-Host ""
Write-Host "Total projected savings: $([math]::Round($totalSavings / 1MB, 2)) MB" -ForegroundColor Cyan
Write-Host ""

if (-not $cwebpAvailable) {
    Write-Host "NOTE: Install cwebp to perform actual conversions:" -ForegroundColor Yellow
    Write-Host "  https://developers.google.com/speed/webp/download" -ForegroundColor Gray
    Write-Host ""
}

# Show projected savings for all files
Write-Host "Projected savings for all priority files:" -ForegroundColor Cyan
Write-Host "  bg_gradient_green_gradient_background.jpg (~22 MB → ~5-7 MB WebP) = ~15-17 MB saved"
Write-Host "  Other files: 60-80% size reduction typical with WebP"
Write-Host ""
Write-Host "Estimated total APK size reduction: 20-25 MB" -ForegroundColor Green
Write-Host ""
