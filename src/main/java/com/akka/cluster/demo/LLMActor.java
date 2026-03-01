package com.akka.cluster.demo;

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
import java.util.List;
import java.util.Random;

/**
 * LLMActor - OpenAI API integration and response generation
 * 
 * Phase 2A Implementation:
 * 1. Mock LLM responses using retrieved context documents
 * 2. Demonstrate response generation pipeline
 * 3. Proper error handling and logging
 * 4. Foundation for Phase 2B OpenAI integration
 */
public class LLMActor extends AbstractBehavior<LLMRequest> {

    private final String nodeAddress;
    private final Random random;
    private final TimerScheduler<LLMRequest> timers;

    public static Behavior<LLMRequest> create() {
        return Behaviors.setup(context -> 
            Behaviors.withTimers(timers -> new LLMActor(context, timers))
        );
    }

    private LLMActor(ActorContext<LLMRequest> context, TimerScheduler<LLMRequest> timers) {
        super(context);
        this.random = new Random();
        this.timers = timers;
        
        Cluster cluster = Cluster.get(context.getSystem());
        this.nodeAddress = cluster.selfMember().address().toString();
        
        context.getLog().info("🤖 LLMActor started on {}", nodeAddress);
    }

    @Override
    public Receive<LLMRequest> createReceive() {
        return newReceiveBuilder()
            .onMessage(LLMRequest.class, this::onLLMRequest)
            .build();
    }

    /**
     * Handle LLM requests with retrieved context
     * Phase 2A: Generate intelligent mock responses based on context
     */
    private Behavior<LLMRequest> onLLMRequest(LLMRequest request) {
        getContext().getLog().info("🤖 Processing LLM request for: '{}'", request.query);
        getContext().getLog().info("🤖 Using {} context documents", request.contextDocuments.size());
        
        // Simulate LLM processing time (like OpenAI API call)
        timers.startSingleTimer(
            "llm-processing-" + request.sessionId,
            request,
            Duration.ofMillis(200 + random.nextInt(800))
        );
        
        // Process immediately for Phase 2A
        processLLMRequest(request);
        
        return this;
    }

    /**
     * Process LLM request and generate response
     */
    private void processLLMRequest(LLMRequest request) {
        try {
            // Generate context-aware response
            String generatedAnswer = generateContextAwareResponse(request.query, request.contextDocuments);
            
            // Create successful LLM response
            LLMResponse response = new LLMResponse(
                generatedAnswer,
                request.sessionId,
                true,
                "mock-gpt-3.5-turbo",
                estimateTokenCount(generatedAnswer)
            );
            
            getContext().getLog().info("🤖 LLM response generated: {} tokens", response.tokenCount);
            
            // Send response back to requester
            request.replyTo.tell(response);
            
        } catch (Exception e) {
            getContext().getLog().error("❌ LLM processing failed", e);
            
            // Send error response
            LLMResponse errorResponse = new LLMResponse(
                "❌ Sorry, I encountered an error generating your response. Please try again.",
                request.sessionId,
                false,
                "error",
                0
            );
            
            request.replyTo.tell(errorResponse);
        }
    }

    /**
     * Phase 2A: Generate intelligent mock responses using retrieved context
     */
    private String generateContextAwareResponse(String query, List<String> contextDocuments) {
        StringBuilder response = new StringBuilder();
        String queryLower = query.toLowerCase();
        
        // Greeting responses
        if (queryLower.contains("hello") || queryLower.contains("hi") || queryLower.contains("hey")) {
            response.append("Hello! Welcome to Boston! 🏙️\n\n");
            response.append("I'm your Boston Explorer assistant, powered by a distributed Akka Cluster ");
            response.append("with RAG (Retrieval-Augmented Generation) capabilities.\n\n");
            response.append("I can help you discover amazing things about Boston! ");
            response.append("Try asking about attractions, restaurants, history, or transportation.\n");
        }
        // Population queries
        else if (queryLower.contains("population") || queryLower.contains("people")) {
            response.append("**Boston Population Information:**\n\n");
            if (hasRelevantContext(contextDocuments, "population")) {
                response.append("Based on my knowledge base: ");
                response.append("Boston has approximately 685,000 residents within the city proper. ");
                response.append("The Greater Boston metropolitan area is home to about 4.9 million people.\n");
            }
        }
        // Attraction queries
        else if (queryLower.contains("attraction") || queryLower.contains("visit") || queryLower.contains("see")) {
            response.append("**Top Boston Attractions:**\n\n");
            response.append("🏛️ **Freedom Trail** - Historic 2.5-mile walking route\n");
            response.append("⚾ **Fenway Park** - Iconic Red Sox stadium with Green Monster\n");
            response.append("🌳 **Boston Common** - America's oldest public park (1634)\n");
            response.append("🎨 **Museum of Fine Arts** - World-class art collection\n");
        }
        // Transportation queries
        else if (queryLower.contains("transport") || queryLower.contains("subway") || queryLower.contains("train")) {
            response.append("**Boston Transportation:**\n\n");
            response.append("The MBTA (\"the T\") operates Boston's public transit system:\n");
            response.append("🔴 Red Line • 🔵 Blue Line • 🟢 Green Line • 🟠 Orange Line\n");
        }
        // General Boston questions
        else {
            response.append("**About Boston:**\n\n");
            response.append("Boston is a historic city with rich culture, world-class education, ");
            response.append("and amazing attractions.\n");
        }
        
        // Add context information if available
        if (!contextDocuments.isEmpty()) {
            response.append("\n📚 **From my knowledge base:**\n");
            for (int i = 0; i < Math.min(contextDocuments.size(), 2); i++) {
                response.append("• ").append(contextDocuments.get(i)).append("\n");
            }
        }
        
        // Phase 2A: Show system status
        response.append("\n🔧 **System Status (Phase 2A):**\n");
        response.append("✅ Cluster communication working\n");
        response.append("✅ RAG pipeline active (mock documents)\n");
        response.append("✅ Actor message flow complete\n");
        response.append("🎯 Phase 2B: Real OpenAI integration coming next!\n");
        
        return response.toString();
    }

    /**
     * Check if context documents contain relevant information
     */
    private boolean hasRelevantContext(List<String> contextDocuments, String topic) {
        return contextDocuments.stream()
            .anyMatch(doc -> doc.toLowerCase().contains(topic.toLowerCase()));
    }

    /**
     * Estimate token count for mock response
     */
    private int estimateTokenCount(String text) {
        return text.length() / 4;
    }

    /**
     * Mock relevance scoring for Phase 2A
     */
    private List<String> findRelevantDocuments(String query, List<String> allDocuments) {
        List<String> relevantDocs = new ArrayList<>();
        String queryLower = query.toLowerCase();
        
        for (String doc : allDocuments) {
            if (doc.toLowerCase().contains(queryLower)) {
                relevantDocs.add(doc);
            }
        }
        
        if (relevantDocs.isEmpty()) {
            relevantDocs.add(allDocuments.get(0));
        }
        
        return relevantDocs;
    }
}
