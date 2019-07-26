package org.atoko.call4code.entrado.service;

import akka.actor.typed.ActorRef;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import org.atoko.call4code.entrado.actors.activity.ActivityActor;
import org.atoko.call4code.entrado.actors.activity.ActivityCommands;
import org.atoko.call4code.entrado.actors.activity.ActivityManager;
import org.atoko.call4code.entrado.actors.person.PersonActor;
import org.atoko.call4code.entrado.model.details.ActivityDetails;
import org.atoko.call4code.entrado.service.meta.ActorSystemService;
import org.atoko.call4code.entrado.service.meta.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static org.atoko.call4code.entrado.utils.MonoConverter.toMono;

@Component
public class ActivityService {
    static public Duration duration = Duration.ofSeconds(4);

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private ActorSystemService actorSystemService;

    private EntityRef getShard() {
        String deviceId = deviceService.getDeviceId();
        return actorSystemService.child(ActivityManager.entityTypeKey(deviceId), ActivityManager.getEntityId(deviceId));
    }

    public Mono<ActivityDetails> create(String name) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        CompletionStage<Boolean> create = getShard().ask((replyTo) ->
                        new ActivityCommands.ActivityCreateCommand(
                                (ActorRef) replyTo,
                                deviceService.getDeviceId(),
                                id,
                                name
                        ),
                duration
        );

        return toMono(create).map((na) -> {
            return new ActivityDetails(
                    deviceService.getDeviceId(),
                    id,
                    name,
                    Collections.EMPTY_LIST
            );
        });
    }

    public Mono<ActivityDetails> join(String activityId, String personId) {
        return toMono(getShard().ask((replyTo) ->
                new ActivityCommands.ActivityJoinCommand(
                        (ActorRef) replyTo,
                        ActivityActor.getEntityId(deviceService.getDeviceId(), activityId),
                        PersonActor.getEntityId(deviceService.getDeviceId(), personId)
                ), duration));
    }

    public Mono<List<ActivityDetails>> get(String id) {
        if (!StringUtils.isEmpty(id)) {
            return getById(id).map(Collections::singletonList);
        } else {
            return getAll().map(List::of);
        }
    }

    public Mono<ActivityDetails> getById(String id) {
        return toMono(getShard().ask((replyTo) ->
                new ActivityCommands.ActivityDetailsPoll(
                        (ActorRef) replyTo,
                        ActivityActor.getEntityId(deviceService.getDeviceId(), id)
                ), duration));
    }

    private Mono<ActivityDetails[]> getAll() {
        return toMono(getShard().ask((replyTo) ->
                new ActivityCommands.ActivityQueryPoll(
                        (ActorRef) replyTo
                ), duration));
    }
}
