#!/bin/bash

# Monitor actor communications in real-time
# Run this in a separate terminal while testing

echo "🔍 Real-time Actor Communication Monitor"
echo "========================================"
echo ""
echo "Monitoring logs for actor message patterns..."
echo "Send chat requests to see the flow!"
echo ""

# Function to colorize output
colorize() {
    local color=$1
    local text=$2
    echo -e "\033[${color}m${text}\033[0m"
}

# Monitor both log files simultaneously
tail -f logs/frontend.log logs/backend.log | while read line; do
    # Frontend node activities
    if echo "$line" | grep -q "🎯.*QueryRouter"; then
        colorize "36" "FRONTEND → $line"
    elif echo "$line" | grep -q "📝.*Logger"; then
        colorize "33" "LOGGING  → $line"
    elif echo "$line" | grep -q "🌐.*WebServer"; then
        colorize "32" "HTTP     → $line"
    
    # Backend node activities
    elif echo "$line" | grep -q "🔍.*RAGSearch"; then
        colorize "35" "BACKEND  → $line"
    elif echo "$line" | grep -q "🤖.*LLM"; then
        colorize "34" "LLM      → $line"
    
    # Cluster events
    elif echo "$line" | grep -q "Member UP\|Member DOWN"; then
        colorize "31" "CLUSTER  → $line"
    
    # Show important events
    elif echo "$line" | grep -E "(tell|ask|forward|TELL|ASK|FORWARD)"; then
        colorize "1;37" "PATTERN  → $line"
    fi
done
