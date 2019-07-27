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
import lombok.Data;
import org.atoko.call4code.entrado.model.details.ActivityDetails;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

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
                            AtomicReference<ActivityEvents.Event> event = new AtomicReference<>();
                            actorContext.getChild(command.activityId)
                                .ifPresentOrElse((action) -> {
                                    ActorRef child = (ActorRef) action;
                                    child.tell(command);
                                    if (command instanceof ActivityCommands.ActivityStationCreateCommand) {
                                        event.set(new ActivityEvents.ActivityStationCreatedEvent((ActivityCommands.ActivityStationCreateCommand)command));
                                    }

                                    if (command instanceof ActivityCommands.ActivityJoinCommand) {
                                        event.set(new ActivityEvents.ActivityJoinedEvent((ActivityCommands.ActivityJoinCommand)command));
                                    }
                                }, () -> command.replyTo.tell(new ActivityDetails.ActivityNullDetails()));

                            if (event.get() == null) {
                                return Effect().none();
                            } else {
                                return Effect().persist(event.get());
                            }
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
                .onEvent(ActivityEvents.Event.class, (state, event) -> {
                    if (event instanceof ActivityEvents.ActivityCreatedEvent) {
                        return state.handle((ActivityEvents.ActivityCreatedEvent)event);
                    } else if(event instanceof ActivityEvents.ActivityJoinedEvent) {
                        return state.handle((ActivityEvents.ActivityJoinedEvent) event);
                    } else if(event instanceof ActivityEvents.ActivityStationCreatedEvent) {
                        return state.handle((ActivityEvents.ActivityStationCreatedEvent) event);
                    }
                    return state;
                })
                .onAnyEvent((state, e) -> {
                    return state;
                });
    }

    @Data
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

        public ActivityManager.State handle(ActivityEvents.ActivityStationEvent event) {
            actorContext.getChild(event.command.activityId).ifPresent( action -> {
                ActorRef child = (ActorRef) action;
                child.tell(event.command);
            });
            return this;
        }

        public ActivityManager.State handle(ActivityEvents.ActivityJoinedEvent event) {
            actorContext.getChild(event.command.activityId).ifPresent( action -> {
                ActorRef child = (ActorRef) action;
                child.tell(event.command);
            });
            return this;
        }
    }
}
