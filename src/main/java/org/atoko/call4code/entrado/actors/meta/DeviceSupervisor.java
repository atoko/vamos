package org.atoko.call4code.entrado.actors.meta;


import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.pattern.Patterns;
import org.atoko.call4code.entrado.actors.PersonActor;
import org.atoko.call4code.entrado.model.details.PersonDetails;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

import static org.atoko.call4code.entrado.actors.PersonActor.PERSON_PREFIX;
import static org.atoko.call4code.entrado.utils.MonoConverter.toMono;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeviceSupervisor extends UntypedAbstractActor {
    private ActorRef activityManager;

    public static class Children {
        public static final String ACTIVITY_MANAGER = "ActivityManager";
    }

    public static Props props() {
        return Props.create(DeviceSupervisor.class, DeviceSupervisor::new);
    }

    @Override
    public void preStart() {
        activityManager = getContext().actorOf(
                ActivityManager.props(),
                Children.ACTIVITY_MANAGER
        );
    }

    @Override
    public void postRestart(Throwable reason) {}

    @Override
    public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
        postStop();
    }

    private void onReceive(PersonAddMessage personAddMessage) {
        getContext().actorOf(
                PersonActor.props(
                    personAddMessage.deviceId,
                    personAddMessage.personId,
                    personAddMessage.fname,
                    personAddMessage.lname,
                    personAddMessage.pin
                ),
                PERSON_PREFIX + personAddMessage.personId);
        getSender().tell(true, getSelf());
    }

    static PersonActor.PersonDetailsPoll tellCommand = new PersonActor.PersonDetailsPoll();
    private void onReceive(PersonQueryPoll tellListCommand) {
        ArrayList<Mono<PersonDetails>> tellCommands = new ArrayList<>();
        Iterable<ActorRef> children = getContext().getChildren();
        children.forEach((c) -> {
            tellCommands.add(
                    toMono(Patterns.ask(c, tellCommand, 3L)).map((o) -> {
                        return (PersonDetails) o;
                    })
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
        if (message instanceof PersonAddMessage) {
            onReceive((PersonAddMessage) message);
        } else if (message instanceof PersonQueryPoll) {
            onReceive((PersonQueryPoll) message);
        } else {
            unhandled(message);
        }
    }

    public static class PersonAddMessage {
        public String deviceId;
        public String personId;
        public String fname;
        public String lname;
        public String pin;

        public PersonAddMessage(String deviceId, String personId, String fname, String lname, String pin) {
            this.deviceId = deviceId;
            this.personId = personId;
            this.fname = fname;
            this.lname = lname;
            this.pin = pin;
        }
    }

    public static class PersonQueryPoll {
        public PersonQueryPoll() {
        }

    }
}