package org.atoko.call4code.entrado.actors.person;

import lombok.Data;

public class PersonEvents {
    public static class Event {
    }

    @Data
    public static class PersonCreatedEvent extends Event {
        String deviceId;
        String personId;
        String firstName;
        String lastName;
        String pin;

        public PersonCreatedEvent(String deviceId, String personId, String firstName, String lastName, String pin) {
            this.deviceId = deviceId;
            this.personId = personId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.pin = pin;
        }

        public PersonCreatedEvent(PersonCommands.PersonCreateCommand command) {
            this.deviceId = command.deviceId;
            this.personId = command.personId;
            this.firstName = command.firstName;
            this.lastName = command.lastName;
            this.pin = command.pin;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getPersonId() {
            return personId;
        }
    }
}
