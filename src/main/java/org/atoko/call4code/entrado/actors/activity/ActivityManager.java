package org.atoko.call4code.entrado.actors.activity;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.EventSourcedEntity;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import org.atoko.call4code.entrado.model.details.ActivityDetails;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.atoko.call4code.entrado.actors.activity.ActivityActor.ACTIVITY_PREFIX;

public class ActivityManager extends EventSourcedEntity<
        ActivityCommands.Command, ActivityEvents.Event, ActivityManager.State
        > {

    public static final String ACTIVITY_MANAGER = "ActivityManager_;";
    public static EntityTypeKey<ActivityCommands.Command> entityTypeKey = EntityTypeKey.create(ActivityCommands.Command.class, "ActivityManager;");
    private static ActivityDetails[] activityDetails = new ActivityDetails[]{};
    private ActorContext actorContext;

    private Map<String, ActivityDetails> map = new HashMap<>();

    public ActivityManager(String deviceId, ActorContext actorContext) {
        super(entityTypeKey, getEntityId(deviceId));
        this.actorContext = actorContext;
    }

    public static Behavior<ActivityCommands.Command> behavior(String persistenceId) {
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
    public CommandHandler<ActivityCommands.Command, ActivityEvents.Event, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(ActivityCommands.ActivityCreateCommand.class,
                        (state, event) -> {
                            ActorRef child = actorContext.spawn(
                                    new ActivityActor(
                                            event.activityId)
                                    ,
                                    ACTIVITY_PREFIX + event.activityId
                            );
                            child.tell(event);
                            map.put(event.activityId, new ActivityDetails(
                                    event.deviceId,
                                    event.activityId,
                                    event.name,
                                    Collections.EMPTY_LIST
                            ));
                            return Effect().none().thenRun(() -> event.replyTo.tell(true));
                        })
                .onCommand(ActivityCommands.ActivityTargetedCommand.class,
                        (state, command) -> {
                            actorContext.getChild(ACTIVITY_PREFIX + command.activityId)
                                    .ifPresentOrElse((action) -> {
                                        ActorRef child = (ActorRef) action;
                                        child.tell(command);
                                    }, () -> {
                                        command.replyTo.tell(new ActivityDetails.ActivityNullDetails());
                                    });
                            return Effect().none();
                        })
                .onCommand(ActivityCommands.ActivityQueryPoll.class,
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
    public EventHandler<State, ActivityEvents.Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState().build();
    }

    public class State {
    }
}
