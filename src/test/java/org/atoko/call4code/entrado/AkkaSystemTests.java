package org.atoko.call4code.entrado;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import org.atoko.call4code.entrado.actors.person.PersonActor;
import org.atoko.call4code.entrado.model.details.PersonDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class AkkaSystemTests {
    @Autowired
    ActorSystem system;


    @Test
    void loadsAkka() throws Exception {
        String id = UUID.randomUUID().toString();
        String name = "John";

        ActorRef greeter = system
                .actorOf(
                        PersonActor.props(name, name, id, id),
                        "greeter"
                );
        Future<Object> result = Patterns.ask(greeter, new PersonActor.PersonDetailsPoll(), 3L);

        Assertions.assertEquals(new PersonDetails(name, name, id).personId, ((PersonDetails)Await.result(result, Duration.create(3, TimeUnit.SECONDS))).personId);
    }
}
