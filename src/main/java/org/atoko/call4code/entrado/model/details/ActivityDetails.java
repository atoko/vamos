package org.atoko.call4code.entrado.model.details;

import lombok.Data;
import org.atoko.call4code.entrado.actors.ActivityActor;

@Data
public class ActivityDetails {
    public String deviceId = "";
    public String activityId = "";
    public String name = "";

    public ActivityDetails(ActivityActor actor) {
        this.deviceId = actor.deviceId;
        this.activityId = actor.activityId;
        this.name = actor.name;
    }

    public ActivityDetails(String deviceId, String activityId,  String name) {
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
