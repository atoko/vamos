package org.atoko.call4code.entrado.model.details;

import lombok.Data;
import org.atoko.call4code.entrado.actors.activity.ActivityActor;

@Data
public class ActivityDetails {
    public String deviceId = "";
    public String activityId = "";
    public String name = "";

    public ActivityDetails(ActivityActor.State actor) {
        this.deviceId = actor.deviceId.id();
        this.activityId = actor.activityId.id();
        this.name = actor.name;
    }

    public ActivityDetails(String deviceId, String activityId, String name) {
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

    public String getUniqueId() {
        return String.format("%s;%s", deviceId, activityId);
    }

}
