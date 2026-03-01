package com.akka.cluster.demo.messages;

import com.akka.cluster.demo.CborSerializable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User chat query message sent from WebServerActor to QueryRouterActor
 */
public class ChatQuery implements CborSerializable {
    public final String message;
    public final String sessionId;
    public final long timestamp;

    @JsonCreator
    public ChatQuery(
        @JsonProperty("message") String message,
        @JsonProperty("sessionId") String sessionId,
        @JsonProperty("timestamp") long timestamp
    ) {
        this.message = message;
        this.sessionId = sessionId;
        this.timestamp = timestamp;
    }

    public ChatQuery(String message, String sessionId) {
        this(message, sessionId, System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return String.format("ChatQuery{message='%s', sessionId='%s', timestamp=%d}", 
                           message, sessionId, timestamp);
    }
}
