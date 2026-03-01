package com.akka.cluster.demo.messages;

import akka.actor.typed.ActorRef;
import com.akka.cluster.demo.CborSerializable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * LLM request message sent from RAGSearchActor to LLMActor
 */
public class LLMRequest implements CborSerializable {
    public final String query;
    public final String sessionId;
    public final List<String> contextDocuments;
    public final ActorRef<LLMResponse> replyTo;

    @JsonCreator
    public LLMRequest(
        @JsonProperty("query") String query,
        @JsonProperty("sessionId") String sessionId,
        @JsonProperty("contextDocuments") List<String> contextDocuments,
        @JsonProperty("replyTo") ActorRef<LLMResponse> replyTo
    ) {
        this.query = query;
        this.sessionId = sessionId;
        this.contextDocuments = contextDocuments;
        this.replyTo = replyTo;
    }

    @Override
    public String toString() {
        return String.format("LLMRequest{query='%s', sessionId='%s', contextDocs=%d}", 
                           query, sessionId, contextDocuments.size());
    }
}
