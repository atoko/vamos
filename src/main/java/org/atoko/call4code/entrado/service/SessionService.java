package org.atoko.call4code.entrado.service;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import org.atoko.call4code.entrado.actors.SessionActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionService {
    ActorRef sessionReference;
    @Autowired
    private ActorSystem actorSystem;

    public SessionService(@Autowired ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
        sessionReference = actorSystem.actorOf(SessionActor.props(null), "session");
    }

    public ActorRef get() {
        return sessionReference;
    }

    public ActorSelection child(String id) {
        return actorSystem.actorSelection(sessionReference.path().child(id));
    }
}
