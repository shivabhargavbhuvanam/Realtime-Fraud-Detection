package com.akka.cluster.demo.messages;

import com.akka.cluster.demo.CborSerializable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Log event message sent to LoggerActor for audit trail
 */
public class LogEvent implements CborSerializable {
    public final String level;
    public final String category;
    public final String message;
    public final String sessionId;
    public final long timestamp;

    @JsonCreator
    public LogEvent(
        @JsonProperty("level") String level,
        @JsonProperty("category") String category,
        @JsonProperty("message") String message,
        @JsonProperty("sessionId") String sessionId,
        @JsonProperty("timestamp") long timestamp
    ) {
        this.level = level;
        this.category = category;
        this.message = message;
        this.sessionId = sessionId;
        this.timestamp = timestamp;
    }

    public LogEvent(String level, String category, String message, String sessionId) {
        this(level, category, message, sessionId, System.currentTimeMillis());
    }

    public LogEvent(String category, String message, String sessionId) {
        this("INFO", category, message, sessionId, System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (session: %s, time: %d)", 
                           level, category, message, sessionId, timestamp);
    }
}
