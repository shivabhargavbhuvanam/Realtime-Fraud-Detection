#!/bin/bash

# Boston Bot - Quick 2-Node Demo Startup
# Starts frontend (seed + web) and backend (RAG + LLM) nodes

echo "🚀 Boston Bot - 2-Node Quick Demo"
echo "================================="
echo ""

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed or not in PATH"
    echo "Please install Maven first"
    exit 1
fi

# Clean and compile
echo "📦 Building project..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo "✅ Build successful"
echo ""

# Function to start a node in the background
start_node() {
    local node_role=$1
    local log_file="logs/${node_role}.log"
    
    mkdir -p logs
    
    echo "🔧 Starting $node_role node..."
    
    mvn exec:java -Dexec.mainClass="com.akka.cluster.demo.ClusterApp" \
                  -Dexec.args="$node_role" \
                  > "$log_file" 2>&1 &
    
    local pid=$!
    echo $pid > "logs/${node_role}.pid"
    
    return $pid
}

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local url=$2
    local max_attempts=20
    local attempt=0
    
    echo "⏳ Waiting for $service_name to be ready..."
    
    while [ $attempt -lt $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo "✅ $service_name is ready!"
            return 0
        fi
        
        attempt=$((attempt + 1))
        sleep 1
        echo -n "."
    done
    
    echo ""
    echo "❌ $service_name failed to start within timeout"
    return 1
}

# Cleanup function
cleanup() {
    echo ""
    echo "🧹 Cleaning up..."
    
    # Kill all processes
    if [ -f "logs/frontend.pid" ]; then
        kill $(cat logs/frontend.pid) 2>/dev/null
        rm -f logs/frontend.pid
    fi
    
    if [ -f "logs/backend.pid" ]; then
        kill $(cat logs/backend.pid) 2>/dev/null
        rm -f logs/backend.pid
    fi
    
    echo "👋 2-Node cluster shutdown complete!"
}

# Set trap for cleanup on exit
trap cleanup EXIT INT TERM

# Start frontend node first (acts as seed)
start_node "frontend"
sleep 4

# Start backend node  
start_node "backend"
sleep 3

# Wait for web service to be ready
if wait_for_service "Web Server" "http://localhost:8080"; then
    echo ""
    echo "🎉 2-Node Boston Bot Cluster Ready!"
    echo "=================================="
    echo "🌐 Frontend Node:  127.0.0.1:2551 [frontend] (seed + web)"
    echo "🔧 Backend Node:   127.0.0.1:2552 [backend] (RAG + LLM)"
    echo ""
    echo "🌐 Chat Interface: http://localhost:8080"
    echo "🔗 API Endpoint:   http://localhost:8080/api/chat"
    echo "📊 Logs Directory: ./logs/"
    echo ""
    echo "💡 Test the cluster:"
    echo "   curl -X POST http://localhost:8080/api/chat \\"
    echo "        -H 'Content-Type: application/json' \\"
    echo "        -d '{\"message\":\"Hello Boston!\",\"sessionId\":\"test\"}'"
    echo ""
    echo "🛑 Press Ctrl+C to stop all services"
    echo ""
    
    # Keep script running to maintain services
    while true; do
        sleep 1
    done
else
    echo "❌ Failed to start services"
    exit 1
fi
