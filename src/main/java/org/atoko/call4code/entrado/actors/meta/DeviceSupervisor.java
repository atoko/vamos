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
import org.atoko.call4code.entrado.actors.activity.ActivityManager;
import org.atoko.call4code.entrado.actors.person.PersonActor;
import org.atoko.call4code.entrado.actors.person.PersonManager;

import static org.atoko.call4code.entrado.actors.activity.ActivityManager.ACTIVITY_MANAGER;
import static org.atoko.call4code.entrado.actors.person.PersonManager.PERSON_MANAGER;

public class DeviceSupervisor extends EventSourcedEntity<
        Object, DeviceSupervisor.Event, DeviceSupervisor.State
        > {

    public static EntityTypeKey<Object> entityTypeKey =
            EntityTypeKey.create(Object.class, "DeviceSupervisor;");
    ActorRef personManager;
    ActorRef activityManager;
    private ActorContext actorContext;

    public DeviceSupervisor(String deviceId, ActorContext actorContext) {
        super(entityTypeKey, "DeviceSupervisor_" + deviceId);
        this.actorContext = actorContext;
    }

    public static Behavior<Object> behavior(String persistenceId) {
        return Behaviors.setup(actorContext -> new DeviceSupervisor(persistenceId, actorContext));
    }

    @Override
    public State emptyState() {
        return null;
    }

    @Override
    public CommandHandler<Object, Event, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(GenesisCommand.class, command -> {
                    personManager = actorContext.spawn(PersonManager.behavior(command.deviceId), PERSON_MANAGER + command.deviceId);
                    activityManager = actorContext.spawn(ActivityManager.behavior(command.deviceId), ACTIVITY_MANAGER + command.deviceId);
                    return Effect().none();
                })
                .onAnyCommand((state, command) -> {
                    if (command instanceof PersonManager.PersonCreateCommand
                            || command instanceof PersonManager.PersonQueryPoll
                            || command instanceof PersonActor.PersonDetailsPoll
                    ) {
                        personManager.tell(command);
                    } else if (command instanceof ActivityManager.ActivityCreateCommand
                            || command instanceof ActivityManager.ActivityQueryPoll
                            || command instanceof ActivityActor.ActivityDetailsPoll

                    ) {
                        activityManager.tell(command);
                    }
                    return Effect().none();
                });
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

    public static class GenesisCommand implements Command {
        public String deviceId;

        public GenesisCommand(String deviceId) {
            this.deviceId = deviceId;
        }
    }

    public class State {
    }
}
