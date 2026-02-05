# Image optimization script for Pyera Finance (PowerShell version)
# This script converts large JPG images to WebP format for APK size reduction

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Pyera Finance - Image Optimization Script" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

$ResDir = "app/src/main/res"
$TotalSaved = 0
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
Set-Location $ProjectRoot

# Function to convert JPG to WebP
function Convert-JpgToWebp {
    param([string]$InputFile)
    
    $OutputFile = [System.IO.Path]::ChangeExtension($InputFile, ".webp")
    $FileName = Split-Path -Leaf $InputFile
    
    # Check if WebP already exists
    if (Test-Path $OutputFile) {
        Write-Host "  ⚠ WebP already exists: $(Split-Path -Leaf $OutputFile) - skipping" -ForegroundColor Yellow
        return 0
    }
    
    # Check for cwebp
    $CwebpPath = Get-Command cwebp -ErrorAction SilentlyContinue
    if (-not $CwebpPath) {
        # Try common installation paths
        $PossiblePaths = @(
            "C:\Program Files\libwebp\bin\cwebp.exe",
            "C:\libwebp\bin\cwebp.exe",
            "$env:USERPROFILE\libwebp\bin\cwebp.exe"
        )
        
        foreach ($Path in $PossiblePaths) {
            if (Test-Path $Path) {
                $CwebpPath = $Path
                break
            }
        }
    }
    
    if ($CwebpPath) {
        $OriginalSize = (Get-Item $InputFile).Length
        
        # Convert with quality 85
        & $CwebpPath -q 85 $InputFile -o $OutputFile 2>$null
        
        if ($LASTEXITCODE -eq 0 -and (Test-Path $OutputFile)) {
            $NewSize = (Get-Item $OutputFile).Length
            $Saved = $OriginalSize - $NewSize
            $SavedKB = [math]::Round($Saved / 1KB, 2)
            $Percent = [math]::Round(($Saved / $OriginalSize) * 100, 1)
            
            Write-Host "  ✓ Converted: $FileName" -ForegroundColor Green
            Write-Host "    Original: $([math]::Round($OriginalSize/1KB, 2))KB → New: $([math]::Round($NewSize/1KB, 2))KB (Saved: ${SavedKB}KB, ${Percent}%)" -ForegroundColor Gray
            return $Saved
        } else {
            Write-Host "  ✗ Failed to convert: $FileName" -ForegroundColor Red
            return 0
        }
    } else {
        Write-Host "  ⚠ cwebp not found. Install WebP tools:" -ForegroundColor Yellow
        Write-Host "    Download from: https://developers.google.com/speed/webp/download" -ForegroundColor Gray
        Write-Host "    Add to PATH or place in C:\libwebp\bin\" -ForegroundColor Gray
        return -1
    }
}

# Function to check mipmap icons
function Check-MipmapIcons {
    Write-Host ""
    Write-Host "Checking mipmap icons..." -ForegroundColor Cyan
    
    $MipmapDirs = Get-ChildItem -Path $ResDir -Directory -Filter "mipmap-*"
    foreach ($Dir in $MipmapDirs) {
        $PngCount = (Get-ChildItem -Path $Dir.FullName -Filter "*.png" -ErrorAction SilentlyContinue).Count
        $WebpCount = (Get-ChildItem -Path $Dir.FullName -Filter "*.webp" -ErrorAction SilentlyContinue).Count
        
        if ($PngCount -gt 0) {
            Write-Host "  📁 $($Dir.Name): $PngCount PNG files, $WebpCount WebP files" -ForegroundColor White
        }
    }
}

Write-Host ""
Write-Host "Step 1: Finding large JPG files (>100KB)..." -ForegroundColor Cyan
Write-Host "------------------------------------------" -ForegroundColor Cyan

# Find large JPG files
$JpgFiles = Get-ChildItem -Path "$ResDir\drawable*" -Filter "*.jpg" -Recurse | Where-Object { $_.Length -gt 100KB }

if ($JpgFiles.Count -eq 0) {
    Write-Host "  No large JPG files found (>100KB)" -ForegroundColor Yellow
} else {
    Write-Host "Found $($JpgFiles.Count) JPG files to convert:" -ForegroundColor Green
    
    foreach ($File in $JpgFiles | Sort-Object Length -Descending) {
        $Saved = Convert-JpgToWebp -InputFile $File.FullName
        if ($Saved -eq -1) {
            Write-Host "`nPlease install WebP tools and run again." -ForegroundColor Red
            exit 1
        }
        $TotalSaved += $Saved
    }
}

Write-Host ""
Write-Host "Step 2: Checking mipmap icons..." -ForegroundColor Cyan
Write-Host "------------------------------------------" -ForegroundColor Cyan
Check-MipmapIcons

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "Optimization Summary" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green

if ($TotalSaved -gt 0) {
    $SavedMB = [math]::Round($TotalSaved / 1MB, 2)
    Write-Host "Total space saved: $([math]::Round($TotalSaved/1KB, 2))KB ($SavedMB MB)" -ForegroundColor Green
} else {
    Write-Host "No conversions performed" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "  1. Review converted WebP files" -ForegroundColor White
Write-Host "  2. Update code references from .jpg to .webp" -ForegroundColor White
Write-Host "  3. Delete original JPG files after verification" -ForegroundColor White
Write-Host "  4. Consider removing unused resources (see UNUSED_RESOURCES.md)" -ForegroundColor White
Write-Host ""
Write-Host "Image optimization complete!" -ForegroundColor Green
