package org.atoko.call4code.entrado.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class ActivityActor extends AbstractActor {
    public static String ACTIVITY_PREFIX = "activity;";

    public final String id;
    public final String name;

    public ActivityActor(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Props props(String id, String name) {
        return Props.create(ActivityActor.class, () -> new ActivityActor(id, name));
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .build();
    }
}
