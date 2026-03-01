package com.akka.cluster.demo;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.typed.Cluster;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.util.Random;

/**
 * Backend worker actor that processes work requests.
 * This demonstrates how backend nodes can process work in a cluster.
 */
public class BackendWorker extends AbstractBehavior<BackendWorker.Command> {

    // Command messages
    public interface Command extends CborSerializable {}

    public static final class WorkRequest implements Command {
        public final String workId;
        public final String task;
        public final ActorRef<WorkResponse> replyTo;

        @JsonCreator
        public WorkRequest(
            @JsonProperty("workId") String workId,
            @JsonProperty("task") String task,
            @JsonProperty("replyTo") ActorRef<WorkResponse> replyTo
        ) {
            this.workId = workId;
            this.task = task;
            this.replyTo = replyTo;
        }
    }

    public static final class WorkResponse implements CborSerializable {
        public final String workId;
        public final String result;
        public final String processedBy;

        @JsonCreator
        public WorkResponse(
            @JsonProperty("workId") String workId,
            @JsonProperty("result") String result,
            @JsonProperty("processedBy") String processedBy
        ) {
            this.workId = workId;
            this.result = result;
            this.processedBy = processedBy;
        }
    }

    private static final class ProcessingComplete implements Command {
        public final WorkRequest originalRequest;
        public final String result;

        public ProcessingComplete(WorkRequest originalRequest, String result) {
            this.originalRequest = originalRequest;
            this.result = result;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(BackendWorker::new);
    }

    private final Random random = new Random();
    private final String nodeAddress;

    private BackendWorker(ActorContext<Command> context) {
        super(context);
        Cluster cluster = Cluster.get(context.getSystem());
        this.nodeAddress = cluster.selfMember().address().toString();
        
        System.out.println("🔧 Backend worker started on " + nodeAddress);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .onMessage(WorkRequest.class, this::onWorkRequest)
            .onMessage(ProcessingComplete.class, this::onProcessingComplete)
            .build();
    }

    private Behavior<Command> onWorkRequest(WorkRequest request) {
        System.out.println(String.format(
            "🔄 Processing work request '%s': %s", 
            request.workId, 
            request.task
        ));

        // Simulate processing time (1-3 seconds)
        Duration processingTime = Duration.ofMillis(1000 + random.nextInt(2000));
        
        getContext().scheduleOnce(
            processingTime,
            getContext().getSelf(),
            new ProcessingComplete(request, processTask(request.task))
        );

        return this;
    }

    private Behavior<Command> onProcessingComplete(ProcessingComplete complete) {
        WorkRequest originalRequest = complete.originalRequest;
        
        WorkResponse response = new WorkResponse(
            originalRequest.workId,
            complete.result,
            nodeAddress
        );
        
        System.out.println(String.format(
            "✅ Completed work '%s': %s", 
            originalRequest.workId, 
            complete.result
        ));
        
        originalRequest.replyTo.tell(response);
        return this;
    }

    private String processTask(String task) {
        // Simulate different types of processing based on task content
        if (task.toLowerCase().contains("calculate")) {
            int result = random.nextInt(1000);
            return String.format("Calculation result: %d", result);
        } else if (task.toLowerCase().contains("analyze")) {
            String[] analyses = {"positive", "negative", "neutral", "complex"};
            return String.format("Analysis result: %s", analyses[random.nextInt(analyses.length)]);
        } else if (task.toLowerCase().contains("transform")) {
            return String.format("Transformed: %s -> %s_processed", task, task.toUpperCase());
        } else {
            return String.format("Processed: %s [%d operations completed]", task, random.nextInt(100));
        }
    }
}
