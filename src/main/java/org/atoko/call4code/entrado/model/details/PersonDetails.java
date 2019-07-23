package org.atoko.call4code.entrado.model.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.atoko.call4code.entrado.actors.person.PersonActor;

@Data
public class PersonDetails {

    public String deviceId = "";
    public String personId = "";
    public String firstName = "";
    public String lastName = "";
    @JsonIgnore
    public String pin = "";

    public PersonDetails(PersonActor.State actor) {
        this.deviceId = actor.deviceId.id();
        this.personId = actor.personId.id();
        this.firstName = actor.firstName;
        this.lastName = actor.lastName;
        this.pin = actor.pin;
    }

    public PersonDetails(String deviceId, String personId, String firstName, String lastName, String pin) {
        this.deviceId = deviceId;
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.pin = pin;
    }


    public String getDeviceId() {
        return deviceId;
    }

    public String getPersonId() {
        return personId;
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
        return String.format("%s;%s", deviceId, personId);
    }

}
