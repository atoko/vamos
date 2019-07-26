package org.atoko.call4code.entrado.actors.person;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.EventSourcedEntity;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import org.atoko.call4code.entrado.model.details.PersonDetails;

public class PersonActor extends EventSourcedEntity<
        PersonManager.Command, PersonManager.Event, PersonActor.State
        > {
    public static String PERSON_PREFIX = "person*";
    public static String getEntityId(String sourceId, String personId) {
        return PERSON_PREFIX + personId + "&" + sourceId;
    }
    public static EntityTypeKey<PersonManager.Command> entityTypeKey = EntityTypeKey.create(PersonManager.Command.class, "PersonActor*+");
    private State _state = emptyState();

    public PersonActor(PersonCreatedEvent event) {
        super(entityTypeKey, getEntityId(event.deviceId, event.personId));
    }

    public PersonActor(String sourceId, String personId) {
        super(entityTypeKey, getEntityId(sourceId, personId));
    }
    public static Behavior<PersonManager.Command> behavior(PersonCreatedEvent event) {
        return Behaviors.setup(actorContext -> new PersonActor(event));
    }

    public boolean shouldSnapshot(State state, PersonManager.Event event, long sequenceNr) {
        return event instanceof PersonCreatedEvent;
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
                .onCommand(PersonActor.PersonGenesis.class, (state, command) -> {
                    this._state = new State(command.event);
                    return Effect().none();
                })
                .onCommand(PersonActor.PersonDetailsPoll.class, (state, command) ->
                        Effect().none().thenRun(() -> command.replyTo.tell(new PersonDetails(
                                this._state
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
        public PersistenceId sourceId = PersistenceId.apply("CAFE");
        public PersistenceId personId = PersistenceId.apply("BEBA");
        public String firstName = "";
        public String lastName = "";
        public String pin = "";

        public State() {
        }

        public State(PersonActor.PersonCreatedEvent event) {
            this.sourceId = PersistenceId.apply(event.getDeviceId());
            this.personId = PersistenceId.apply(event.getPersonId());
            this.firstName = event.firstName;
            this.lastName = event.lastName;
            this.pin = event.pin;
        }

        public static PersonActor.State inception(PersonActor.PersonCreatedEvent event) {
            return new PersonActor.State(event);
        }
    }

    public static class PersonDetailsPoll extends PersonManager.Command {
        ActorRef<PersonDetails> replyTo;
        String personId;

        public <U> PersonDetailsPoll(ActorRef<PersonDetails> replyTo, String personId) {
            this.replyTo = replyTo;
            this.personId = personId;
        }
    }

    public static class PersonGenesis extends PersonManager.Command {
        PersonCreatedEvent event;

        public PersonGenesis(PersonCreatedEvent event) {
            this.event = event;
        }
    }

    public static class PersonCreatedEvent extends PersonManager.Event {
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
            this.firstName = command.firstName;
            this.lastName = command.lastName;
            this.pin = command.pin;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getPersonId() {
            return personId;
        }
    }

}