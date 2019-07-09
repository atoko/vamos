package org.atoko.call4code.entrado.actors;

import akka.actor.ActorSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ActorTest {
    @Autowired
    ActorSystem system;
}
