package org.atoko.call4code.entrado.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.atoko.call4code.entrado.actors.PersonActor;

@Data
public class PersonDetails {
    public String id = "";
    public String firstName = "";
    public String lastName = "";
    public String deviceId = "";
    @JsonIgnore
    public String pin = "";

    public PersonDetails(PersonActor actor, String deviceId) {
        this.id = actor.id;
        this.firstName = actor.firstName;
        this.lastName = actor.lastName;
        this.deviceId = deviceId;
        this.pin = actor.pin;
    }

    public PersonDetails(String id, String firstName, String lastName, String deviceId, String pin) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.deviceId = deviceId;
        this.pin = pin;
    }

    public String getUniqueId() {
        return String.format("%s;%s", deviceId, id);
    }

}
