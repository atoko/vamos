package org.atoko.call4code.entrado.service;

import akka.pattern.Patterns;
import org.atoko.call4code.entrado.actors.PersonActor;
import org.atoko.call4code.entrado.actors.meta.ActivityManager;
import org.atoko.call4code.entrado.actors.meta.DeviceSupervisor;
import org.atoko.call4code.entrado.exception.ResponseCodeException;
import org.atoko.call4code.entrado.model.PersonDetails;
import org.atoko.call4code.entrado.service.meta.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;
import reactor.core.publisher.Mono;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.atoko.call4code.entrado.actors.PersonActor.PERSON_PREFIX;
import static org.atoko.call4code.entrado.utils.MonoConverter.toMono;

@Component
public class ActivityService {

    FiniteDuration duration = FiniteDuration.create(1, TimeUnit.SECONDS);
    @Autowired
    private DeviceService deviceService;

    @PostConstruct
    private void buildCommands() {

    }

    public Mono<String> create(String name) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Future<Object> create = Patterns.ask(
                deviceService.get(),
                new ActivityManager.ActivityCreateMessage(
                        deviceService.getDeviceId(),
                        id,
                        name
                ),
                5000
        );

        return toMono(create).map((na) -> {
            return id;
        });
    }
//
//
//    public Mono<List<PersonDetails>> get(String personId) {
//        if (!StringUtils.isEmpty(personId)) {
//            return getById(personId).map(Collections::singletonList);
//        } else {
//            return getAll();
//        }
//
//    }
//
//    public Mono<PersonDetails> getById(String personId) {
//        return toMono(deviceService.child(PERSON_PREFIX + personId).resolveOne(duration))
//                .onErrorResume((t) -> {
//                    throw new ResponseCodeException(HttpStatus.NOT_FOUND, "PERSON_NOT_FOUND", "Person was not found");
//                })
//                .flatMap((ref) -> toMono(Patterns.ask(ref, tellPersonDetailsCommand, 5000))).map((response) -> {
//                    if (response instanceof PersonDetails) {
//                        return ((PersonDetails) response);
//                    } else {
//                        throw new ResponseCodeException(HttpStatus.LOOP_DETECTED, "PERSON_DETAILS_RESPONSE_INVALID", "Could not process message");
//                    }
//                });
//    }
//
//    private Mono<List<PersonDetails>> getAll() {
//
//        return toMono(Patterns.ask(deviceService.get(), pollPersonQueryCommand, 5000))
//                .map((response) -> {
//                    if (response instanceof List) {
//                        return (List<PersonDetails>) response;
//                    }
//                    return Collections.emptyList();
//                });
//    }
}
