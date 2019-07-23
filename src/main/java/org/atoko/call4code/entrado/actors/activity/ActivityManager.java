package org.atoko.call4code.entrado.actors.activity;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.EventSourcedEntity;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import lombok.Data;
import org.atoko.call4code.entrado.model.details.ActivityDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.atoko.call4code.entrado.actors.activity.ActivityActor.ACTIVITY_PREFIX;

public class ActivityManager extends EventSourcedEntity<
        ActivityManager.Command, ActivityManager.Event, ActivityManager.State
        > {

    public static final String ACTIVITY_MANAGER = "ActivityManager_;";
    public static EntityTypeKey<Command> entityTypeKey = EntityTypeKey.create(ActivityManager.Command.class, "ActivityManager;");
    private static ActivityDetails[] activityDetails = new ActivityDetails[]{};
    private ActorContext actorContext;

    private Map<String, ActivityDetails> map = new HashMap<>();

    public ActivityManager(String deviceId, ActorContext actorContext) {
        super(entityTypeKey, getEntityId(deviceId));
        this.actorContext = actorContext;
    }

    public static Behavior<Command> behavior(String persistenceId) {
        return Behaviors.setup(actorContext -> new ActivityManager(persistenceId, actorContext));
    }

    public static String getEntityId(String deviceId) {
        return ACTIVITY_MANAGER;
    }

    @Override
    public State emptyState() {
        return null;
    }

    @Override
    public CommandHandler<Command, Event, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(ActivityCreateCommand.class,
                        (state, event) -> {
                            ActorRef child = actorContext.spawn(
                                    new ActivityActor(
                                            event.getActivityId())
                                    ,
                                    ACTIVITY_PREFIX + event.getActivityId()
                            );
                            child.tell(event);
                            map.put(event.activityId, new ActivityDetails(
                                    event.deviceId,
                                    event.activityId,
                                    event.name
                            ));
                            return Effect().none().thenRun(() -> event.replyTo.tell(true));
                        })
                .onCommand(ActivityActor.ActivityDetailsPoll.class,
                        (state, command) -> {
                            actorContext.getChild(ACTIVITY_PREFIX + command.id)
                                    .ifPresentOrElse((action) -> {
                                        ActorRef child = (ActorRef) action;
                                        child.tell(command);
                                    }, () -> {
                                        command.replyTo.tell(new ActivityDetails.ActivityNullDetails());
                                    });
                            return Effect().none();
                        })
                .onCommand(ActivityQueryPoll.class,
                        (state, poll) -> {
                            return Effect().none().thenRun(() -> {
                                poll.replyTo.tell(
                                        map.values().toArray(activityDetails)
                                );
                            });
                        })
                .build();
    }

    @Override
    public EventHandler<State, Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState().build();
    }

    public interface Command {
    }

    public interface Event {
    }

    @Data
    public static class ActivityCreateCommand implements Command {
        public ActorRef replyTo;
        String deviceId;
        String activityId;
        String name;

        public ActivityCreateCommand(ActorRef replyTo, String deviceId, String activityId, String name) {
            this.replyTo = replyTo;
            this.deviceId = deviceId;
            this.activityId = activityId;
            this.name = name;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getActivityId() {
            return activityId;
        }

        public String getName() {
            return name;
        }
    }

    public static class ActivityQueryPoll implements ActivityManager.Command {
        ActorRef<ActivityDetails[]> replyTo;

        public ActivityQueryPoll(ActorRef<ActivityDetails[]> replyTo) {
            this.replyTo = replyTo;
        }

        public ActivityQueryPoll(Supplier<ActorRef<ActivityDetails[]>> f) {
            this.replyTo = f.get();
        }
    }

    public class State {
    }
}
