#!/bin/bash

echo "🎯 Running automated Phase 1 verification tests..."
echo "=================================================="

# Track test results
passed_tests=0
total_tests=0

# Function to run test and track results
run_test() {
    local test_name="$1"
    local test_command="$2"
    local expected_pattern="$3"
    
    total_tests=$((total_tests + 1))
    echo ""
    echo "Test $total_tests: $test_name"
    echo "Command: $test_command"
    
    if eval "$test_command" | grep -q "$expected_pattern"; then
        echo "✅ PASSED"
        passed_tests=$((passed_tests + 1))
    else
        echo "❌ FAILED"
    fi
}

echo ""
echo "📋 Prerequisites Tests:"

run_test "Java Version" "java -version 2>&1" "17\|18\|19\|20\|21"
run_test "Maven Version" "mvn -version 2>&1" "Apache Maven"

echo ""
echo "📦 Build Tests:"

run_test "Maven Clean Compile" "mvn clean compile -q" "BUILD SUCCESS"

echo ""
echo "📁 File Structure Tests:"

if [ -f "src/main/resources/demo.html" ]; then
    echo "✅ demo.html exists"
    passed_tests=$((passed_tests + 1))
else
    echo "❌ demo.html missing"
fi
total_tests=$((total_tests + 1))

if [ -f "src/main/java/com/akka/cluster/demo/WebServerActor.java" ]; then
    echo "✅ WebServerActor.java exists"
    passed_tests=$((passed_tests + 1))
else
    echo "❌ WebServerActor.java missing"
fi
total_tests=$((total_tests + 1))

if [ -f "pom.xml" ]; then
    echo "✅ pom.xml exists"
    passed_tests=$((passed_tests + 1))
else
    echo "❌ pom.xml missing"
fi
total_tests=$((total_tests + 1))

echo ""
echo "🎯 Test Results Summary:"
echo "========================"
echo "Passed: $passed_tests / $total_tests"

if [ $passed_tests -eq $total_tests ]; then
    echo "🎉 ALL TESTS PASSED! Ready for manual testing."
    echo ""
    echo "Next steps:"
    echo "1. ./start-web-demo.sh"
    echo "2. Open http://localhost:8080"
    echo "3. Test chat interface"
else
    echo "⚠️  Some tests failed. Please fix issues before proceeding."
fi

echo ""
echo "For detailed testing: See the complete Phase 1 Testing Guide"