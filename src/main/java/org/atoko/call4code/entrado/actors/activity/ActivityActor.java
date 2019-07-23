package org.atoko.call4code.entrado.actors.activity;

import akka.actor.typed.ActorRef;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.EventSourcedEntity;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import lombok.Data;
import org.atoko.call4code.entrado.model.details.ActivityDetails;

public class ActivityActor extends EventSourcedEntity<
        ActivityManager.Command, ActivityManager.Event, ActivityActor.State
        > {
    public static String ACTIVITY_PREFIX = "activity;";
    public static EntityTypeKey<ActivityManager.Command> entityTypeKey = EntityTypeKey.create(ActivityManager.Command.class, "PersonActor;");

    public ActivityActor(String activityId) {
        super(entityTypeKey, activityId);
    }

    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public CommandHandler<ActivityManager.Command, ActivityManager.Event, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(ActivityManager.ActivityCreateCommand.class,
                        command -> Effect().persist(new ActivityCreatedEvent(command))
                )
                .build();
    }

    @Override
    public EventHandler<State, ActivityManager.Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(ActivityCreatedEvent.class,
                        (state, event) -> State.inception(event))
                .build();
    }

    public static class ActivityDetailsPoll implements ActivityManager.Command {
        ActorRef<ActivityDetails> replyTo;
        String id;

        public ActivityDetailsPoll(ActorRef<ActivityDetails> replyTo, String id) {
            this.replyTo = replyTo;
            this.id = id;
        }
    }

    @Data
    public static class State {
        public PersistenceId deviceId;
        public PersistenceId activityId;
        public String name;

        public State() {
        }

        public State(ActivityCreatedEvent event) {
            this.deviceId = PersistenceId.apply(event.getDeviceId());
            this.activityId = PersistenceId.apply(event.getActivityId());
            this.name = event.getName();
        }

        public static State inception(ActivityCreatedEvent event) {
            return new State(event);
        }
    }

    public static class ActivityCreatedEvent implements ActivityManager.Event {
        String deviceId;
        String activityId;
        String name;

        public ActivityCreatedEvent(String deviceId, String activityId, String name) {
            this.deviceId = deviceId;
            this.activityId = activityId;
            this.name = name;
        }

        public ActivityCreatedEvent(ActivityManager.ActivityCreateCommand command) {
            this.deviceId = command.getDeviceId();
            this.activityId = command.getActivityId();
            this.name = command.getName();
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

}
