package org.atoko.call4code.entrado.service;

import akka.actor.typed.ActorRef;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import org.atoko.call4code.entrado.actors.person.PersonActor;
import org.atoko.call4code.entrado.actors.person.PersonCommands;
import org.atoko.call4code.entrado.actors.person.PersonManager;
import org.atoko.call4code.entrado.model.details.PersonDetails;
import org.atoko.call4code.entrado.service.meta.ActorSystemService;
import org.atoko.call4code.entrado.service.meta.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static org.atoko.call4code.entrado.utils.MonoConverter.toMono;

@Component
public class PersonService {

    static public Duration duration = Duration.ofSeconds(4);

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private ActorSystemService actorSystemService;

    private EntityRef getShard() {
        String deviceId = deviceService.getDeviceId();
        return actorSystemService.child(PersonManager.entityTypeKey(deviceId), PersonManager.getEntityId(deviceId));
    }

    public Mono<PersonDetails> create(String firstName, String lastName, String pin) {
        String personId = UUID.randomUUID().toString().substring(0, 8);

        CompletionStage<Boolean> create = getShard().ask(
                (replyTo) ->
                        new PersonCommands.PersonCreateCommand(
                                (ActorRef) replyTo,
                                deviceService.getDeviceId(),
                                personId,
                                firstName,
                                lastName,
                                pin
                        ),
                duration
        );

        return toMono(create).map((na) -> {
            return new PersonDetails(
                    deviceService.getDeviceId(),
                    personId,
                    firstName,
                    lastName,
                    pin
            );
        });
    }


    public Mono<List<PersonDetails>> get(String id) {
        if (!StringUtils.isEmpty(id)) {
            return getById(id).map(Collections::singletonList);
        } else {
            return getAll().map(List::of);
        }
    }

    public Mono<PersonDetails> getById(String id) {
        return toMono(getShard().ask((replyTo) ->
                new PersonCommands.PersonDetailsPoll((ActorRef) replyTo, PersonActor.getEntityId(deviceService.getDeviceId(), id)), duration));
    }

    private Mono<PersonDetails[]> getAll() {
        return toMono(getShard().ask((replyTo) -> new PersonCommands.PersonQueryPoll((ActorRef) replyTo), duration));
    }
}
