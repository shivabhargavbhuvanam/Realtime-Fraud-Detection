#!/bin/bash

# Boston Bot - 2-Node Cluster Startup
# Starts frontend node (seed + web) and backend node (RAG + LLM)

echo "🚀 Boston Bot - 2-Node Cluster Startup"
echo "======================================"
echo ""

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed or not in PATH"
    echo "Please install Maven first"
    exit 1
fi

# Function to kill all background processes on exit
cleanup() {
    echo ""
    echo "🛑 Shutting down cluster nodes..."
    
    # Kill processes if PID files exist
    if [ -f "logs/frontend.pid" ]; then
        kill $(cat logs/frontend.pid) 2>/dev/null
        rm -f logs/frontend.pid
    fi
    
    if [ -f "logs/backend.pid" ]; then
        kill $(cat logs/backend.pid) 2>/dev/null  
        rm -f logs/backend.pid
    fi
    
    echo "👋 Cluster shutdown complete!"
    exit
}

# Set trap to cleanup on script exit
trap cleanup SIGINT SIGTERM EXIT

# Build the project first
echo "📦 Building project..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build successful!"
echo ""

# Create logs directory
mkdir -p logs

# Start frontend node (seed + web server)
echo "🌐 Starting FRONTEND node (seed + web server)..."
mvn exec:java -Dexec.mainClass="com.akka.cluster.demo.ClusterApp" \
              -Dexec.args="frontend" \
              > logs/frontend.log 2>&1 &
FRONTEND_PID=$!
echo $FRONTEND_PID > logs/frontend.pid

# Wait for frontend to initialize
sleep 4

# Start backend node (RAG + LLM processing)  
echo "🔧 Starting BACKEND node (RAG + LLM processing)..."
mvn exec:java -Dexec.mainClass="com.akka.cluster.demo.ClusterApp" \
              -Dexec.args="backend" \
              > logs/backend.log 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > logs/backend.pid

# Wait for backend to join cluster
sleep 3

echo ""
echo "🎉 2-Node Cluster Started Successfully!"
echo "======================================"
echo ""
echo "📊 Cluster Configuration:"
echo "  🌐 Frontend Node:  127.0.0.1:2551 [frontend] (seed)"
echo "  🔧 Backend Node:   127.0.0.1:2552 [backend]"
echo ""
echo "🌐 Web Interface:    http://localhost:8080"
echo "🔗 API Endpoint:     http://localhost:8080/api/chat"
echo "📋 Logs Directory:   ./logs/"
echo ""
echo "💡 Frontend node acts as both seed and web server"
echo "💡 Backend node handles RAG processing and LLM calls"
echo "💡 Check logs/frontend.log and logs/backend.log for details"
echo ""
echo "🛑 Press Ctrl+C to shutdown both nodes cleanly"
echo ""

# Function to check if service is ready
wait_for_service() {
    local service_name=$1
    local url=$2
    local max_attempts=15
    local attempt=0
    
    echo "⏳ Waiting for $service_name..."
    
    while [ $attempt -lt $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo "✅ $service_name is ready!"
            return 0
        fi
        
        attempt=$((attempt + 1))
        sleep 1
        printf "."
    done
    
    echo ""
    echo "⚠️  $service_name may still be starting (check logs/frontend.log)"
    return 1
}

# Check if web server is ready
wait_for_service "Web Server" "http://localhost:8080"

echo ""
echo "🎯 Ready to test! Try:"
echo "   curl -X POST http://localhost:8080/api/chat \\"
echo "        -H 'Content-Type: application/json' \\"
echo "        -d '{\"message\":\"Hello Boston!\",\"sessionId\":\"test\"}'"
echo ""

# Keep script running to maintain services
while true; do
    sleep 1
done
