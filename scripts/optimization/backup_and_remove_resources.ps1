# Backup and Remove Unused Resources Script
# Pyera Finance App - Resource Cleanup
# Date: 2026-02-05

param(
    [switch]$Restore = $false,
    [switch]$CleanBackup = $false
)

$backupDir = "scripts/backup_resources"
$drawableDir = "app/src/main/res/drawable"

# List of files that were removed (verified unused)
$removedFiles = @(
    "bg_gradient_photo_green_backgrounds_green_abstract_backgrounds.jpg",
    "bg_gradient_photo_green_backgrounds_green_abstract_backgrounds_1.jpg",
    "bg_gradient_abstract_colorful_gradient_background_design_as_banner_ads_presentation_concept.jpg",
    "bg_gradient_abstract_pui_91_background_wallpaper.jpg"
)

function Backup-AndRemoveFiles {
    Write-Host "=== Pyera Resource Cleanup ===" -ForegroundColor Cyan
    Write-Host ""
    
    # Create backup directory
    if (!(Test-Path $backupDir)) {
        New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
        Write-Host "Created backup directory: $backupDir" -ForegroundColor Green
    }
    
    $totalSize = 0
    $removedCount = 0
    
    foreach ($file in $removedFiles) {
        $sourcePath = Join-Path $drawableDir $file
        $backupPath = Join-Path $backupDir $file
        
        if (Test-Path $sourcePath) {
            $size = (Get-Item $sourcePath).Length
            $totalSize += $size
            $sizeMB = [math]::Round($size / 1MB, 2)
            
            # Backup first
            Copy-Item $sourcePath $backupDir -Force
            Write-Host "Backed up: $file" -ForegroundColor Yellow
            
            # Remove
            Remove-Item $sourcePath -Force
            Write-Host "Removed: $file ($sizeMB MB)" -ForegroundColor Green
            
            $removedCount++
        } else {
            Write-Host "File not found (already removed?): $file" -ForegroundColor Gray
        }
    }
    
    Write-Host ""
    Write-Host "=== Summary ===" -ForegroundColor Cyan
    Write-Host "Files processed: $removedCount" -ForegroundColor White
    Write-Host "Total space saved: $([math]::Round($totalSize / 1MB, 2)) MB" -ForegroundColor Green
    Write-Host "Backup location: $backupDir" -ForegroundColor Yellow
}

function Restore-Files {
    Write-Host "=== Restoring Resources ===" -ForegroundColor Cyan
    Write-Host ""
    
    if (!(Test-Path $backupDir)) {
        Write-Host "Backup directory not found: $backupDir" -ForegroundColor Red
        return
    }
    
    $restoredCount = 0
    
    foreach ($file in $removedFiles) {
        $backupPath = Join-Path $backupDir $file
        $targetPath = Join-Path $drawableDir $file
        
        if (Test-Path $backupPath) {
            Copy-Item $backupPath $targetPath -Force
            Write-Host "Restored: $file" -ForegroundColor Green
            $restoredCount++
        } else {
            Write-Host "Backup not found: $file" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    Write-Host "Restored $restoredCount files" -ForegroundColor Green
}

function Clear-Backup {
    Write-Host "=== Cleaning Backup Directory ===" -ForegroundColor Cyan
    Write-Host ""
    
    if (Test-Path $backupDir) {
        Remove-Item $backupDir -Recurse -Force
        Write-Host "Backup directory removed: $backupDir" -ForegroundColor Green
    } else {
        Write-Host "Backup directory not found" -ForegroundColor Gray
    }
}

# Main execution
if ($Restore) {
    Restore-Files
} elseif ($CleanBackup) {
    $confirm = Read-Host "Are you sure you want to delete the backup folder? (yes/no)"
    if ($confirm -eq "yes") {
        Clear-Backup
    } else {
        Write-Host "Operation cancelled" -ForegroundColor Yellow
    }
} else {
    # Show what would be done
    Write-Host "This script manages resource cleanup for Pyera Finance app" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Usage:" -ForegroundColor Yellow
    Write-Host "  .\scripts\backup_and_remove_resources.ps1           # Show this help"
    Write-Host "  .\scripts\backup_and_remove_resources.ps1 -Restore  # Restore files from backup"
    Write-Host "  .\scripts\backup_and_remove_resources.ps1 -CleanBackup  # Remove backup folder"
    Write-Host ""
    Write-Host "Files that were removed:" -ForegroundColor Cyan
    foreach ($file in $removedFiles) {
        Write-Host "  - $file" -ForegroundColor Gray
    }
    Write-Host ""
    Write-Host "Backup location: $backupDir" -ForegroundColor Yellow
}
