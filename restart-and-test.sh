#!/bin/bash

# Quick restart and test
echo "🔄 Restarting cluster with fixes..."

# Kill existing processes
if [ -f "logs/frontend.pid" ]; then
    kill $(cat logs/frontend.pid) 2>/dev/null
    rm -f logs/frontend.pid
fi

if [ -f "logs/backend.pid" ]; then
    kill $(cat logs/backend.pid) 2>/dev/null
    rm -f logs/backend.pid
fi

# Wait for cleanup
sleep 2

# Rebuild
echo "📦 Rebuilding..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo "✅ Build successful"

# Start fresh
echo "🚀 Starting fresh cluster..."
./start-web-demo.sh &
sleep 8

# Test immediately
echo "🧪 Testing API..."
curl -X POST http://localhost:8080/api/chat \
     -H 'Content-Type: application/json' \
     -d '{"message":"Test fix","sessionId":"test"}' | head -c 200

echo ""
echo ""
echo "📋 Check recent logs:"
echo "Frontend (last 10 lines):"
tail -10 logs/frontend.log | grep -E "(WebServer|HTTP|🌐|🎉)"

echo ""
echo "Backend (last 5 lines):"  
tail -5 logs/backend.log | grep -E "(RAG|LLM|🔍|🤖)"
