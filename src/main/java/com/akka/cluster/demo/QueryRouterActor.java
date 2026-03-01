package com.akka.cluster.demo;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.cluster.typed.Cluster;
import com.akka.cluster.demo.messages.*;

import java.util.Optional;
import java.util.Set;

/**
 * QueryRouterActor - Central orchestrator for chat workflow
 * 
 * Phase 2A Implementation:
 * 1. Receives ChatQuery from WebServerActor
 * 2. Logs the query using LoggerActor (tell pattern)
 * 3. Routes to backend RAGSearchActor (forward pattern)
 * 4. Handles responses and error scenarios
 * 
 * Demonstrates: tell (logging), forward (routing), ask (coordination)
 */
public class QueryRouterActor extends AbstractBehavior<QueryRouterActor.Command> {

    // Command messages for QueryRouterActor
    public interface Command extends CborSerializable {}

    public static final class RouteChatQuery implements Command {
        public final ChatQuery query;
        public final ActorRef<ChatResponse> replyTo;

        public RouteChatQuery(ChatQuery query, ActorRef<ChatResponse> replyTo) {
            this.query = query;
            this.replyTo = replyTo;
        }
    }

    public static final class RAGServiceUpdate implements Command {
        public final Receptionist.Listing listing;

        public RAGServiceUpdate(Receptionist.Listing listing) {
            this.listing = listing;
        }
    }

    public static final class ProcessingError implements Command {
        public final String error;
        public final ChatQuery originalQuery;
        public final ActorRef<ChatResponse> originalSender;

        public ProcessingError(String error, ChatQuery originalQuery, ActorRef<ChatResponse> originalSender) {
            this.error = error;
            this.originalQuery = originalQuery;
            this.originalSender = originalSender;
        }
    }

    private final ActorRef<LogEvent> logger;
    private final String nodeAddress;
    private Optional<ActorRef<RAGRequest>> ragSearchActor;

    public static Behavior<Command> create(ActorRef<LogEvent> logger) {
        return Behaviors.setup(context -> new QueryRouterActor(context, logger));
    }

    private QueryRouterActor(ActorContext<Command> context, ActorRef<LogEvent> logger) {
        super(context);
        this.logger = logger;
        this.ragSearchActor = Optional.empty();
        
        Cluster cluster = Cluster.get(context.getSystem());
        this.nodeAddress = cluster.selfMember().address().toString();
        
        // Subscribe to RAG search service updates
        ActorRef<Receptionist.Listing> listingAdapter = context.messageAdapter(
            Receptionist.Listing.class, 
            RAGServiceUpdate::new
        );
        
        context.getSystem().receptionist().tell(
            Receptionist.subscribe(ClusterApp.RAG_SEARCH_KEY, listingAdapter)
        );
        
        context.getLog().info("🎯 QueryRouterActor started on {}", nodeAddress);
        context.getLog().info("🔍 Subscribing to RAG search service updates");
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .onMessage(RouteChatQuery.class, this::onRouteChatQuery)
            .onMessage(RAGServiceUpdate.class, this::onRAGServiceUpdate)
            .onMessage(ProcessingError.class, this::onProcessingError)
            .build();
    }

    /**
     * Handle RAG service discovery updates
     */
    private Behavior<Command> onRAGServiceUpdate(RAGServiceUpdate update) {
        Set<ActorRef<RAGRequest>> ragServices = update.listing.getServiceInstances(ClusterApp.RAG_SEARCH_KEY);
        
        if (!ragServices.isEmpty()) {
            this.ragSearchActor = Optional.of(ragServices.iterator().next());
            getContext().getLog().info("🔍 RAG search service discovered: {}", 
                                     ragSearchActor.get().path());
            
            // Log service discovery
            logger.tell(new LogEvent("SYSTEM", "RAG search service connected", "system"));
        } else {
            this.ragSearchActor = Optional.empty();
            getContext().getLog().warn("🔍 RAG search service unavailable");
            
            logger.tell(new LogEvent("WARN", "RAG search service disconnected", "system"));
        }
        
        return this;
    }

