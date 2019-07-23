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
import org.atoko.call4code.entrado.model.details.PersonDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.atoko.call4code.entrado.actors.person.PersonActor.PERSON_PREFIX;

public class PersonManager extends EventSourcedEntity<
        PersonManager.Command, PersonManager.Event, PersonManager.State
        > {

    public static final String PERSON_MANAGER = "PersonManager_;";
    public static EntityTypeKey<PersonManager.Command> entityTypeKey = EntityTypeKey.create(PersonManager.Command.class, "PersonManager;");
    private static PersonDetails[] personDetails = new PersonDetails[]{};
    private ActorContext actorContext;

    private Map<String, PersonDetails> map = new HashMap<>();

    public PersonManager(String deviceId, ActorContext actorContext) {
        super(entityTypeKey, getEntityId(deviceId));
        this.actorContext = actorContext;
    }

    public static Behavior<Command> behavior(String deviceId) {
        return Behaviors.setup(actorContext -> new PersonManager(deviceId, actorContext));
    }

    public static String getEntityId(String deviceId) {
        return PERSON_MANAGER;
    }

    @Override
    public State emptyState() {
        return null;
    }

    @Override
    public CommandHandler<Command, Event, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(PersonCreateCommand.class,
                        (state, command) -> {
                            ActorRef child = actorContext.spawn(
                                    new PersonActor(
                                            command.personId
                                    ),
                                    PERSON_PREFIX + command.personId
                            );
                            child.tell(command);
                            map.put(command.personId, new PersonDetails(
                                    command.deviceId,
                                    command.personId,
                                    command.firstName,
                                    command.lastName,
                                    command.pin
                            ));

                            return Effect().none().thenRun(() -> command.replyTo.tell(true));
                        })
                .onCommand(PersonActor.PersonDetailsPoll.class,
                        (state, command) -> {
                            actorContext.getChild(PERSON_PREFIX + command.id)
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
                                        map.values().toArray(personDetails)
                                );
                            });
                        })
                .build();
    }

    @Override
    public EventHandler<State, Event> eventHandler() {
        //Handle person created event
        return newEventHandlerBuilder()
                .forAnyState().build();
    }

    public interface Command {
    }

    public interface Event {
    }

    @Data
    public static class PersonCreateCommand implements Command {
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

    public static class PersonQueryPoll implements PersonManager.Command {
        ActorRef<PersonDetails[]> replyTo;

        public PersonQueryPoll(ActorRef<PersonDetails[]> replyTo) {
            this.replyTo = replyTo;
        }

        public PersonQueryPoll(Supplier<ActorRef<PersonDetails[]>> f) {
            this.replyTo = f.get();
        }
    }

    public class State {
    }

}
