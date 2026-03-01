#!/bin/bash

# Make all shell scripts executable
# Run this after restructuring to 2-node setup

echo "⚡ Making scripts executable..."

chmod +x start-cluster.sh
chmod +x start-node.sh  
chmod +x start-web-demo.sh
chmod +x test-2node-setup.sh
chmod +x make-executable.sh
chmod +x quick-test-guide.sh
chmod +x run-automated-tests.sh
chmod +x setup-assets.sh
chmod +x test-integration.sh

echo "✅ All scripts are now executable!"
echo ""
echo "🎯 2-Node cluster is ready to test:"
echo "   ./start-web-demo.sh     # Start both nodes"
echo "   ./start-node.sh         # Start individual nodes"  
echo "   ./test-2node-setup.sh   # Test cluster formation"
