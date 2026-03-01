package com.akka.cluster.demo;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.http.javadsl.Http;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.Route;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.akka.cluster.demo.messages.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.Directives.*;

/**
 * WebServerActor - Simplified for debugging Phase 2A
 */
public class WebServerActor extends AbstractBehavior<WebServerActor.Command> {

    public interface Command extends CborSerializable {}
    
    public static final class StartServer implements Command {
        public final String host; 
        public final int port;
        
        public StartServer(String host, int port) { 
            this.host = host; 
            this.port = port; 
        }
    }

    public static class ChatRequest {
        public final String message;
        public final String sessionId;
        
        @JsonCreator
        public ChatRequest(
            @JsonProperty("message") String message,
            @JsonProperty("sessionId") String sessionId
        ) {
            this.message = message;
            this.sessionId = sessionId;
        }
    }
    
    public static class ChatResponseDTO {
        public final String answer;
        public final String sessionId;
        
        public ChatResponseDTO(String answer, String sessionId) {
            this.answer = answer;
            this.sessionId = sessionId;
        }
        
        @JsonProperty("answer")
        public String getAnswer() { return answer; }
        
        @JsonProperty("sessionId") 
        public String getSessionId() { return sessionId; }
    }

    private final ActorSystem<Void> system;
    private final ActorRef<QueryRouterActor.Command> queryRouter;

    public static Behavior<Command> create(ActorRef<QueryRouterActor.Command> queryRouter) {
        return Behaviors.setup(context -> new WebServerActor(context, queryRouter));
    }

    private WebServerActor(ActorContext<Command> context, ActorRef<QueryRouterActor.Command> queryRouter) {
        super(context);
        this.system = context.getSystem();
        this.queryRouter = queryRouter;
        
        System.out.println("🌐 WebServerActor constructed successfully");
        getContext().getLog().info("🌐 WebServerActor ready for StartServer message");
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartServer.class, this::onStartServer)
                .build();
    }

    private Behavior<Command> onStartServer(StartServer cmd) {
        System.out.println("🌐 StartServer message received: " + cmd.host + ":" + cmd.port);
        getContext().getLog().info("🌐 Starting HTTP server at {}:{}", cmd.host, cmd.port);

        try {
            Route routes = route(
                    // Health check
                    path("health", () -> complete("OK - Phase 2A")),
                    
                    // Simple chat API for testing
                    pathPrefix("api", () ->
                            path("chat", () ->
                                    post(() ->
                                            entity(Jackson.unmarshaller(ChatRequest.class), req -> {
                                                System.out.println("🎯 Received chat request: " + req.message);
                                                
                                                // Simple test response for now
                                                ChatResponseDTO response = new ChatResponseDTO(
                                                    "✅ Phase 2A Test Response: " + req.message,
                                                    req.sessionId
                                                );
                                                
                                                return complete(StatusCodes.OK, response, Jackson.marshaller());
                                            })
                                    )
                            )
                    ),
                    
                    // Static files
                    pathSingleSlash(() -> complete("WebServer is working!")),
                    
                    complete(StatusCodes.NOT_FOUND, "Not found")
            );

            System.out.println("🔧 Routes created, attempting to bind...");

            Http.get(system).newServerAt(cmd.host, cmd.port).bind(routes)
                    .whenComplete((binding, ex) -> {
                        if (binding != null) {
                            System.out.println("🎉 SUCCESS! HTTP server running at: http://" +
                                    binding.localAddress().getHostString() + ":" + 
                                    binding.localAddress().getPort());
                            getContext().getLog().info("🎉 HTTP server bound successfully");
                        } else {
                            System.out.println("❌ FAILED to bind HTTP server: " + ex.getMessage());
                            getContext().getLog().error("❌ HTTP bind failed", ex);
                            ex.printStackTrace();
                        }
                    });

        } catch (Exception e) {
            System.out.println("❌ EXCEPTION in onStartServer: " + e.getMessage());
            getContext().getLog().error("❌ Exception in HTTP server startup", e);
            e.printStackTrace();
        }

        return this;
    }
}
