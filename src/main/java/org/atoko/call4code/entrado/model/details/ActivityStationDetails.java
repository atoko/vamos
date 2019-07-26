package org.atoko.call4code.entrado.model.details;

import lombok.Data;
import org.atoko.call4code.entrado.actors.activity.ActivityActor;
import org.atoko.call4code.entrado.actors.activity.ActivityStationState;

import java.util.Collections;
import java.util.List;

@Data
public class ActivityStationDetails {
    public String deviceId = "";
    public String activityId = "";
    public String stationId = "";
    public String name = "";
    public String assignedPersonId = "";

    public ActivityStationDetails(ActivityActor.State actor, ActivityStationState station) {
        this.deviceId = actor.deviceId.id();
        this.activityId = actor.activityId.id();
        this.stationId = station.id;
        this.name = station.name;
    }

    public ActivityStationDetails(String deviceId, String activityId, String stationId, String name, String assignedPersonId) {
        this.deviceId = deviceId;
        this.activityId = activityId;
        this.stationId = stationId;
        this.name = name;
        this.assignedPersonId = assignedPersonId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getUniqueId() {
        return String.format("%s;%s;%s", deviceId, activityId, stationId);
    }

    public static class ActivityStationNullDetails extends ActivityStationDetails {
        public ActivityStationNullDetails() {
            super("", "", "" , "", "");
        }
    }
}
