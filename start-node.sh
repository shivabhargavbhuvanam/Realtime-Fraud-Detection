#!/bin/bash

# Individual node startup for 2-node cluster
# Use this to manually start nodes one by one

echo "🚀 Boston Bot - Manual Node Startup"
echo "===================================="
echo ""
echo "Choose which node to start:"
echo "1) Frontend node (seed + web server) - port 2551"
echo "2) Backend node (RAG + LLM) - port 2552"
echo ""

read -p "Enter choice (1-2): " choice

case $choice in
    1)
        echo "🌐 Starting frontend node (seed + web server)..."
        echo "   Cluster port: 2551 (acts as seed)"
        echo "   HTTP port: 8080"
        echo "   Role: [frontend]"
        echo ""
        mvn exec:java -Dexec.mainClass="com.akka.cluster.demo.ClusterApp" -Dexec.args="frontend"
        ;;
    2)
        echo "🔧 Starting backend node (RAG + LLM processing)..."
        echo "   Cluster port: 2552"
        echo "   Role: [backend]"
        echo "   Seed: 127.0.0.1:2551"
        echo ""
        echo "⚠️  Make sure frontend node is running first!"
        echo ""
        mvn exec:java -Dexec.mainClass="com.akka.cluster.demo.ClusterApp" -Dexec.args="backend"
        ;;
    *)
        echo "❌ Invalid choice! Please select 1 or 2."
        exit 1
        ;;
esac
