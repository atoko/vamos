package org.atoko.call4code.entrado.actors.meta;

import akka.actor.AbstractActor;
import akka.actor.Props;
import org.atoko.call4code.entrado.actors.ActivityActor;

import static org.atoko.call4code.entrado.actors.ActivityActor.ACTIVITY_PREFIX;

public class ActivityManager extends AbstractActor {
    public static Props props() {
        return Props.create(ActivityManager.class, ActivityManager::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(ActivityCreateMessage.class, this::onReceive)
            .build();
    }

    private void onReceive(ActivityCreateMessage message) {
        getContext().actorOf(
                ActivityActor.props(message.getActivityId(), message.getName()),
                ACTIVITY_PREFIX + message.getActivityId()
        );
        getSender().tell(true, getSelf());
    }

    public static class ActivityCreateMessage {
        String deviceId;
        String activityId;
        String name;

        public ActivityCreateMessage(String deviceId, String activityId, String name) {
            this.deviceId = deviceId;
            this.activityId = activityId;
            this.name = name;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getActivityId() {
            return activityId;
        }

        public String getName() {
            return name;
        }
    }
}
