package org.atoko.call4code.entrado.actors.activity;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.EventSourcedEntity;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import lombok.Data;
import org.atoko.call4code.entrado.model.details.ActivityDetails;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ActivityActor extends EventSourcedEntity<
        ActivityCommands.Command, ActivityEvents.Event, ActivityActor.State
        > {
    public static String ACTIVITY_PREFIX = "activity*";
    public static EntityTypeKey<ActivityCommands.Command> entityTypeKey = EntityTypeKey.create(ActivityCommands.Command.class, "ActivityActor*+");
    private State _state = emptyState();
    public ActivityActor(ActivityEvents.ActivityCreatedEvent event) {
        super(entityTypeKey, getEntityId(event.deviceId, event.activityId));
    }

    public static String getEntityId(String sourceId, String personId) {
        return ACTIVITY_PREFIX + personId + "&" + sourceId;
    }

    public static Behavior<ActivityCommands.Command> behavior(ActivityEvents.ActivityCreatedEvent event) {
        return Behaviors.setup(actorContext -> new ActivityActor(event));
    }

    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public CommandHandler<ActivityCommands.Command, ActivityEvents.Event, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(ActivityCommands.ActivityGenesis.class,
                        command -> {
                            this._state = new State(command.event);
                            return Effect().none();
                        })
                .onCommand(ActivityCommands.ActivityJoinCommand.class,
                        command -> {
                            this._state = new State(_state, new ActivityEvents.ActivityJoinedEvent(command));
                            return Effect().none().thenRun(() -> {
                                command.replyTo.tell(new ActivityDetails(_state));
                            });
                        })
                .onCommand(ActivityCommands.ActivityStationCreateCommand.class,
                        command -> {
                            this._state = new State(_state, new ActivityEvents.ActivityStationCreatedEvent(command));
                            return Effect().none().thenRun(() -> {
                                command.replyTo.tell(new ActivityDetails(_state));
                            });
                        })
                .onCommand(ActivityCommands.ActivityDetailsPoll.class,
                        (state, command) -> Effect().none()
                                .thenRun(() -> command.replyTo.tell(new ActivityDetails(_state)))
                )
                .build();
    }

    @Override
    public EventHandler<State, ActivityEvents.Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .build();
    }

    @Data
    public static class State {
        public PersistenceId deviceId;
        public PersistenceId activityId;
        public String name;
        public List<String> personIds;
        public Map<String, ActivityStationState> stations;

        public State() {
        }

        public State(ActivityEvents.ActivityCreatedEvent event) {
            this.deviceId = PersistenceId.apply(event.deviceId);
            this.activityId = PersistenceId.apply(event.activityId);
            this.name = event.name;
            this.personIds = new LinkedList<>();
            this.stations = new WeakHashMap<>();
        }

        public State(State state, ActivityEvents.ActivityJoinedEvent event) {
            this.deviceId = state.deviceId;
            this.activityId = state.activityId;
            this.name = state.name;

            state.personIds.add(event.command.personId);
            this.personIds = new LinkedList<>(state.personIds);
        }

        public State(State state, ActivityEvents.ActivityStationEvent activityStationEvent) {
            this.deviceId = state.deviceId;
            this.activityId = state.activityId;
            this.name = state.name;

            ActivityStationState station = state.stations.get(activityStationEvent.stationId);
            state.stations.put(activityStationEvent.stationId, new ActivityStationState(station, activityStationEvent));
            this.stations = new WeakHashMap<>(state.stations);
        }

        public static State inception(ActivityEvents.ActivityCreatedEvent event) {
            return new State(event);
        }
    }
}
