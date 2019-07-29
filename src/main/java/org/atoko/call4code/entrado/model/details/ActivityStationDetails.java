package org.atoko.call4code.entrado.model.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.atoko.call4code.entrado.actors.activity.ActivityActor;
import org.atoko.call4code.entrado.actors.activity.ActivityStationState;
import org.atoko.call4code.entrado.model.identifiers.PersonIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class ActivityStationDetails {
    @JsonIgnore
    public String deviceId = "";
    @JsonIgnore
    public String activityId = "";
    public String stationId = "";
    public String name = "";
    public PersonIdentifier assignedPersonId;
    public List<PersonIdentifier> queue = Collections.emptyList();

    public ActivityStationDetails(ActivityActor.State actor, ActivityStationState station) {
        this.deviceId = actor.deviceId.id();
        this.activityId = actor.activityId.id();
        this.stationId = station.stationId;
        this.assignedPersonId = station.assignedTo;
        this.name = station.name;
        this.queue = new ArrayList(station.queue);
    }

    public ActivityStationDetails(String deviceId, String activityId, String stationId, String name, PersonIdentifier assignedPersonId) {
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
            super("", "", "" , "", PersonIdentifier.empty);
        }
    }
}
