package org.atoko.call4code.entrado.service;

import akka.actor.ActorPath;
import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import org.atoko.call4code.entrado.actors.ActivityActor;
import org.atoko.call4code.entrado.actors.meta.ActivityManager;
import org.atoko.call4code.entrado.exception.ResponseCodeException;
import org.atoko.call4code.entrado.model.details.ActivityDetails;
import org.atoko.call4code.entrado.service.meta.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.atoko.call4code.entrado.actors.ActivityActor.ACTIVITY_PREFIX;
import static org.atoko.call4code.entrado.actors.meta.DeviceSupervisor.Children.ACTIVITY_MANAGER;
import static org.atoko.call4code.entrado.utils.MonoConverter.toMono;

@Component
public class ActivityService {

    FiniteDuration duration = FiniteDuration.create(1, TimeUnit.SECONDS);
    @Autowired
    private DeviceService deviceService;


    private ActivityActor.ActivityDetailsPoll pollActivityDetails;

    @PostConstruct
    private void buildCommands() {
        pollActivityDetails = new ActivityActor.ActivityDetailsPoll();
    }

    public Mono<ActivityDetails> create(String name) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Future<Object> create = Patterns.ask(
                deviceService.child(() -> deviceService.path().child(ACTIVITY_MANAGER)),
                new ActivityManager.ActivityCreateMessage(
                        deviceService.getDeviceId(),
                        id,
                        name
                ),
                5000
        );

        return toMono(create).map((na) -> {
            return new ActivityDetails(
                    deviceService.getDeviceId(),
                    id,
                    name
            );
        });
    }

    public Mono<List<ActivityDetails>> get(String personId) {
        if (!StringUtils.isEmpty(personId)) {
            return getById(personId).map(Collections::singletonList);
        } else {
            return Mono.just(Collections.EMPTY_LIST);//getAll();
        }

    }
//
    public Mono<ActivityDetails> getById(String activityId) {
        ActorSelection actorSelection = deviceService.child(
                () -> deviceService.path().child(ACTIVITY_MANAGER).child(ACTIVITY_PREFIX + activityId)
        );
        return toMono(actorSelection.resolveOne(duration))
                .onErrorResume((t) -> {
                    throw new ResponseCodeException(HttpStatus.NOT_FOUND, "ACTIVITY_NOT_FOUND", "Activity was not found");
                })
                .flatMap((ref) -> toMono(Patterns.ask(ref, pollActivityDetails, 5000))).map((response) -> {
                    if (response instanceof ActivityDetails) {
                        return ((ActivityDetails) response);
                    } else {
                        throw new ResponseCodeException(HttpStatus.LOOP_DETECTED, "ACTIVITY_DETAILS_RESPONSE_INVALID", "Could not process message");
                    }
                });
    }
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
