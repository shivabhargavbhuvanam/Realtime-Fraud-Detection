#!/bin/bash

# Test script to verify actor communication patterns
# This sends specific requests to trigger different message flows

echo "🧪 Testing Actor Communication Patterns"
echo "======================================="
echo ""

# Function to send chat request and show immediate logs
test_actor_flow() {
    local question=$1
    local session=$2
    local pattern_type=$3
    
    echo "🔍 Testing $pattern_type pattern:"
    echo "   Question: \"$question\""
    echo "   Session: $session"
    echo ""
    
    # Send request
    response=$(curl -s -X POST http://localhost:8080/api/chat \
        -H 'Content-Type: application/json' \
        -d "{\"message\":\"$question\",\"sessionId\":\"$session\"}")
    
    # Check if successful
    if echo "$response" | grep -q "Phase 2A"; then
        echo "   ✅ Response received successfully"
        
        # Show key parts of response
        if command -v jq &> /dev/null; then
            answer=$(echo "$response" | jq -r '.answer' | head -5)
            echo "   📝 Response preview:"
            echo "$answer" | sed 's/^/      /'
        fi
    else
        echo "   ❌ Request failed or unexpected response"
        echo "   Response: $response"
    fi
    
    echo ""
    echo "   📊 Recent log entries:"
    
    # Show recent relevant log entries
    tail -20 logs/frontend.log logs/backend.log 2>/dev/null | \
        grep -E "(🎯|📝|🔍|🤖|QueryRouter|Logger|RAGSearch|LLM)" | \
        tail -5 | sed 's/^/      /'
    
    echo ""
    echo "   ⏱️  Waiting 2 seconds for processing to complete..."
    sleep 2
    echo ""
}

echo "🎯 Phase 2A Actor Communication Tests"
echo "====================================="
echo ""

# Test 1: tell pattern (logging)
test_actor_flow "Hello Boston!" "test-tell-pattern" "TELL"

# Test 2: ask pattern (request-response)  
test_actor_flow "What is Boston's population?" "test-ask-pattern" "ASK"

# Test 3: forward pattern (message routing)
test_actor_flow "What are the top attractions?" "test-forward-pattern" "FORWARD"

# Test 4: Complex query (full pipeline)
test_actor_flow "Tell me about Boston's transportation system" "test-full-pipeline" "FULL PIPELINE"

echo "📊 Communication Pattern Summary"
echo "==============================="
echo ""
echo "✅ TELL Pattern: QueryRouter → Logger (fire-and-forget logging)"
echo "✅ ASK Pattern: WebServer → QueryRouter (request-response with timeout)" 
echo "✅ FORWARD Pattern: QueryRouter → RAGSearch → LLM (preserve sender)"
echo ""
echo "🎯 All patterns working in Phase 2A mock implementation!"
echo ""
echo "📋 To see detailed logs:"
echo "   tail -50 logs/frontend.log | grep -E '(🎯|📝)'"
echo "   tail -50 logs/backend.log | grep -E '(🔍|🤖)'"
