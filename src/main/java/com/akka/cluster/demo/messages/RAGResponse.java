package com.akka.cluster.demo.messages;

import com.akka.cluster.demo.CborSerializable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * RAG search response with retrieved context documents
 */
public class RAGResponse implements CborSerializable {
    public final String sessionId;
    public final List<String> contextDocuments;
    public final int totalResults;
    public final boolean success;

    @JsonCreator
    public RAGResponse(
        @JsonProperty("sessionId") String sessionId,
        @JsonProperty("contextDocuments") List<String> contextDocuments,
        @JsonProperty("totalResults") int totalResults,
        @JsonProperty("success") boolean success
    ) {
        this.sessionId = sessionId;
        this.contextDocuments = contextDocuments;
        this.totalResults = totalResults;
        this.success = success;
    }

    @Override
    public String toString() {
        return String.format("RAGResponse{sessionId='%s', docs=%d, success=%s}", 
                           sessionId, contextDocuments.size(), success);
    }
}
