package com.akka.cluster.demo;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.typed.Cluster;
import com.akka.cluster.demo.messages.LogEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LoggerActor - Audit logging and interaction tracking
 * 
 * This actor runs on the frontend node and handles all logging:
 * 1. User interaction logging
 * 2. System event logging  
 * 3. Error and debugging logs
 * 4. Performance metrics
 * 
 * Demonstrates: tell pattern (fire-and-forget logging)
 */
public class LoggerActor extends AbstractBehavior<LogEvent> {

    private final String nodeAddress;
    private final DateTimeFormatter timeFormatter;

    public static Behavior<LogEvent> create() {
        return Behaviors.setup(LoggerActor::new);
    }

    private LoggerActor(ActorContext<LogEvent> context) {
        super(context);
        
        Cluster cluster = Cluster.get(context.getSystem());
        this.nodeAddress = cluster.selfMember().address().toString();
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        
        context.getLog().info("📝 LoggerActor started on {}", nodeAddress);
        
        // Log startup event
        logToConsole("SYSTEM", "LoggerActor initialized", "system", System.currentTimeMillis());
    }

    @Override
    public Receive<LogEvent> createReceive() {
        return newReceiveBuilder()
            .onMessage(LogEvent.class, this::onLogEvent)
            .build();
    }

    /**
     * Handle all log events with structured logging
     * This is the target of tell messages from other actors
     */
    private Behavior<LogEvent> onLogEvent(LogEvent event) {
        logToConsole(event.category, event.message, event.sessionId, event.timestamp);
        
        // In future phases, we could:
        // - Write to file
        // - Send to external logging service
        // - Store in database
        // - Send metrics to monitoring system
        
        return this;
    }

    /**
     * Format and output log entries consistently
     */
    private void logToConsole(String category, String message, String sessionId, long timestamp) {
        LocalDateTime time = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp), 
            java.time.ZoneId.systemDefault()
        );
        
        String formattedTime = time.format(timeFormatter);
        String logLine = String.format(
            "📝 [%s] %s | %s | session:%s | node:%s",
            formattedTime,
            category.toUpperCase(),
            message,
            sessionId,
            nodeAddress
        );
        
        // Use different logging based on category
        switch (category.toLowerCase()) {
            case "error":
                getContext().getLog().error(logLine);
                break;
            case "warn":
            case "warning":
                getContext().getLog().warn(logLine);
                break;
            case "debug":
                getContext().getLog().debug(logLine);
                break;
            default:
                getContext().getLog().info(logLine);
        }
        
        // Also print to console for visibility during development
        System.out.println(logLine);
    }
}
