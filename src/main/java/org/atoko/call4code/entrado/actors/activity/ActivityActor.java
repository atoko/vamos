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
import org.atoko.call4code.entrado.model.identifiers.PersonIdentifier;

import java.util.*;

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

    Set<String> eventsSeen = new HashSet<>();

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
                            if (!eventsSeen.contains(command.commandGuid)) {
                                this._state = new State(_state, new ActivityEvents.ActivityJoinedEvent(command));
                                this.eventsSeen.add(command.commandGuid);
                            }
                            return Effect().none().thenRun(() -> {
                                if (command.replyTo != null) {
                                    command.replyTo.tell(new ActivityDetails(_state));
                                }
                            });
                        })
                .onCommand(ActivityCommands.ActivityStationTargetedCommand.class,
                        command -> {
                            if (!eventsSeen.contains(command.commandGuid)) {
                                this._state = new State(_state, ActivityEvents.fromCommand(command));
                                this.eventsSeen.add(command.commandGuid);
                            }
                            return Effect().none().thenRun(() -> {
                                if (command.replyTo != null) {
                                    command.replyTo.tell(new ActivityDetails(_state));
                                }
                            });
                        })
                .onCommand(ActivityCommands.ActivityDetailsPoll.class,
                        (state, command) -> Effect().none()
                                .thenRun(() -> {
                                    if (command.replyTo != null) {
                                        command.replyTo.tell(new ActivityDetails(_state));
                                    }
                                })
                )
                .onCommand(ActivityCommands.Command.class, () -> {
                    return Effect().none();
                })
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
        public List<PersonIdentifier> personIds = new LinkedList<>();
        public Map<String, ActivityStationState> stations = new WeakHashMap<>();

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

            if (!state.personIds.contains(event.personId)) {
                state.personIds.add(event.personId);
            }
            this.personIds = new LinkedList<>(state.personIds);
            this.stations = new WeakHashMap<>(state.stations);
        }

        public State(State state, ActivityEvents.ActivityStationEvent activityStationEvent) {
            this.deviceId = state.deviceId;
            this.activityId = state.activityId;
            this.personIds = state.personIds;
            this.name = state.name;

            ActivityStationState station = state.stations.get(activityStationEvent.stationId);
            if (station != null) {
                state.stations.put(
                        activityStationEvent.stationId,
                        new ActivityStationState(station, activityStationEvent)
                );
            } else {
                state.stations.put(
                        activityStationEvent.stationId,
                        new ActivityStationState(ActivityStationState.empty, activityStationEvent)
                );
            }
            this.stations = new WeakHashMap<>(state.stations);
        }

        public static State inception(ActivityEvents.ActivityCreatedEvent event) {
            return new State(event);
        }
    }
}
