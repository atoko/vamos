package org.atoko.call4code.entrado.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.atoko.call4code.entrado.actors.PersonActor;

@Data
public class PersonDetails {
    public String id = "";
    public String deviceId = "";
    public String firstName = "";
    public String lastName = "";
    @JsonIgnore
    public String pin = "";

    public PersonDetails(PersonActor actor) {
        this.id = actor.personId;
        this.deviceId = actor.deviceId;
        this.firstName = actor.firstName;
        this.lastName = actor.lastName;
        this.pin = actor.pin;
    }

    public PersonDetails(String id, String deviceId, String firstName, String lastName, String pin) {
        this.id = id;
        this.deviceId = deviceId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.pin = pin;
    }


    public String getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPin() {
        return pin;
    }

    public String getUniqueId() {
        return String.format("%s;%s", deviceId, id);
    }

}
