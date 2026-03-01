#!/bin/bash

# Diagnose the internal server error
echo "🔍 Diagnosing Internal Server Error"
echo "==================================="
echo ""

# Check if web server started properly
echo "📊 Checking web server status..."
if curl -s http://localhost:8080/health > /dev/null; then
    echo "✅ Web server is responding to health checks"
    health_response=$(curl -s http://localhost:8080/health)
    echo "   Health response: $health_response"
else
    echo "❌ Web server is not responding"
    exit 1
fi

echo ""

# Check frontend logs for HTTP server status
echo "📋 Frontend logs - HTTP server initialization:"
grep -E "(HTTP|WebServer|server|🌐|🎉)" logs/frontend.log | tail -10

echo ""

# Check for any error messages in logs
echo "🚨 Checking for errors in logs:"
echo ""
echo "Frontend errors:"
grep -i "error\|exception\|failed" logs/frontend.log | tail -5 || echo "   No errors found in frontend log"

echo ""
echo "Backend errors:"
grep -i "error\|exception\|failed" logs/backend.log | tail -5 || echo "   No errors found in backend log"

echo ""

# Test API endpoint with verbose output
echo "🧪 Testing API endpoint with verbose output:"
echo ""
curl -v -X POST http://localhost:8080/api/chat \
     -H 'Content-Type: application/json' \
     -d '{"message":"Debug test","sessionId":"debug"}' 2>&1 | head -20

echo ""
echo ""
echo "💡 If you see connection refused, the WebServerActor HTTP binding failed"
echo "💡 If you see 500 error, there's an exception in the actor processing"
echo "💡 Check the logs above for specific error details"
