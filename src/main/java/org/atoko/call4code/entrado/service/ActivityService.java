package org.atoko.call4code.entrado.service;

import akka.actor.typed.ActorRef;
import org.atoko.call4code.entrado.actors.activity.ActivityActor;
import org.atoko.call4code.entrado.actors.activity.ActivityManager;
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

    public Mono<ActivityDetails> create(String name) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        CompletionStage<Boolean> create = actorSystemService.get().ask((replyTo) ->
                        new ActivityManager.ActivityCreateCommand(
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
                    name
            );
        });
    }

    public Mono<List<ActivityDetails>> get(String id) {
        if (!StringUtils.isEmpty(id)) {
            return getById(id).map(Collections::singletonList);
        } else {
            return getAll().map(List::of);
        }
    }

    public Mono<ActivityDetails> getById(String id) {
        return toMono(actorSystemService.get().ask((replyTo) ->
                new ActivityActor.ActivityDetailsPoll((ActorRef) replyTo, id), duration));
    }

    private Mono<ActivityDetails[]> getAll() {
        return toMono(actorSystemService.get().ask((replyTo) ->
                new ActivityManager.ActivityQueryPoll((ActorRef) replyTo), duration));
    }
}
