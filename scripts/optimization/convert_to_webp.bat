@echo off
REM WebP Conversion Batch Script for Pyera Finance App
REM =====================================================
REM Simple batch file alternative to PowerShell script
REM 
REM USAGE:
REM   convert_to_webp.bat              - Convert with quality 85
REM   convert_to_webp.bat 90           - Convert with quality 90
REM =====================================================

setlocal EnableDelayedExpansion

set QUALITY=%~1
if "%~1"=="" set QUALITY=85

echo.
echo ========================================
echo   Pyera Finance - WebP Conversion Tool
echo ========================================
echo.
echo Quality setting: %QUALITY%%%
echo.

REM Check for cwebp
where cwebp >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo WARNING: cwebp not found in PATH
    echo.
    echo To install WebP tools:
    echo   1. Download from: https://developers.google.com/speed/webp/download
    echo   2. Extract and add bin/ folder to your PATH
    echo   3. Restart command prompt
    echo.
    pause
    exit /b 1
)

set DRAWABLE_DIR=app\src\main\res\drawable
set TOTAL_SAVED=0

echo Starting conversion of JPG files in %DRAWABLE_DIR%...
echo.

for %%F in (%DRAWABLE_DIR%\*.jpg) do (
    set "FILE=%%F"
    set "BASENAME=%%~nF"
    set "OUTPUT=%DRAWABLE_DIR%\%%~nF.webp"
    
    echo Converting: %%~nxF ...
    
    cwebp -q %QUALITY% "%%F" -o "!OUTPUT!"
    
    if exist "!OUTPUT!" (
        for %%A in ("%%F") do set ORIG_SIZE=%%~zA
        for %%B in ("!OUTPUT!") do set NEW_SIZE=%%~zB
        
        set /a SAVED=!ORIG_SIZE! - !NEW_SIZE!
        set /a TOTAL_SAVED+=!SAVED!
        
        echo   Original: !ORIG_SIZE! bytes
        echo   WebP:     !NEW_SIZE! bytes
        echo   Saved:    !SAVED! bytes
        echo.
    ) else (
        echo   FAILED to convert %%~nxF
    )
)

echo.
echo ========================================
echo   Conversion Complete
echo ========================================
echo.
echo Total bytes saved: %TOTAL_SAVED%
set /a SAVED_MB=%TOTAL_SAVED% / 1048576
echo Approx: %SAVED_MB% MB
echo.

pause
