package org.atoko.call4code.entrado.actors.person;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.EventSourcedEntity;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import lombok.Data;
import org.atoko.call4code.entrado.actors.meta.DeviceSupervisor;
import org.atoko.call4code.entrado.model.details.PersonDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.atoko.call4code.entrado.actors.person.PersonActor.PERSON_PREFIX;

public class PersonManager extends EventSourcedEntity<
        PersonManager.Command, PersonManager.Event, PersonManager.State
        > {

    public static final String PERSON_MANAGER = "PersonManager*";
    public static EntityTypeKey<PersonManager.Command> entityTypeKey = EntityTypeKey.create(PersonManager.Command.class, "*PersonManager+");
    private static PersonDetails[] personDetails = new PersonDetails[]{};
    private ActorContext actorContext;
    //Refactor to state
    private String deviceId;

    public PersonManager(String deviceId, ActorContext actorContext) {
        super(entityTypeKey, getEntityId(deviceId));
        this.actorContext = actorContext;
        this.deviceId = deviceId;
    }

    public static Behavior<Command> behavior(String deviceId) {
        return Behaviors.setup(actorContext -> new PersonManager(deviceId, actorContext));
    }

    public static String getEntityId(String deviceId) {
        return PERSON_MANAGER + deviceId;
    }

    @Override
    public State emptyState() {
        return new State(null);
    }

    @Override
    public CommandHandler<Command, Event, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(PersonCreateCommand.class,
                        (state, command) -> {
                            ActorRef child = actorContext.spawn(
                                    PersonActor.behavior(command.deviceId, command.personId),
                                    PersonActor.getEntityId(command.deviceId, command.personId)
                            );
                            child.tell(command);

                            return Effect().persist(
                                    new PersonActor.PersonCreatedEvent(command)
                            ).thenRun(() -> command.replyTo.tell(true));
                        })
                .onCommand(PersonActor.PersonDetailsPoll.class,
                        (state, command) -> {
                            actorContext.getChild(PersonActor.getEntityId(deviceId, command.id))
                                    .ifPresentOrElse((action) -> {
                                            ActorRef child = (ActorRef) action;
                                            child.tell(command);
                                        },
                                        () -> command.replyTo.tell(new PersonDetails.PersonNullDetails())
                                    );
                            return Effect().none();
                        })
                .onCommand(PersonQueryPoll.class,
                        (state, poll) -> {
                            return Effect().none().thenRun(() -> {
                                poll.replyTo.tell(
                                        state.map.values().toArray(personDetails)
                                );
                            });
                        })
                .build();
    }

    @Override
    public EventHandler<State, Event> eventHandler() {
        //Handle person created event
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(PersonActor.PersonCreatedEvent.class, (state, event) -> {
                    return state.handle(event);
                })
                .onEvent(PersonManager.Event.class, (state, event) -> {
                    return state;
                })
                .onAnyEvent((state, e) -> {
                    return state;
                });
    }

    public static class Command {
    }

    public static class Event {
    }

    @Data
    public static class PersonCreateCommand extends Command {
        public ActorRef replyTo;
        String deviceId;
        String personId;
        String firstName;
        String lastName;
        String pin;

        public PersonCreateCommand(
                ActorRef replyTo,
                String deviceId,
                String personId,
                String firstName,
                String lastName,
                String pin
        ) {
            this.replyTo = replyTo;
            this.deviceId = deviceId;
            this.personId = personId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.pin = pin;
        }
    }

    public static class PersonQueryPoll extends PersonManager.Command {
        ActorRef<PersonDetails[]> replyTo;

        public PersonQueryPoll(ActorRef<PersonDetails[]> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public class State {
        private Map<String, PersonDetails> map;

        public State(Map<String, PersonDetails> map) {
            if (map == null) {
                this.map = new HashMap<>();
            } else {
                this.map = new HashMap<>(map);
            }
        }

        public State handle(PersonActor.PersonCreatedEvent event) {
            map.put(event.personId, new PersonDetails(
                    event.deviceId,
                    event.personId,
                    event.firstName,
                    event.lastName,
                    event.pin
            ));

            return new State(this.map);
        }
    }

}
