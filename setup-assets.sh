#!/bin/bash

echo "🎨 Setting up Boston Bot assets..."

# Make startup script executable
chmod +x start-web-demo.sh
echo "✅ Made start-web-demo.sh executable"

# Check for Boston cityscape image
if [ -f "/Users/lohith/Documents/InteliJ/bostonbot/src/main/resources/2.png" ]; then
    echo "🖼️  Copying Boston cityscape image..."
    cp "/Users/lohith/Documents/InteliJ/bostonbot/src/main/resources/2.png" "src/main/resources/2.png"
    echo "✅ Boston cityscape image copied"
else
    echo "⚠️  Boston cityscape image not found at expected location"
    echo "   Expected: /Users/lohith/Documents/InteliJ/bostonbot/src/main/resources/2.png"
    echo "   Please copy it manually to: src/main/resources/2.png"
fi

# Create logs directory
mkdir -p logs
echo "✅ Created logs directory"

echo ""
echo "🎉 Setup complete! You can now run:"
echo "   ./start-web-demo.sh"