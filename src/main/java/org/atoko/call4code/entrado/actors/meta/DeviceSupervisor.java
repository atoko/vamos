package org.atoko.call4code.entrado.actors.meta;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.EventSourcedEntity;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import org.atoko.call4code.entrado.actors.activity.ActivityActor;
import org.atoko.call4code.entrado.actors.activity.ActivityCommands;
import org.atoko.call4code.entrado.actors.activity.ActivityManager;
import org.atoko.call4code.entrado.actors.person.PersonActor;
import org.atoko.call4code.entrado.actors.person.PersonManager;

import java.util.Date;
import java.util.UUID;

import static org.atoko.call4code.entrado.actors.activity.ActivityManager.ACTIVITY_MANAGER;
import static org.atoko.call4code.entrado.actors.person.PersonManager.PERSON_MANAGER;

public class DeviceSupervisor extends EventSourcedEntity<
        Object, DeviceSupervisor.Event, DeviceSupervisor.State
        > {

    public static EntityTypeKey<Object> entityTypeKey =
            EntityTypeKey.create(Object.class, "*DeviceSupervisor+");
    ActorRef personManager;
    ActorRef activityManager;
    private ActorContext actorContext;

    public DeviceSupervisor(String persistenceId, ActorContext actorContext) {
        super(entityTypeKey, persistenceId);
        this.actorContext = actorContext;
    }

    public static String getEntityId(String persistenceId) {
        return "DeviceSupervisor*" + persistenceId;
    }

    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public CommandHandler<Object, Event, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(GenesisCommand.class, command -> {
                    activityManager = actorContext.spawn(ActivityManager.behavior(command.deviceId), ACTIVITY_MANAGER + command.deviceId);
                    return Effect().persist(new StartedEvent());
                })
                .onAnyCommand((state, command) -> {
                    if (command instanceof ActivityCommands.Command) {
                        activityManager.tell(command);
                    }

                    return Effect().none();
                });
    }

    @Override
    public EventHandler<State, Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(StartedEvent.class, (state, event) -> { return state; })
                .build();
    }

    public interface Command {
    }

    public static class Event {
    }

    public static class StartedEvent extends Event {
        String guid = UUID.randomUUID().toString();
        String timestamp = String.valueOf(new Date().getTime());
    }

    public static class GenesisCommand implements Command {
        public String deviceId;

        public GenesisCommand(String deviceId) {
            this.deviceId = deviceId;
        }
    }

    public class State {
    }
}
