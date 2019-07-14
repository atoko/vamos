package org.atoko.call4code.entrado.actors;


import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import org.atoko.call4code.entrado.model.PersonDetails;
import org.atoko.call4code.entrado.service.PersonService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

import static org.atoko.call4code.entrado.actors.PersonActor.PERSON_PREFIX;
import static org.atoko.call4code.entrado.utils.MonoConverter.toMono;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SessionActor extends UntypedActor {
    private final PersonService personService;

    public static Props props(PersonService personService) {
        // You need to specify the actual type of the returned actor
        // since Java 8 lambdas have some runtime type information erased
        return Props.create(SessionActor.class, () -> new SessionActor(personService));
    }

    public SessionActor(PersonService personService) {
        this.personService = personService;
    }

    // constructor

    private void onReceive(AddPerson addPerson) {
        getContext().actorOf(
                PersonActor.props(
                        addPerson.fname,
                        addPerson.lname,
                        addPerson.pin,
                        addPerson.id
                ),
                PERSON_PREFIX + addPerson.id);
        getSender().tell(true, getSelf());
    }

    private void onReceive(TellPersonList tellListCommand) {
        ArrayList<Mono<PersonDetails>> tellCommands = new ArrayList<>();
        PersonActor.TellDetails tellCommand = new PersonActor.TellDetails();
        Iterable<ActorRef> children = getContext().getChildren();
        children.forEach((c) -> {
            tellCommands.add(
                    toMono(Patterns.ask(c, tellCommand, 3L)).map((o) -> {return (PersonDetails)o;})
            );
        });

        if (!tellCommands.isEmpty()) {
            Mono.zip(tellCommands, (Function<Object[], Object>) Arrays::asList)
                .doOnSuccess((detailList) -> {
                    getSender().tell(detailList, getSelf());
                }).block();
        } else {
            getSender().tell(Collections.EMPTY_LIST, getSelf());
        }
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof AddPerson) {
            onReceive((AddPerson)message);
        } else if (message instanceof TellPersonList) {
            onReceive((TellPersonList) message);
        } else {
            unhandled(message);
        }
    }

    public static class AddPerson {
        public String id;
        public String pin;
        public String fname;
        public String lname;

        public AddPerson(String fname, String lname, String pin, String id) {
            this.fname = fname;
            this.lname = lname;
            this.pin = pin;
            this.id = id;
        }
    }

    public static class TellPersonList {}
}