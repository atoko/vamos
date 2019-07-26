package org.atoko.call4code.entrado.actors.meta;

import akka.actor.typed.javadsl.ActorContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.EventSourcedEntity;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;

import java.util.Date;
import java.util.UUID;

public class DeviceSupervisor extends EventSourcedEntity<
        DeviceSupervisor.Command, DeviceSupervisor.Event, DeviceSupervisor.State
        > {

    private ActorContext actorContext;

    public DeviceSupervisor(String deviceId, String persistenceId, ActorContext actorContext) {
        super(entityTypeKey(deviceId), persistenceId);
        this.actorContext = actorContext;
    }

    public static EntityTypeKey<DeviceSupervisor.Command> entityTypeKey(String sourceId) {
        return EntityTypeKey.create(DeviceSupervisor.Command.class, "*DeviceSupervisor(" + sourceId + ")+");
    }

    public static String getEntityId(String persistenceId) {
        return "DeviceSupervisor*" + persistenceId;
    }

    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public CommandHandler<Command, Event, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(GenesisCommand.class, command -> {
                    return Effect().persist(new StartedEvent());
                })
                .build();
    }

    @Override
    public EventHandler<State, Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(StartedEvent.class, (state, event) -> {
                    return state;
                })
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
