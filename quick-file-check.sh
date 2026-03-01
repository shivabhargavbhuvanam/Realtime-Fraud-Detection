#!/bin/bash

# Quick file check before compilation test

echo "🔍 Checking file integrity..."
echo ""

# Check WebServerActor
echo "📄 WebServerActor.java:"
if grep -q "public class WebServerActor" /Users/shivabhargavbhuvanam/Desktop/akka-clusters/src/main/java/com/akka/cluster/demo/WebServerActor.java; then
    echo "   ✅ Contains correct WebServerActor class"
else
    echo "   ❌ Missing WebServerActor class"
fi

if grep -q "public class QueryRouterActor" /Users/shivabhargavbhuvanam/Desktop/akka-clusters/src/main/java/com/akka/cluster/demo/WebServerActor.java; then
    echo "   ❌ Still contains QueryRouterActor (should not)"
else
    echo "   ✅ Does not contain QueryRouterActor"
fi

# Check QueryRouterActor
echo ""
echo "📄 QueryRouterActor.java:"
if [ -f "/Users/shivabhargavbhuvanam/Desktop/akka-clusters/src/main/java/com/akka/cluster/demo/QueryRouterActor.java" ]; then
    echo "   ✅ File exists"
    if grep -q "public class QueryRouterActor" /Users/shivabhargavbhuvanam/Desktop/akka-clusters/src/main/java/com/akka/cluster/demo/QueryRouterActor.java; then
        echo "   ✅ Contains QueryRouterActor class"
    else
        echo "   ❌ Missing QueryRouterActor class"
    fi
else
    echo "   ❌ File missing"
fi

# Check LLMActor imports
echo ""
echo "📄 LLMActor.java:"
if grep -q "import java.util.ArrayList" /Users/shivabhargavbhuvanam/Desktop/akka-clusters/src/main/java/com/akka/cluster/demo/LLMActor.java; then
    echo "   ✅ ArrayList import present"
else
    echo "   ❌ Missing ArrayList import"
fi

echo ""
echo "🔨 Now testing compilation..."
cd /Users/shivabhargavbhuvanam/Desktop/akka-clusters
mvn clean compile -q

if [ $? -eq 0 ]; then
    echo "✅ BUILD SUCCESSFUL!"
else
    echo "❌ Build still has issues"
fi
