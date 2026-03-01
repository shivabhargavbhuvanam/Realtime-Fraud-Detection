package com.akka.cluster.demo;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.TimerScheduler;
import akka.cluster.typed.Cluster;
import com.akka.cluster.demo.messages.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * RAGSearchActor - Vector database search and document retrieval
 * 
 * Phase 2A Implementation:
 * 1. Mock vector database search with sample Boston documents
 * 2. Context formatting and preparation for LLM
 * 3. Forward enriched queries to LLMActor (forward pattern)
 * 4. Demonstrate cross-node actor communication
 */
public class RAGSearchActor extends AbstractBehavior<RAGRequest> {

    private final ActorRef<LLMRequest> llmActor;
    private final String nodeAddress;
    private final Random random;
    private final TimerScheduler<RAGRequest> timers;

    public static Behavior<RAGRequest> create(ActorRef<LLMRequest> llmActor) {
        return Behaviors.setup(context -> 
            Behaviors.withTimers(timers -> new RAGSearchActor(context, llmActor, timers))
        );
    }

    private RAGSearchActor(ActorContext<RAGRequest> context, ActorRef<LLMRequest> llmActor, TimerScheduler<RAGRequest> timers) {
        super(context);
        this.llmActor = llmActor;
        this.random = new Random();
        this.timers = timers;
        
        Cluster cluster = Cluster.get(context.getSystem());
        this.nodeAddress = cluster.selfMember().address().toString();
        
        context.getLog().info("🔍 RAGSearchActor started on {}", nodeAddress);
    }

    @Override
    public Receive<RAGRequest> createReceive() {
        return newReceiveBuilder()
            .onMessage(RAGRequest.class, this::onRAGRequest)
            .build();
    }

    /**
     * Handle RAG search requests
     * Phase 2A: Mock vector search with sample Boston documents
     */
    private Behavior<RAGRequest> onRAGRequest(RAGRequest request) {
        getContext().getLog().info("🔍 Processing RAG search for: '{}'", request.query);
        
        // Phase 2A: Mock document retrieval based on query
        List<String> retrievedDocuments = performMockVectorSearch(request.query);
        
        getContext().getLog().info("🔍 Retrieved {} relevant documents", retrievedDocuments.size());
        
        // Process the RAG request and forward to LLM
        processRAGRequest(request, retrievedDocuments);
        
        return this;
    }

    /**
     * Process RAG request and forward to LLM with context
     */
    private void processRAGRequest(RAGRequest request, List<String> retrievedDocuments) {
        // Create LLM response adapter that will complete the original request
        ActorRef<LLMResponse> llmResponseAdapter = getContext().messageAdapter(
            LLMResponse.class,
            llmResponse -> {
                // When LLM responds, send RAG response back to original requester
                RAGResponse ragResponse = new RAGResponse(
                    request.sessionId,
                    retrievedDocuments,
                    retrievedDocuments.size(),
                    llmResponse.success
                );
                
                request.replyTo.tell(ragResponse);
                return request; // Return original request to satisfy type system
            }
        );
        
        // Create LLM request with retrieved context
        LLMRequest llmRequest = new LLMRequest(
            request.query,
            request.sessionId,
            retrievedDocuments,
            llmResponseAdapter
        );
        
        getContext().getLog().info("🔍 Forwarding to LLMActor with {} context documents", 
                                 retrievedDocuments.size());
        
        // Forward to LLMActor
        llmActor.tell(llmRequest);
    }

    /**
     * Phase 2A: Mock vector search implementation
     */
    private List<String> performMockVectorSearch(String query) {
        List<String> bostonDocs = Arrays.asList(
            "Boston is the capital and largest city of Massachusetts, with a population of approximately 685,000 residents.",
            "The Freedom Trail is a 2.5-mile red-brick walking trail through downtown Boston that connects 16 historically significant sites.",
            "Fenway Park, home to the Boston Red Sox since 1912, features the iconic 37-foot-tall Green Monster left field wall.",
            "The Greater Boston area is home to over 50 colleges and universities, including Harvard University and MIT.",
            "Boston Common, established in 1634, is America's oldest public park and spans 50 acres in downtown Boston.",
            "The North End is Boston's historic Italian-American neighborhood, famous for authentic restaurants and historic sites.",
            "The MBTA operates the subway system known as 'the T' with Red, Blue, Green, and Orange lines.",
            "The Museum of Fine Arts Boston houses one of the world's largest art collections.",
            "Quincy Market and Faneuil Hall Marketplace offer over 50 shops and restaurants in a historic setting.",
            "The Boston Tea Party Ships & Museum recreates the famous 1773 protest that helped spark the American Revolution."
        );
        
        List<String> relevantDocs = findRelevantDocuments(query, bostonDocs);
        return relevantDocs.subList(0, Math.min(relevantDocs.size(), 3));
    }

    /**
     * Mock relevance scoring for Phase 2A
     */
    private List<String> findRelevantDocuments(String query, List<String> allDocuments) {
        List<String> relevantDocs = new ArrayList<>();
        String queryLower = query.toLowerCase();
        
        for (String doc : allDocuments) {
            String docLower = doc.toLowerCase();
            if (containsQueryKeywords(docLower, queryLower)) {
                relevantDocs.add(doc);
            }
        }
        
        if (relevantDocs.isEmpty()) {
            relevantDocs.add(allDocuments.get(0)); // Default: population info
            relevantDocs.add(allDocuments.get(1)); // Default: Freedom Trail
        }
        
        return relevantDocs;
    }

    /**
     * Simple keyword matching for mock implementation
     */
    private boolean containsQueryKeywords(String document, String query) {
        String[] queryWords = query.split("\\s+");
        
        for (String word : queryWords) {
            if (word.length() > 2 && document.contains(word)) {
                return true;
            }
        }
        
        // Special keyword mappings
        if (query.contains("population") && document.contains("population")) return true;
        if (query.contains("park") && document.contains("park")) return true;
        if (query.contains("museum") && document.contains("museum")) return true;
        if (query.contains("transport") && document.contains("MBTA")) return true;
        if (query.contains("attraction") && (document.contains("Trail") || document.contains("Park"))) return true;
        
        return false;
    }
}
