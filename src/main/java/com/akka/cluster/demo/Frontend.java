package com.akka.cluster.demo;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.TimerScheduler;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Subscribe;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Frontend actor that monitors cluster membership and simulates work distribution.
 * This demonstrates cluster membership monitoring and shows when backend nodes are available.
 */
public class Frontend extends AbstractBehavior<Frontend.Command> {

    // Command messages
    public interface Command extends CborSerializable {}

    private static final class SendWork implements Command {}
    
    private static final class ClusterMemberEvent implements Command {
        public final ClusterEvent.ClusterDomainEvent event;
        
        public ClusterMemberEvent(ClusterEvent.ClusterDomainEvent event) {
            this.event = event;
        }
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(context -> 
            Behaviors.withTimers(timers -> new Frontend(context, timers))
        );
    }

    private final TimerScheduler<Command> timers;
    private final Random random = new Random();
    private final AtomicInteger workCounter = new AtomicInteger(0);
    private final List<String> availableBackends = new ArrayList<>();
    private final String nodeAddress;

    private Frontend(ActorContext<Command> context, TimerScheduler<Command> timers) {
        super(context);
        this.timers = timers;
        
        Cluster cluster = Cluster.get(context.getSystem());
        this.nodeAddress = cluster.selfMember().address().toString();
        
        // Subscribe to cluster events to monitor backend nodes
        ActorRef<ClusterEvent.ClusterDomainEvent> clusterEventAdapter = 
            context.messageAdapter(ClusterEvent.ClusterDomainEvent.class, ClusterMemberEvent::new);
        cluster.subscriptions().tell(Subscribe.create(clusterEventAdapter, ClusterEvent.ClusterDomainEvent.class));
        
        // Start sending work simulation periodically
        timers.startTimerWithFixedDelay("send-work", new SendWork(), Duration.ofSeconds(5));
        
        System.out.println("🌐 Frontend started on " + nodeAddress);
        System.out.println("Frontend will simulate sending work to backend nodes every 5 seconds");
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
            .onMessage(SendWork.class, this::onSendWork)
            .onMessage(ClusterMemberEvent.class, this::onClusterMemberEvent)
            .build();
    }

    private Behavior<Command> onSendWork(SendWork command) {
        if (availableBackends.isEmpty()) {
            System.out.println("⏳ No backend workers available yet. Waiting for backend nodes to join...");
            return this;
        }

        // Simulate work distribution
        String selectedBackend = availableBackends.get(workCounter.get() % availableBackends.size());
        String workId = "work-" + workCounter.incrementAndGet();
        String task = generateRandomTask();
        
        System.out.println(String.format(
            "📤 [SIMULATED] Sending work '%s' to backend %s: %s", 
            workId, 
            selectedBackend,
            task
        ));
        
        // Simulate work completion after a delay
        timers.startSingleTimer("work-complete-" + workId, new SendWork(), Duration.ofSeconds(2));
        
        return this;
    }

    private Behavior<Command> onClusterMemberEvent(ClusterMemberEvent memberEvent) {
        ClusterEvent.ClusterDomainEvent event = memberEvent.event;
        
        if (event instanceof ClusterEvent.MemberUp) {
            ClusterEvent.MemberUp memberUp = (ClusterEvent.MemberUp) event;
            Member member = memberUp.member();
            
            if (member.hasRole("backend")) {
                availableBackends.add(member.address().toString());
                System.out.println("🔧 New backend node joined: " + member.address());
                System.out.println("📊 Available backends: " + availableBackends.size());
            }
        } else if (event instanceof ClusterEvent.MemberRemoved) {
            ClusterEvent.MemberRemoved memberRemoved = (ClusterEvent.MemberRemoved) event;
            Member member = memberRemoved.member();
            
            if (member.hasRole("backend")) {
                availableBackends.remove(member.address().toString());
                System.out.println("🔧 Backend node left: " + member.address());
                System.out.println("📊 Available backends: " + availableBackends.size());
            }
        }
        
        return this;
    }

    private String generateRandomTask() {
        String[] taskTypes = {
            "calculate prime numbers up to 1000",
            "analyze sentiment of customer feedback", 
            "transform data format from JSON to XML",
            "calculate fibonacci sequence",
            "analyze network traffic patterns",
            "transform image to grayscale",
            "calculate statistical variance",
            "analyze user behavior patterns"
        };
        
        return taskTypes[random.nextInt(taskTypes.length)];
    }
}
