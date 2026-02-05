#!/bin/bash
# Image optimization script for Pyera Finance
# This script converts large JPG images to WebP format for APK size reduction

echo "=========================================="
echo "Pyera Finance - Image Optimization Script"
echo "=========================================="

RES_DIR="app/src/main/res"
TOTAL_SAVED=0

# Function to convert JPG to WebP
convert_jpg_to_webp() {
    local input_file=$1
    local output_file="${input_file%.jpg}.webp"
    local filename=$(basename "$input_file")
    
    # Check if WebP already exists
    if [ -f "$output_file" ]; then
        echo "  ⚠ WebP already exists: $(basename $output_file) - skipping"
        return 0
    fi
    
    if command -v cwebp &> /dev/null; then
        # Get original file size
        local original_size=$(stat -f%z "$input_file" 2>/dev/null || stat -c%s "$input_file" 2>/dev/null)
        
        # Convert with quality 85 (good balance between quality and size)
        cwebp -q 85 "$input_file" -o "$output_file" -quiet
        
        if [ $? -eq 0 ]; then
            local new_size=$(stat -f%z "$output_file" 2>/dev/null || stat -c%s "$output_file" 2>/dev/null)
            local saved=$((original_size - new_size))
            local saved_kb=$((saved / 1024))
            local percent=$((saved * 100 / original_size))
            
            echo "  ✓ Converted: $filename"
            echo "    Original: $((original_size / 1024))KB → New: $((new_size / 1024))KB (Saved: ${saved_kb}KB, ${percent}%)"
            TOTAL_SAVED=$((TOTAL_SAVED + saved))
        else
            echo "  ✗ Failed to convert: $filename"
        fi
    else
        echo "  ⚠ cwebp not found. Install WebP tools:"
        echo "    macOS: brew install webp"
        echo "    Ubuntu/Debian: sudo apt-get install webp"
        echo "    Windows: https://developers.google.com/speed/webp/download"
        return 1
    fi
}

# Function to check mipmap icons
check_mipmap_icons() {
    echo ""
    echo "Checking mipmap icons..."
    
    for dir in $RES_DIR/mipmap-*; do
        if [ -d "$dir" ]; then
            local png_count=$(find "$dir" -name "*.png" | wc -l)
            local webp_count=$(find "$dir" -name "*.webp" | wc -l)
            
            if [ $png_count -gt 0 ]; then
                echo "  📁 $(basename $dir): $png_count PNG files, $webp_count WebP files"
            fi
        fi
    done
}

echo ""
echo "Step 1: Finding large JPG files (>100KB)..."
echo "------------------------------------------"

# Find and convert large JPGs
jpg_count=0
while IFS= read -r file; do
    if [ $jpg_count -eq 0 ]; then
        echo "Found JPG files to convert:"
    fi
    convert_jpg_to_webp "$file"
    jpg_count=$((jpg_count + 1))
done < <(find $RES_DIR -name "*.jpg" -size +100k)

if [ $jpg_count -eq 0 ]; then
    echo "  No large JPG files found (>100KB)"
else
    echo ""
    echo "Converted $jpg_count JPG files"
fi

echo ""
echo "Step 2: Checking mipmap icons..."
echo "------------------------------------------"
check_mipmap_icons

echo ""
echo "=========================================="
echo "Optimization Summary"
echo "=========================================="
if [ $TOTAL_SAVED -gt 0 ]; then
    echo "Total space saved: $((TOTAL_SAVED / 1024))KB ($((TOTAL_SAVED / 1024 / 1024))MB)"
else
    echo "No conversions performed"
fi

echo ""
echo "Next Steps:"
echo "  1. Review converted WebP files"
echo "  2. Update code references from .jpg to .webp"
echo "  3. Delete original JPG files after verification"
echo "  4. Consider removing unused resources (see UNUSED_RESOURCES.md)"
echo ""
echo "Image optimization complete!"
