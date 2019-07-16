package org.atoko.call4code.entrado.actors;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import org.atoko.call4code.entrado.model.PersonDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PersonActorTests extends ActorTest {

    @Test
    void it_returns_PersonDetail_when_TellDetail_command_is_sent() throws Exception {
        String fname = "John";
        String lname = "Doe";
        String id = UUID.randomUUID().toString();
        String pin = UUID.randomUUID().toString();

        ActorRef greeter = system
                .actorOf(
                        PersonActor.props(fname, lname, pin, id),
                        "greeter"
                );
        Future<Object> future = Patterns.ask(greeter, new PersonActor.TellDetails(), 3L);
        PersonDetails result = (PersonDetails)Await.result(future, Duration.create(3, TimeUnit.SECONDS));

        Assertions.assertEquals(fname, result.firstName);
        Assertions.assertEquals(lname, result.lastName);
        Assertions.assertEquals(id, result.id);
    }
}
