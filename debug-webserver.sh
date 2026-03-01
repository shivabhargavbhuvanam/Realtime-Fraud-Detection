#!/bin/bash

# Debug the WebServerActor issue
echo "🔍 Debugging WebServerActor Issue"
echo "================================="

# Stop current processes
echo "🛑 Stopping current processes..."
pkill -f "ClusterApp" 2>/dev/null
sleep 2

# Check for the HTTP binding message specifically
echo "🔍 Looking for HTTP server startup in logs..."

# Start just frontend to isolate the issue
echo "🌐 Starting frontend node only..."
mvn exec:java -Dexec.mainClass="com.akka.cluster.demo.ClusterApp" -Dexec.args="frontend" > debug-frontend.log 2>&1 &
FRONTEND_PID=$!

# Wait and check logs
sleep 5

echo "📋 Frontend debug log:"
cat debug-frontend.log | grep -E "(WebServer|HTTP|StartServer|🌐|🎉)" || echo "No HTTP server messages found"

echo ""
echo "📋 All frontend messages:"
cat debug-frontend.log | tail -20

# Cleanup
kill $FRONTEND_PID 2>/dev/null
