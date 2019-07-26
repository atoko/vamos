package org.atoko.call4code.entrado.actors.person;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.EventSourcedEntity;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import org.atoko.call4code.entrado.model.details.PersonDetails;

public class PersonActor extends EventSourcedEntity<
        PersonCommands.Command, PersonEvents.Event, PersonActor.State
        > {
    public static String PERSON_PREFIX = "person*";
    public static EntityTypeKey<PersonCommands.Command> entityTypeKey = EntityTypeKey.create(PersonCommands.Command.class, "PersonActor*+");
    private State _state = emptyState();
    public PersonActor(PersonEvents.PersonCreatedEvent event) {
        super(entityTypeKey, getEntityId(event.deviceId, event.personId));
    }

    public PersonActor(String sourceId, String personId) {
        super(entityTypeKey, getEntityId(sourceId, personId));
    }

    public static String getEntityId(String sourceId, String personId) {
        return PERSON_PREFIX + personId + "&" + sourceId;
    }

    public static Behavior<PersonCommands.Command> behavior(PersonEvents.PersonCreatedEvent event) {
        return Behaviors.setup(actorContext -> new PersonActor(event));
    }

    // constructor
    @Override
    public State emptyState() {
        return new State();
    }

    @Override
    public CommandHandler<PersonCommands.Command, PersonEvents.Event, PersonActor.State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(PersonCommands.PersonGenesis.class, (state, command) -> {
                    this._state = new State(command.event);
                    return Effect().none();
                })
                .onCommand(PersonCommands.PersonDetailsPoll.class, (state, command) ->
                        Effect().none().thenRun(() -> command.replyTo.tell(new PersonDetails(
                                this._state
                        ))))
                .build();
    }

    @Override
    public EventHandler<PersonActor.State, PersonEvents.Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
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

        public State(PersonEvents.PersonCreatedEvent event) {
            this.sourceId = PersistenceId.apply(event.getDeviceId());
            this.personId = PersistenceId.apply(event.getPersonId());
            this.firstName = event.firstName;
            this.lastName = event.lastName;
            this.pin = event.pin;
        }

        public static PersonActor.State inception(PersonEvents.PersonCreatedEvent event) {
            return new PersonActor.State(event);
        }
    }

}