package org.atoko.call4code.entrado.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import org.atoko.call4code.entrado.actors.PersonActor;
import org.atoko.call4code.entrado.actors.SessionActor;
import org.atoko.call4code.entrado.config.SpringExtension;
import org.atoko.call4code.entrado.model.PersonDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import scala.compat.java8.FutureConverters;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.atoko.call4code.entrado.actors.PersonActor.PERSON_PREFIX;
import static org.atoko.call4code.entrado.utils.MonoConverter.toMono;

@Component
public class PersonService {

    public static final SessionActor.TellPersonList tellPersonListCommand = new SessionActor.TellPersonList();
    @Autowired
    private SessionService sessionService;


    FiniteDuration duration = FiniteDuration.create(1, TimeUnit.SECONDS);

    private Props props = null;

    public Mono<PersonDetails> create(String fname, String lname, String pin) {
        String id = UUID.randomUUID().toString().substring(0, 4);
        Future<Object> create = Patterns.ask(sessionService.get(), new SessionActor.AddPerson(fname, lname, pin, id), 5000);
        return toMono(create).map((na) -> { return new PersonDetails(fname, lname, id); });
    }


    public Mono<PersonDetails> get(String id) {
        return toMono(sessionService.child(PERSON_PREFIX + id).resolveOne(duration))
            .flatMap((ref) -> {
                return toMono(Patterns.ask(ref, new PersonActor.TellDetails(), 5000));
            }).map((response) -> {
            if (response instanceof PersonDetails) {
                return (PersonDetails)response;
            }
            return new PersonDetails("none", "none", "none");
        });
    }

    public Mono<List<PersonDetails>> getAll() {
        return toMono(Patterns.ask(sessionService.get(), tellPersonListCommand, 5000))
            .map((response) -> {
                if (response instanceof List) {
                    return (List<PersonDetails>)response;
                }
                return Collections.emptyList();
            });
    }
}
