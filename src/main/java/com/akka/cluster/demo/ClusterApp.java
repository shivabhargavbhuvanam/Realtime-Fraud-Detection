package com.akka.cluster.demo;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.cluster.typed.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.akka.cluster.demo.messages.*;

/**
 * Main application class for Boston Bot Akka Cluster.
 * 
 * Phase 2A: 2-node cluster with actor communication pipeline
 * - frontend: Web server, routing, logging (port 2551) 
 * - backend: RAG search, LLM processing (port 2552)
 */
public class ClusterApp {
    
    // Service keys for cluster-wide actor discovery
    public static final ServiceKey<RAGRequest> RAG_SEARCH_KEY = 
        ServiceKey.create(RAGRequest.class, "rag-search-service");
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("🚀 Boston Bot Cluster - 2-Node Architecture (Phase 2A)");
            System.out.println("java ClusterApp <node-role>");
            System.out.println("");
            System.out.println("Node roles:");
            System.out.println("  frontend - Web UI, routing, logging (port 2551)");
            System.out.println("  backend  - RAG search, LLM processing (port 2552)");
            System.out.println("");
            System.out.println("Start frontend node first, then backend node.");
            System.exit(1);
        }
        
        String nodeRole = args[0];
        
        switch (nodeRole) {
            case "frontend":
                startFrontendNode(2551, 8080);
                break;
            case "backend":
                startBackendNode(2552);
                break;
            default:
                System.out.println("❌ Invalid node role: " + nodeRole);
                System.exit(1);
        }
    }
    
    private static void startFrontendNode(int clusterPort, int httpPort) {
        Config config = createConfig("frontend", clusterPort, httpPort);
        
        ActorSystem<Void> system = ActorSystem.create(
            getFrontendNodeBehavior(httpPort), 
            "ClusterSystem", 
            config
        );
        
        System.out.println("🌐 FRONTEND node starting...");
        registerShutdownHook(system, "frontend node");
    }
    
    private static void startBackendNode(int clusterPort) {
        Config config = createConfig("backend", clusterPort, 0);
        
        ActorSystem<Void> system = ActorSystem.create(
            getBackendNodeBehavior(), 
            "ClusterSystem", 
            config
        );
        
        System.out.println("🔧 BACKEND node starting...");
        registerShutdownHook(system, "backend node");
    }
    
    /**
     * Frontend node behavior - Web server + routing + logging
     */
    private static Behavior<Void> getFrontendNodeBehavior(int httpPort) {
        return Behaviors.setup(context -> {
            Cluster cluster = Cluster.get(context.getSystem());
            
            // Create cluster listener
            context.spawn(ClusterListener.create(), "cluster-listener");
            
            // Create LoggerActor
            ActorRef<LogEvent> logger = context.spawn(LoggerActor.create(), "logger");
            
            // Create QueryRouterActor
            ActorRef<QueryRouterActor.Command> queryRouter = context.spawn(
                QueryRouterActor.create(logger), 
                "query-router"
            );
            
            // Create and start WebServerActor
            ActorRef<WebServerActor.Command> webServer = 
                context.spawn(WebServerActor.create(queryRouter), "web-server");
            webServer.tell(new WebServerActor.StartServer("0.0.0.0", httpPort));
            
            System.out.println("✅ Frontend actors created successfully");
            
            return Behaviors.empty();
        });
    }
    
    /**
     * Backend node behavior - RAG search + LLM processing
     */
    private static Behavior<Void> getBackendNodeBehavior() {
        return Behaviors.setup(context -> {
            Cluster cluster = Cluster.get(context.getSystem());
            
            // Create cluster listener
            context.spawn(ClusterListener.create(), "cluster-listener");
            
            // Create LLMActor
            ActorRef<LLMRequest> llmActor = context.spawn(LLMActor.create(), "llm-actor");
            
            // Create RAGSearchActor
            ActorRef<RAGRequest> ragSearchActor = context.spawn(
                RAGSearchActor.create(llmActor), 
                "rag-search"
            );
            
            // Register with cluster receptionist
            context.getSystem().receptionist().tell(
                Receptionist.register(RAG_SEARCH_KEY, ragSearchActor)
            );
            
            System.out.println("✅ Backend actors created successfully");
            
            return Behaviors.empty();
        });
    }
    
    private static Config createConfig(String nodeRole, int clusterPort, int httpPort) {
        String configString = String.format("""
            akka {
              actor {
                provider = "cluster"
                serialization-bindings {
                  "com.akka.cluster.demo.CborSerializable" = jackson-cbor
                }
              }
              remote.artery {
                canonical {
                  hostname = "127.0.0.1"
                  port = %d
                }
              }
              cluster {
                seed-nodes = ["akka://ClusterSystem@127.0.0.1:2551"]
                roles = ["%s"]
                downing-provider-class = "akka.cluster.sbr.SplitBrainResolverProvider"
                split-brain-resolver {
                  active-strategy = keep-majority
                  stable-after = 7s
                }
                log-info = on
                min-nr-of-members = 2
              }
              http {
                server {
                  request-timeout = 30s
                  bind-timeout = 10s
                }
              }
              loggers = ["akka.event.slf4j.Slf4jLogger"]
              loglevel = "INFO"
            }
            """, clusterPort, nodeRole);
            
        return ConfigFactory.parseString(configString)
                .withFallback(ConfigFactory.load());
    }
    
    private static void registerShutdownHook(ActorSystem<Void> system, String nodeDescription) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🛑 Shutting down " + nodeDescription + "...");
            system.terminate();
        }));
    }
}