    /**
     * Main chat query routing logic
     * Demonstrates: tell (logging) and forward (routing to backend)
     */
    private Behavior<Command> onRouteChatQuery(RouteChatQuery command) {
        ChatQuery query = command.query;
        
        getContext().getLog().info("🎯 Routing chat query: '{}'", query.message);
        
        // TELL pattern: Log the query (fire-and-forget)
        logger.tell(new LogEvent("QUERY", "User question received: " + query.message, query.sessionId));
        
        // Check if backend RAG service is available
        if (ragSearchActor.isEmpty()) {
            getContext().getLog().warn("❌ RAG search service not available");
            
            // Return error response
            String errorMsg = "❌ Backend processing service is not available. " +
                            "Please ensure the backend node is running.";
            command.replyTo.tell(new ChatResponse(errorMsg, query.sessionId, "Router-Error"));
            
            logger.tell(new LogEvent("ERROR", "RAG service unavailable", query.sessionId));
            return this;
        }
        
        // FORWARD pattern: Send to backend RAGSearchActor
        // Create response adapter to handle async response flow
        ActorRef<ChatResponse> responseAdapter = getContext().messageAdapter(
            ChatResponse.class,
            response -> {
                // Forward response to original requester
                command.replyTo.tell(response);
                return new ProcessingCompleted(response);
            }
        );
        
        // Create RAG request with response adapter
        RAGRequest ragRequest = new RAGRequest(
            query.message, 
            query.sessionId, 
            createRAGResponseAdapter(query, command.replyTo)
        );
        
        // Send to backend node (cluster-aware)
        ragSearchActor.get().tell(ragRequest);
        
        // Log the routing action
        logger.tell(new LogEvent("ROUTING", 
            "Query forwarded to backend for RAG processing", query.sessionId));
        
        getContext().getLog().info("🎯 Query forwarded to RAG search service");
        
        return this;
    }

    /**
     * Create adapter to handle RAG responses and continue the pipeline
     */
    private ActorRef<RAGResponse> createRAGResponseAdapter(ChatQuery originalQuery, ActorRef<ChatResponse> originalSender) {
        return getContext().messageAdapter(
            RAGResponse.class,
            ragResponse -> {
                getContext().getLog().info("🎯 RAG processing completed: {} documents", 
                                         ragResponse.contextDocuments.size());
                
                // For Phase 2A: Generate mock final response
                String finalAnswer = String.format(
                    "🤖 **Boston Bot Response** (Phase 2A)\n\n" +
                    "**Your Question:** \"%s\"\n\n" +
                    "**Retrieved Context:** %d relevant documents found\n" +
                    "**Processing Status:** ✅ Complete\n\n" +
                    "📊 **Message Flow Verified:**\n" +
                    "1. ✅ WebServerActor → QueryRouterActor (ask)\n" +
                    "2. ✅ QueryRouterActor → LoggerActor (tell)\n" +
                    "3. ✅ QueryRouterActor → RAGSearchActor (forward)\n" +
                    "4. ✅ RAGSearchActor → LLMActor (forward)\n" +
                    "5. ✅ Response pipeline back to WebServerActor\n\n" +
                    "🎯 **Next:** Phase 2B will add real OpenAI responses!\n\n" +
                    "**Sample Context Documents:**\n%s",
                    originalQuery.message,
                    ragResponse.contextDocuments.size(),
                    ragResponse.contextDocuments.isEmpty() ? "• No documents found" : 
                        "• " + String.join("\n• ", ragResponse.contextDocuments.subList(0, Math.min(2, ragResponse.contextDocuments.size())))
                );
                
                // Send final response
                originalSender.tell(new ChatResponse(finalAnswer, originalQuery.sessionId, "Phase2A-Pipeline"));
                
                // Log completion
                logger.tell(new LogEvent("COMPLETED", 
                    "Chat query processing completed successfully", originalQuery.sessionId));
                
                return new ProcessingCompleted(new ChatResponse(finalAnswer, originalQuery.sessionId));
            }
        );
    }

    /**
     * Handle processing errors
     */
    private Behavior<Command> onProcessingError(ProcessingError command) {
        getContext().getLog().error("❌ Processing error: {}", command.error);
        
        // Log error
        logger.tell(new LogEvent("ERROR", 
            "Query processing failed: " + command.error, command.originalQuery.sessionId));
        
        // Send error response
        String errorResponse = String.format(
            "❌ **Processing Error**\n\n" +
            "Sorry, I encountered an error processing your question:\n" +
            "\"%s\"\n\n" +
            "**Error Details:** %s\n" +
            "**Session:** %s\n\n" +
            "🔧 **System Status:** Phase 2A development mode\n" +
            "Please try again or check system logs.",
            command.originalQuery.message,
            command.error,
            command.originalQuery.sessionId
        );
        
        command.originalSender.tell(new ChatResponse(errorResponse, command.originalQuery.sessionId, "Error"));
        
        return this;
    }

    // Internal message for completion tracking
    private static final class ProcessingCompleted implements Command {
        public final ChatResponse response;

        public ProcessingCompleted(ChatResponse response) {
            this.response = response;
        }
    }
}
