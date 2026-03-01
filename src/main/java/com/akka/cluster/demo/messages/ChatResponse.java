package com.akka.cluster.demo.messages;

import com.akka.cluster.demo.CborSerializable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Chat response message sent back to WebServerActor
 */
public class ChatResponse implements CborSerializable {
    public final String answer;
    public final String sessionId;
    public final long timestamp;
    public final String source;

    @JsonCreator
    public ChatResponse(
        @JsonProperty("answer") String answer,
        @JsonProperty("sessionId") String sessionId,
        @JsonProperty("timestamp") long timestamp,
        @JsonProperty("source") String source
    ) {
        this.answer = answer;
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.source = source;
    }

    public ChatResponse(String answer, String sessionId, String source) {
        this(answer, sessionId, System.currentTimeMillis(), source);
    }

    public ChatResponse(String answer, String sessionId) {
        this(answer, sessionId, System.currentTimeMillis(), "system");
    }

    @Override
    public String toString() {
        return String.format("ChatResponse{answer='%s', sessionId='%s', source='%s'}", 
                           answer, sessionId, source);
    }
}
