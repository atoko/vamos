package org.atoko.call4code.entrado.actors.person;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.sharding.typed.javadsl.EventSourcedEntity;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.EventHandler;
import org.atoko.call4code.entrado.model.details.PersonDetails;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class PersonManager extends EventSourcedEntity<
        PersonCommands.Command, PersonEvents.Event, PersonManager.State
        > {

    public static final String PERSON_MANAGER = "PersonManager*";
    private static PersonDetails[] personDetails = new PersonDetails[]{};
    private ActorContext actorContext;
    public PersonManager(String sourceId, String persistenceId, ActorContext actorContext) {
        super(entityTypeKey(sourceId), persistenceId);
        this.actorContext = actorContext;
    }

    public static EntityTypeKey<PersonCommands.Command> entityTypeKey(String sourceId) {
        return EntityTypeKey.create(PersonCommands.Command.class, "*PersonManager(" + sourceId + ")+");
    }

    public static Behavior<PersonCommands.Command> behavior(String sourceId, EntityContext<PersonCommands.Command> context) {
        return Behaviors.setup(actorContext -> new PersonManager(sourceId, context.getEntityId(), context.getActorContext()));
    }

    public static String getEntityId(String deviceId) {
        return PERSON_MANAGER + deviceId;
    }

    @Override
    public State emptyState() {
        return new State(null);
    }

    @Override
    public CommandHandler<PersonCommands.Command, PersonEvents.Event, State> commandHandler() {
        return newCommandHandlerBuilder()
                .forAnyState()
                .onCommand(PersonCommands.PersonCreateCommand.class,
                        (state, command) -> {
                            return Effect().persist(
                                    new PersonEvents.PersonCreatedEvent(command)
                            ).thenRun(() -> command.replyTo.tell(true));
                        })
                .onCommand(PersonCommands.PersonDetailsPoll.class,
                        (state, command) -> {
                            actorContext.getChild(command.personId).ifPresentOrElse((action) -> {
                                ActorRef child = (ActorRef) action;
                                child.tell(command);
                            }, () -> {
                                command.replyTo.tell(new PersonDetails.PersonNullDetails());
                            });
                            return Effect().none();
                        })
                .onCommand(PersonCommands.PersonQueryPoll.class,
                        (state, poll) -> {
                            return Effect().none().thenRun(() -> {
                                Collection<PersonDetails> details = state.map.values();
                                if (poll.ids.size() > 0) {
                                    details = details.stream().filter((personDetails1 ->
                                            poll.ids.contains(personDetails1.personId))).collect(Collectors.toList());
                                }
                                poll.replyTo.tell(
                                        details.toArray(personDetails)
                                );
                            });
                        })
                .build();
    }

    @Override
    public EventHandler<State, PersonEvents.Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(PersonEvents.PersonCreatedEvent.class, (state, event) -> {
                    return state.handle(event);
                })
                .onEvent(PersonEvents.Event.class, (state, event) -> {
                    return state;
                })
                .onAnyEvent((state, e) -> {
                    return state;
                });
    }

    public class State {
        private Map<String, PersonDetails> map;

        public State(Map<String, PersonDetails> map) {
            if (map == null) {
                this.map = new WeakHashMap<>();
            } else {
                this.map = new WeakHashMap<>(map);
            }
        }


        public State handle(PersonEvents.PersonCreatedEvent event) {
            map.put(event.personId, new PersonDetails(
                    event.deviceId,
                    event.personId,
                    event.firstName,
                    event.lastName,
                    event.pin
            ));

            actorContext.spawn(
                    PersonActor.behavior(event),
                    PersonActor.getEntityId(event.deviceId, event.personId)
            ).tell(new PersonCommands.PersonRegister(event));

            return new State(this.map);
        }
    }

}
