# Fraud-Detection - Akka Cluster Chatbot

A distributed Fraud detection Chatbot built with Akka Cluster, featuring a modern web interface and AI-powered responses.

## 🚀 Quick Start (1-Minute Demo)

### Prerequisites
- Java 17+
- Maven 3.6+

### Instant Demo
```bash
# Make startup script executable
chmod +x start-web-demo.sh

# Start the demo (starts seed node + web interface)
./start-web-demo.sh
```

Then open: http://localhost:8080

## 🏗️ Architecture Overview

```
Node 1 (Seed)     Node 2 (Web Frontend)
┌─────────────┐   ┌──────────────────┐
│ Port 2551   │◄──│ Cluster: 2553    │
│ ClusterSeed │   │ HTTP: 8080       │
│             │   │ WebServerActor   │
└─────────────┘   │ + Chat UI        │
                  └──────────────────┘
```

## 📋 Manual Startup (Step by Step)

### Step 1: Build Project
```bash
mvn clean compile
```

### Step 2: Start Seed Node (Terminal 1)
```bash
mvn exec:java -Dexec.mainClass="com.akka.cluster.demo.ClusterApp" -Dexec.args="seed"
```
Wait for: "🌱 Seed node started"

### Step 3: Start Web Frontend (Terminal 2) 
```bash
mvn exec:java -Dexec.mainClass="com.akka.cluster.demo.ClusterApp" -Dexec.args="web 8080"
```
Wait for: "🌐 Web server running at http://0.0.0.0:8080"

### Step 4: Test the Interface
Open: http://localhost:8080

## 🎯 Current Features (Phase 1)

### ✅ Implemented
- **Akka Cluster**: 2-node distributed system
- **Web Interface**: Modern chat UI with animations
- **HTTP API**: `/api/chat` endpoint for chat requests
- **Static File Serving**: HTML, CSS, JavaScript, images
- **Session Management**: Browser-side conversation history
- **Cluster Integration**: WebServerActor communicates with cluster
- **Error Handling**: Graceful error handling and logging

### 🔄 Current Functionality
1. **UI Features**: 
   - Real-time chat interface
   - Message history persistence
   - Typing indicators
   - Keyboard shortcuts (Ctrl+Enter to send)
   - Clear conversation button

2. **Backend Features**:
   - Akka HTTP server with WebServerActor
   - Cluster membership and monitoring
   - JSON request/response handling
   - Static file serving with proper MIME types

3. **Demo Responses**:
   - Currently returns demo responses showing system status
   - Confirms cluster is working
   - Shows session management

### 🧪 Test the Current System

**Try these messages:**
- "Hello, tell me about Boston!"
- "What can you help me with?"
- "How does your system work?"

**Expected Response Format:**
```
Hello! You asked: "Your question here". 
I'm a Boston expert powered by Akka Cluster! 🏙️

Current status: 
✅ Web UI working
✅ Akka Cluster active  
⏳ RAG pipeline coming next!
```

## 🔧 Troubleshooting

### Common Issues

**Port Already in Use:**
```bash
# Check what's using port 8080
lsof -i :8080
# Use different port
mvn exec:java -Dexec.args="web 9090"
```

**Build Errors:**
```bash
# Clean rebuild
mvn clean compile -U
```

**Web Server Not Starting:**
- Check if seed node started first
- Verify Java 17+ is being used
- Check logs in Terminal 1 and 2

### Logs and Debugging

**View cluster status:**
- Check Terminal 1 (seed node) for cluster membership events
- Check Terminal 2 (web node) for HTTP request logs

**Log Locations:**
- When using `start-web-demo.sh`: Check `logs/` directory
- Manual startup: Check terminal output

## 🛠️ Development

### Project Structure
```
src/main/java/com/akka/cluster/demo/
├── ClusterApp.java          # Main application entry
├── WebServerActor.java      # HTTP server + API endpoints  
├── ClusterListener.java     # Cluster events monitoring
├── Frontend.java           # Original frontend logic
└── BackendWorker.java      # Backend worker (for future phases)

src/main/resources/
├── demo.html               # Chat interface
├── styles.css             # UI styling  
├── application.conf       # Akka configuration
└── logback.xml           # Logging configuration
```

### Adding the Boston Image
To add the Boston cityscape background:
1. Copy `2.png` from `/Users/lohith/Documents/InteliJ/bostonbot/src/main/resources/2.png`
2. Place it in `src/main/resources/2.png`
3. The CSS will automatically pick it up

## 📈 Next Development Phases

### Phase 2: Actor Communication Patterns
- [ ] Add QueryRouterActor for orchestration  
- [ ] Implement tell/ask/forward patterns from lab demos
- [ ] Add LoggerActor for audit trail

### Phase 3: Backend Processing Node
- [ ] Create dedicated backend node (port 2552)
- [ ] Add RAGSearchActor and LLMProcessorActor
- [ ] Test cross-node communication

### Phase 4: Vector Database Integration
- [ ] Integrate Qdrant vector database
- [ ] Add document processing pipeline
- [ ] Implement semantic search

### Phase 5: LLM Integration  
- [ ] Add OpenAI API integration
- [ ] Implement RAG (Retrieval-Augmented Generation)
- [ ] Complete end-to-end chatbot pipeline

## 🎯 Success Metrics

**Current Phase (✅ Complete):**
- [x] Web interface loads and displays properly
- [x] Chat form accepts and submits messages  
- [x] API endpoint receives and processes requests
- [x] Akka cluster forms successfully
- [x] WebServerActor integrates with cluster
- [x] Session management works
- [x] Error handling and logging functional

**Overall Project Goals:**
- [ ] All Akka communication patterns demonstrated
- [ ] RAG pipeline with fraud knowledge base
- [ ] OpenAI integration for natural responses
- [ ] Production-ready error handling and fault tolerance
- [ ] Complete course requirements satisfaction

## 🎉 Congratulations!

You now have a **working distributed web application** with:
- Modern chat interface ✅
- Akka Cluster backend ✅  
- HTTP API integration ✅
- Session management ✅
- Professional UI/UX ✅

**Ready for the next integration phase!** 🚀