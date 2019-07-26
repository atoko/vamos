package org.atoko.call4code.entrado.actors.activity;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.EventSourcedEntity;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import org.atoko.call4code.entrado.model.details.ActivityDetails;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class ActivityManager extends EventSourcedEntity<
        ActivityCommands.Command, ActivityEvents.Event, ActivityManager.State
        > {

    public static final String ACTIVITY_MANAGER = "ActivityManager*";
    private static ActivityDetails[] activityDetails = new ActivityDetails[]{};
    private ActorContext actorContext;
    public ActivityManager(String sourceId, String persistenceId, ActorContext actorContext) {
        super(entityTypeKey(sourceId), persistenceId);
        this.actorContext = actorContext;
    }

    public static EntityTypeKey<ActivityCommands.Command> entityTypeKey(String sourceId) {
        return EntityTypeKey.create(ActivityCommands.Command.class, "*ActivityManager(" + sourceId + ")+");
    }

    public static Behavior<ActivityCommands.Command> behavior(String sourceId, EntityContext<ActivityCommands.Command> context) {
        return Behaviors.setup(actorContext -> new ActivityManager(sourceId, context.getEntityId(), context.getActorContext()));
    }

    public static String getEntityId(String deviceId) {
        return ACTIVITY_MANAGER + deviceId;
    }

    @Override
    public State emptyState() {
        return new State(null);
    }

    @Override
    public CommandHandler<ActivityCommands.Command, ActivityEvents.Event, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(ActivityCommands.ActivityCreateCommand.class,
                        (state, command) -> {
                            return Effect().persist(new ActivityEvents.ActivityCreatedEvent(
                                    command
                            )).thenRun(() -> command.replyTo.tell(true));
                        })
                .onCommand(ActivityCommands.ActivityTargetedCommand.class,
                        (state, command) -> {
                            actorContext.getChild(command.activityId)
                                    .ifPresentOrElse((action) -> {
                                        ActorRef child = (ActorRef) action;
                                        child.tell(command);
                                    }, () -> command.replyTo.tell(new ActivityDetails.ActivityNullDetails()));
                            return Effect().none();
                        })
                .onCommand(ActivityCommands.ActivityQueryPoll.class,
                        (state, poll) -> {
                            return Effect().none().thenRun(() -> {
                                poll.replyTo.tell(
                                        state.map.values().toArray(activityDetails)
                                );
                            });
                        })
                .build();
    }

    @Override
    public EventHandler<State, ActivityEvents.Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(ActivityEvents.ActivityCreatedEvent.class, (state, event) -> {
                    return state.handle(event);
                })
                .onEvent(ActivityEvents.Event.class, (state, event) -> {
                    return state;
                })
                .onAnyEvent((state, e) -> {
                    return state;
                });
    }

    public class State {
        private Map<String, ActivityDetails> map;

        public State(Map<String, ActivityDetails> map) {
            if (map == null) {
                this.map = new WeakHashMap<>();
            } else {
                this.map = new WeakHashMap<>(map);
            }
        }


        public ActivityManager.State handle(ActivityEvents.ActivityCreatedEvent event) {
            map.put(event.activityId, new ActivityDetails(
                    event.deviceId,
                    event.activityId,
                    event.name,
                    Collections.EMPTY_LIST
            ));

            actorContext.spawn(
                    ActivityActor.behavior(event),
                    ActivityActor.getEntityId(event.deviceId, event.activityId)
            ).tell(new ActivityCommands.ActivityGenesis(event));

            return new ActivityManager.State(this.map);
        }
    }
}
