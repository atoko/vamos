package org.atoko.call4code.entrado.service;

import akka.actor.typed.ActorRef;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import org.atoko.call4code.entrado.actors.activity.ActivityActor;
import org.atoko.call4code.entrado.actors.activity.ActivityCommands;
import org.atoko.call4code.entrado.actors.activity.ActivityManager;
import org.atoko.call4code.entrado.actors.person.PersonActor;
import org.atoko.call4code.entrado.exception.ResponseCodeException;
import org.atoko.call4code.entrado.model.details.ActivityDetails;
import org.atoko.call4code.entrado.model.details.ActivityStationDetails;
import org.atoko.call4code.entrado.model.identifiers.PersonIdentifier;
import org.atoko.call4code.entrado.service.meta.ActorSystemService;
import org.atoko.call4code.entrado.service.meta.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
public class ActivityStationService {
    static public Duration duration = Duration.ofSeconds(4);

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private ActorSystemService actorSystemService;

    private EntityRef getShard() {
        String deviceId = deviceService.getDeviceId();
        return actorSystemService.child(ActivityManager.entityTypeKey(deviceId), ActivityManager.getEntityId(deviceId));
    }

    public Mono<ActivityDetails> create(ActivityDetails activityDetails, String name) {
        if (StringUtils.isEmpty(activityDetails.activityId)) {
            throw new ResponseCodeException(HttpStatus.BAD_REQUEST, "ACTIVITY_ID_INVALID");
        }

        String id = UUID.randomUUID().toString().substring(0, 8);
        CompletionStage<ActivityDetails> create = getShard().ask((replyTo) ->
                        new ActivityCommands.ActivityStationCreateCommand(
                                (ActorRef) replyTo,
                                ActivityActor.getEntityId(activityDetails.deviceId, activityDetails.activityId),
                                id,
                                name
                        ),
                duration
        );

        return toMono(create).map((na) -> {
            return na;
        });
    }

    public Mono<ActivityDetails> join(String activityId, String stationId, String personId) {
        return toMono(getShard().ask((replyTo) ->
                new ActivityCommands.ActivityStationJoinQueueCommand(
                        (ActorRef) replyTo,
                        ActivityActor.getEntityId(deviceService.getDeviceId(), activityId),
                        stationId,
                        new PersonIdentifier(deviceService.getDeviceId(), personId)
                ), duration));
    }

    public Mono<ActivityDetails> assign(String activityId, String stationId, String personId) {
        return toMono(getShard().ask((replyTo) ->
                new ActivityCommands.ActivityStationAssignCommand(
                        (ActorRef) replyTo,
                        ActivityActor.getEntityId(deviceService.getDeviceId(), activityId),
                        stationId,
                        new PersonIdentifier(deviceService.getDeviceId(), personId)
                ), duration));
    }
}
