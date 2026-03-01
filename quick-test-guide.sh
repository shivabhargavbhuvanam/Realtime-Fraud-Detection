#!/bin/bash

echo "🧪 Phase 1 - Quick Testing Commands"
echo "==================================="
echo ""

echo "📋 Prerequisites Check:"
echo "java -version"
echo "mvn -version"
echo ""

echo "📦 Build Test:"
echo "mvn clean compile"
echo ""

echo "🚀 Quick Start:"
echo "./start-web-demo.sh"
echo ""

echo "🌐 Manual Start:"
echo "Terminal 1: mvn exec:java -Dexec.args=\"seed\""
echo "Terminal 2: mvn exec:java -Dexec.args=\"web 8080\""
echo ""

echo "🔍 API Tests:"
echo "curl http://localhost:8080/health"
echo "curl -X POST http://localhost:8080/api/chat -H 'Content-Type: application/json' -d '{\"message\": \"test\", \"sessionId\": \"test\"}'"
echo ""

echo "🌐 Browser Test:"
echo "open http://localhost:8080"
echo ""

echo "🛑 Shutdown:"
echo "Ctrl+C (in each terminal)"
echo ""

echo "📊 Log Locations:"
echo "- Quick start: ./logs/seed.log, ./logs/web.log"  
echo "- Manual start: Check terminal outputs"
echo ""

echo "✅ Success Indicators:"
echo "- Cluster size: 2 nodes"
echo "- Web server: http://localhost:8080 loads"
echo "- Chat: Can send/receive messages"
echo "- API: Returns JSON responses"
echo ""

echo "❌ Common Issues:"
echo "- Port 8080 in use: Use different port or kill other processes"
echo "- Java version: Ensure Java 17+"
echo "- Build fails: Check dependencies with 'mvn dependency:tree'"
echo ""

echo "🎯 Full Testing Guide: See README-BOSTON-BOT.md"