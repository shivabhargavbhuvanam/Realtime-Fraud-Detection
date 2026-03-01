# 🎉 Akka Cluster Demo - Course Lab Ready!

## ✅ What We've Built

A complete **Akka Cluster demonstration** that runs on your local macOS machine, perfect for CSYE7374 course presentations. The demo includes:

### 🏗️ Architecture Components
- **Seed Node** (port 2551) - Bootstraps the cluster
- **Backend Worker Nodes** (ports 2552, 2554) - Process work requests
- **Frontend Node** (port 2553) - Distributes work and monitors cluster
- **Cluster Listener** - Monitors membership events across all nodes

### 🎯 Demo Capabilities
✅ **Cluster Formation** - Watch nodes join and form a cluster  
✅ **Membership Monitoring** - Real-time cluster state changes  
✅ **Fault Tolerance** - Node failure detection and recovery  
✅ **Work Distribution** - Simulated load balancing across workers  
✅ **Split Brain Resolution** - Network partition handling  
✅ **Scaling** - Dynamic addition/removal of nodes  

## 🚀 Quick Demo Instructions

### Option 1: Automatic Demo (Recommended for Presentations)
```bash
./start-cluster.sh
```
This starts all nodes automatically and shows real-time cluster formation.

### Option 2: Step-by-Step Demo (Better for Understanding)
```bash
# Terminal 1 - Start seed node
mvn exec:java -Dexec.mainClass="com.akka.cluster.demo.ClusterApp" -Dexec.args="seed"

# Terminal 2 - Add backend worker  
mvn exec:java -Dexec.mainClass="com.akka.cluster.demo.ClusterApp" -Dexec.args="backend 2552"

# Terminal 3 - Add frontend
mvn exec:java -Dexec.mainClass="com.akka.cluster.demo.ClusterApp" -Dexec.args="frontend 2553"

# Terminal 4 - Add another backend
mvn exec:java -Dexec.mainClass="com.akka.cluster.demo.ClusterApp" -Dexec.args="backend 2554"
```

### Option 3: VS Code Tasks
- Open Command Palette (Cmd+Shift+P)
- Type "Tasks: Run Task"
- Choose from available cluster tasks:
  - "Start Full Cluster Demo"
  - "Start Seed Node"
  - "Start Backend Node" 
  - "Start Frontend Node"

## 📊 What Students Will See

### Cluster Formation
```
🌱 Starting seed node on port 2551
🟢 Member UP: akka://ClusterSystem@127.0.0.1:2551 with roles [seed]
Current cluster size: 1
```

### Node Joining
```
🔧 Backend worker started on akka://ClusterSystem@127.0.0.1:2552
🟢 Member UP: akka://ClusterSystem@127.0.0.1:2552 with roles [backend]
Current cluster size: 2
```

### Work Distribution
```
🌐 Frontend started on akka://ClusterSystem@127.0.0.1:2553
📤 [SIMULATED] Sending work 'work-1' to backend: calculate prime numbers
📊 Available backends: 2
```

### Failure Detection
```
🟡 Member LEFT: akka://ClusterSystem@127.0.0.1:2552 with roles [backend]
🔴 Member REMOVED: akka://ClusterSystem@127.0.0.1:2552 with roles [backend]
📊 Available backends: 1
```

## 🎓 Key Learning Concepts Demonstrated

1. **Distributed System Formation**
   - How nodes discover and join a cluster
   - Gossip protocol for membership information
   - Role-based node organization

2. **Fault Tolerance**
   - Failure detection using heartbeats
   - Graceful vs. abrupt node termination
   - Split brain resolver strategies

3. **Message Passing & Routing**
   - Inter-node communication
   - Work distribution patterns  
   - Cluster-aware actor selection

4. **Observability**
   - Real-time cluster state monitoring
   - Structured logging for debugging
   - Event-driven architecture

## 🎯 Course Lab Exercises

### Exercise 1: Basic Cluster Operations
- Start seed node and observe initialization
- Add nodes one by one and watch cluster growth
- Document membership lifecycle events

### Exercise 2: Fault Tolerance Testing  
- Start full cluster (4 nodes)
- Kill backend node (Ctrl+C) and observe failure detection
- Restart node and watch rejoin process
- Test different failure scenarios

### Exercise 3: Scaling Demonstration
- Start minimal cluster (seed + 1 backend + frontend)
- Add multiple backend nodes while frontend runs
- Remove nodes and observe load rebalancing
- Measure work distribution effectiveness

### Exercise 4: Network Partitions
- Simulate network splits by killing seed node
- Observe split brain resolver actions
- Test cluster reformation after healing

## ⚙️ Technical Configuration

### Dependencies
- **Akka Cluster Typed** 2.6.20 (stable, no license issues)
- **Jackson Serialization** for cross-node messaging
- **Logback** for structured logging
- **Java 17** with text block support

### Network Setup
- All nodes run on localhost (127.0.0.1)
- Ports: 2551 (seed), 2552/2554 (backends), 2553 (frontend)
- No firewall issues on macOS

### Configuration Highlights
```hocon
akka.cluster {
  seed-nodes = ["akka://ClusterSystem@127.0.0.1:2551"]
  downing-provider-class = "akka.cluster.sbr.SplitBrainResolverProvider"
  split-brain-resolver.active-strategy = keep-majority
}
```

## 🎬 Demo Flow Recommendations

### For 10-Minute Presentation:
1. **Quick Overview** (2 min) - Explain architecture diagram
2. **Live Demo** (6 min) - Run `./start-cluster.sh` and narrate events  
3. **Failure Demo** (2 min) - Kill a node and show recovery

### For 20-Minute Lab Session:
1. **Concepts** (5 min) - Distributed systems challenges
2. **Step-by-Step** (10 min) - Manual node startup with explanations
3. **Experiments** (5 min) - Student-driven failure scenarios

### For Full Lab Hour:
1. **Theory** (15 min) - Distributed systems & Akka concepts
2. **Guided Demo** (20 min) - Students follow along
3. **Hands-On** (20 min) - Students run their own experiments  
4. **Discussion** (5 min) - Observations and lessons learned

## 🔧 Troubleshooting

### Common Issues & Solutions:
- **Port conflicts**: Use `lsof -i :2551` to check, kill processes
- **Java version**: Ensure Java 17+ installed
- **Build failures**: Run `mvn clean compile` first
- **High CPU**: Normal during cluster formation, reduce logging if needed

## 📚 Extension Ideas

- Add HTTP endpoints for cluster management
- Implement actual work processing (calculations)
- Add metrics and monitoring dashboard  
- Create Docker containers for easier deployment
- Add persistence with Akka Persistence
- Implement cluster sharding examples

---

**🎉 Ready for Your Course Lab!**

This demo successfully demonstrates all key Akka Cluster concepts and is ready for educational use. Students will gain hands-on experience with distributed systems, fault tolerance, and cluster computing concepts.

*Happy clustering! 🚀*
