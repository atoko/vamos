package org.atoko.call4code.entrado.actors.person;

import akka.actor.typed.ActorRef;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.EventSourcedEntity;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import org.atoko.call4code.entrado.model.details.PersonDetails;

public class PersonActor extends EventSourcedEntity<
        PersonManager.Command, PersonManager.Event, PersonActor.State
        > {
    public static String PERSON_PREFIX = "person;";
    public static EntityTypeKey<PersonManager.Command> entityTypeKey = EntityTypeKey.create(PersonManager.Command.class, "PersonActor;");

    public PersonActor(String personId) {
        super(entityTypeKey, personId);
    }

    // constructor
    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public CommandHandler<PersonManager.Command, PersonManager.Event, PersonActor.State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(PersonManager.PersonCreateCommand.class,
                        command -> Effect().persist(new PersonActor.PersonCreatedEvent(command))
                )
                .onCommand(PersonActor.PersonDetailsPoll.class,
                        (state, command) -> Effect().none().thenRun(() -> command.replyTo.tell(new PersonDetails(
                                state
                        ))))
                .build();
    }

    @Override
    public EventHandler<PersonActor.State, PersonManager.Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(PersonActor.PersonCreatedEvent.class,
                        (state, event) -> PersonActor.State.inception(event))
                .build();
    }

    public static class State {
        public PersistenceId deviceId = PersistenceId.apply("CAFE");
        public PersistenceId personId = PersistenceId.apply("BEBA");
        public String firstName = "";
        public String lastName = "";
        public String pin = "";

        public State() {
        }

        public State(PersonActor.PersonCreatedEvent event) {
            this.deviceId = PersistenceId.apply(event.getDeviceId());
            this.personId = PersistenceId.apply(event.getPersonId());
            this.firstName = event.firstName;
            this.lastName = event.lastName;
            this.pin = event.pin;
        }

        public static PersonActor.State inception(PersonActor.PersonCreatedEvent event) {
            return new PersonActor.State(event);
        }
    }

    public static class PersonDetailsPoll implements PersonManager.Command {
        ActorRef<PersonDetails> replyTo;
        String id;

        public <U> PersonDetailsPoll(ActorRef<PersonDetails> replyTo, String id) {
            this.replyTo = replyTo;
            this.id = id;
        }
    }

    public static class PersonCreatedEvent implements PersonManager.Event {
        String deviceId;
        String personId;
        String firstName;
        String lastName;
        String pin;

        public PersonCreatedEvent(String deviceId, String personId, String firstName, String lastName, String pin) {
            this.deviceId = deviceId;
            this.personId = personId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.pin = pin;
        }

        public PersonCreatedEvent(PersonManager.PersonCreateCommand command) {
            this.deviceId = command.deviceId;
            this.personId = command.personId;
            this.firstName = command.getFirstName();
            this.lastName = command.getLastName();
            this.pin = command.getPin();
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getPersonId() {
            return personId;
        }
    }

}