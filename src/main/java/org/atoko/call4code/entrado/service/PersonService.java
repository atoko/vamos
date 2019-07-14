package org.atoko.call4code.entrado.service;

import akka.actor.Props;
import akka.pattern.Patterns;
import org.atoko.call4code.entrado.actors.PersonActor;
import org.atoko.call4code.entrado.actors.SessionActor;
import org.atoko.call4code.entrado.exception.ResponseCodeException;
import org.atoko.call4code.entrado.model.PersonDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.util.StringUtils;
import reactor.core.publisher.Mono;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.atoko.call4code.entrado.actors.PersonActor.PERSON_PREFIX;
import static org.atoko.call4code.entrado.utils.MonoConverter.toMono;

@Component
public class PersonService {

    public static final SessionActor.TellPersonList tellPersonListCommand = new SessionActor.TellPersonList();
    FiniteDuration duration = FiniteDuration.create(1, TimeUnit.SECONDS);
    @Autowired
    private SessionService sessionService;
    private Props props = null;

    public Mono<PersonDetails> create(String fname, String lname, String pin) {
        String id = UUID.randomUUID().toString().substring(0, 4);
        Future<Object> create = Patterns.ask(sessionService.get(), new SessionActor.AddPerson(fname, lname, pin, id), 5000);
        return toMono(create).map((na) -> {
            return new PersonDetails(fname, lname, id);
        });
    }


    public Mono<List<PersonDetails>> get(String id) {
        if (!StringUtils.isEmpty(id)) {
            return getById(id).map(Collections::singletonList);
        } else {
            return getAll();
        }

    }

    private Mono<PersonDetails> getById(String id) {
        return toMono(sessionService.child(PERSON_PREFIX + id).resolveOne(duration))
                .onErrorResume((t) -> {
                    throw new ResponseCodeException(HttpStatus.NOT_FOUND, "PERSON_NOT_FOUND", "Person was not found");
                })
                .flatMap((ref) -> toMono(Patterns.ask(ref, new PersonActor.TellDetails(), 5000))).map((response) -> {
                    if (response instanceof PersonDetails) {
                        return ((PersonDetails) response);
                    } else {
                        throw new ResponseStatusException(HttpStatus.LOOP_DETECTED, "Could not process message");
                    }
                });
    }

    private Mono<List<PersonDetails>> getAll() {
        return toMono(Patterns.ask(sessionService.get(), tellPersonListCommand, 5000))
                .map((response) -> {
                    if (response instanceof List) {
                        return (List<PersonDetails>) response;
                    }
                    return Collections.emptyList();
                });
    }
}
