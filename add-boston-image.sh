#!/bin/bash

echo "🖼️  Adding Boston cityscape background image..."

# Copy the image from the original location
if [ -f "/Users/lohith/Documents/InteliJ/bostonbot/src/main/resources/2.png" ]; then
    cp "/Users/lohith/Documents/InteliJ/bostonbot/src/main/resources/2.png" "src/main/resources/2.png"
    echo "✅ Boston cityscape image copied successfully!"
    
    # Verify the file was copied
    if [ -f "src/main/resources/2.png" ]; then
        file_size=$(ls -lh "src/main/resources/2.png" | awk '{print $5}')
        echo "✅ Image verified: 2.png ($file_size)"
    else
        echo "❌ Image copy failed"
    fi
else
    echo "❌ Original image not found at: /Users/lohith/Documents/InteliJ/bostonbot/src/main/resources/2.png"
    echo "Please check the path or copy the image manually"
fi

echo ""
echo "🎨 Updated styles.css to use the Boston cityscape background"
echo ""
echo "Next steps:"
echo "1. mvn clean compile"
echo "2. Start the system (seed + web nodes)"
echo "3. Open http://localhost:8080"
echo "4. Enjoy your beautiful Boston Bot interface! 🏙️"