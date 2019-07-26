package org.atoko.call4code.entrado.model.details;

import lombok.Data;
import org.atoko.call4code.entrado.actors.activity.ActivityActor;

import java.util.Collections;
import java.util.List;

@Data
public class ActivityDetails {
    public String deviceId = "";
    public String activityId = "";
    public String name = "";
    public List<String> personIds = Collections.EMPTY_LIST;

    public ActivityDetails(ActivityActor.State actor) {
        this.deviceId = actor.deviceId.id();
        this.activityId = actor.activityId.id();
        this.name = actor.name;
        this.personIds = actor.personIds;
    }

    public ActivityDetails(String deviceId, String activityId, String name, List<String> personIds) {
        this.deviceId = deviceId;
        this.activityId = activityId;
        this.name = name;
        this.personIds = personIds;
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

    public static class ActivityNullDetails extends ActivityDetails {
        public ActivityNullDetails() {
            super("", "", "", Collections.emptyList());
        }
    }
}
