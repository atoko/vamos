package org.atoko.call4code.entrado.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import org.atoko.call4code.entrado.model.details.ActivityDetails;

public class ActivityActor extends AbstractActor {
    public static String ACTIVITY_PREFIX = "activity;";

    public final String deviceId;
    public final String activityId;
    public final String name;

    public ActivityActor(String deviceId, String activityId, String name) {
        this.deviceId = deviceId;
        this.activityId = activityId;
        this.name = name;
    }

    public static Props props(String deviceId, String personId, String name) {
        return Props.create(ActivityActor.class, () -> new ActivityActor(deviceId, personId, name));
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActivityDetailsPoll.class, this::onReceive)
                .build();
    }

    private void onReceive(ActivityDetailsPoll message) {
        ActivityDetails response = new ActivityDetails(
                this
        );
        getSender().tell(response, getSelf());
    }

    public static class ActivityDetailsPoll {
    }

    public static class ActivityQueryPoll {
    }
}
