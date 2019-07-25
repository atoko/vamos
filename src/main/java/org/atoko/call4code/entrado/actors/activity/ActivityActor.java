package org.atoko.call4code.entrado.actors.activity;

import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.EventSourcedEntity;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import lombok.Data;
import org.atoko.call4code.entrado.model.details.ActivityDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ActivityActor extends EventSourcedEntity<
        ActivityCommands.Command, ActivityEvents.Event, ActivityActor.State
        > {
    public static String ACTIVITY_PREFIX = "activity*";
    public static EntityTypeKey<ActivityCommands.Command> entityTypeKey = EntityTypeKey.create(ActivityCommands.Command.class, "ActivityActor*+");

    public ActivityActor(String activityId) {
        super(entityTypeKey, activityId);
    }

    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public CommandHandler<ActivityCommands.Command, ActivityEvents.Event, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(ActivityCommands.ActivityCreateCommand.class,
                        command -> Effect().persist(new ActivityEvents.ActivityCreatedEvent(command))
                )
                .onCommand(ActivityCommands.ActivityJoinCommand.class,
                        (state, command) -> Effect().persist(new ActivityEvents.ActivityJoinedEvent(command))
                                .thenRun(() -> command.replyTo.tell(new ActivityDetails(state)))
                )
                .onCommand(ActivityCommands.ActivityDetailsPoll.class,
                        (state, command) -> Effect().none()
                                .thenRun(() -> command.replyTo.tell(new ActivityDetails(state)))
                )
                .build();
    }

    @Data
    public static class State {
        public PersistenceId deviceId;
        public PersistenceId activityId;
        public String name;
        public List<String> personIds;

        public State() {
        }

        public State(ActivityEvents.ActivityCreatedEvent event) {
            this.deviceId = PersistenceId.apply(event.deviceId);
            this.activityId = PersistenceId.apply(event.activityId);
            this.name = event.name;
            this.personIds = new ArrayList<>();
        }

        public State(State state, ActivityEvents.ActivityJoinedEvent event) {
            this.deviceId = state.deviceId;
            this.activityId = state.activityId;
            this.name = state.name;

            state.personIds.add(event.personId);
            this.personIds = new ArrayList<>(state.personIds);
        }


        public static State inception(ActivityEvents.ActivityCreatedEvent event) {
            return new State(event);
        }
    }


    @Override
    public EventHandler<State, ActivityEvents.Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(ActivityEvents.ActivityCreatedEvent.class,
                        (state, event) -> State.inception(event))
                .onEvent(ActivityEvents.ActivityJoinedEvent.class,
                        (BiFunction<State, ActivityEvents.ActivityJoinedEvent, State>) State::new)
                .build();
    }
}
