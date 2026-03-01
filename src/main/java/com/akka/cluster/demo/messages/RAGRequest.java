package com.akka.cluster.demo.messages;

import akka.actor.typed.ActorRef;
import com.akka.cluster.demo.CborSerializable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RAG search request message sent from QueryRouterActor to RAGSearchActor
 */
public class RAGRequest implements CborSerializable {
    public final String query;
    public final String sessionId;
    public final int maxResults;
    public final ActorRef<RAGResponse> replyTo;

    @JsonCreator
    public RAGRequest(
        @JsonProperty("query") String query,
        @JsonProperty("sessionId") String sessionId,
        @JsonProperty("maxResults") int maxResults,
        @JsonProperty("replyTo") ActorRef<RAGResponse> replyTo
    ) {
        this.query = query;
        this.sessionId = sessionId;
        this.maxResults = maxResults;
        this.replyTo = replyTo;
    }

    public RAGRequest(String query, String sessionId, ActorRef<RAGResponse> replyTo) {
        this(query, sessionId, 3, replyTo);
    }

    @Override
    public String toString() {
        return String.format("RAGRequest{query='%s', sessionId='%s', maxResults=%d}", 
                           query, sessionId, maxResults);
    }
}
