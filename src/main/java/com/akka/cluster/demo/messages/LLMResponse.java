package com.akka.cluster.demo.messages;

import com.akka.cluster.demo.CborSerializable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * LLM response message with generated answer
 */
public class LLMResponse implements CborSerializable {
    public final String answer;
    public final String sessionId;
    public final boolean success;
    public final String model;
    public final int tokenCount;

    @JsonCreator
    public LLMResponse(
        @JsonProperty("answer") String answer,
        @JsonProperty("sessionId") String sessionId,
        @JsonProperty("success") boolean success,
        @JsonProperty("model") String model,
        @JsonProperty("tokenCount") int tokenCount
    ) {
        this.answer = answer;
        this.sessionId = sessionId;
        this.success = success;
        this.model = model;
        this.tokenCount = tokenCount;
    }

    public LLMResponse(String answer, String sessionId, boolean success) {
        this(answer, sessionId, success, "mock-llm", 0);
    }

    @Override
    public String toString() {
        return String.format("LLMResponse{answer='%s', sessionId='%s', success=%s, model='%s'}", 
                           answer, sessionId, success, model);
    }
}
