package org.atoko.call4code.entrado.actors.person;

import akka.actor.typed.ActorRef;
import lombok.Data;
import org.atoko.call4code.entrado.model.details.PersonDetails;

public class PersonCommands {
    public static class Command {
    }

    @Data
    public static class PersonGenesis extends Command {
        PersonEvents.PersonCreatedEvent event;

        public PersonGenesis(PersonEvents.PersonCreatedEvent event) {
            this.event = event;
        }
    }

    @Data
    public static class PersonCreateCommand extends Command {
        public ActorRef replyTo;
        String deviceId;
        String personId;
        String firstName;
        String lastName;
        String pin;

        public PersonCreateCommand(
                ActorRef replyTo,
                String deviceId,
                String personId,
                String firstName,
                String lastName,
                String pin
        ) {
            this.replyTo = replyTo;
            this.deviceId = deviceId;
            this.personId = personId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.pin = pin;
        }
    }

    @Data
    public static class PersonQueryPoll extends Command {
        ActorRef<PersonDetails[]> replyTo;

        public PersonQueryPoll(ActorRef<PersonDetails[]> replyTo) {
            this.replyTo = replyTo;
        }
    }

    @Data
    public static class PersonDetailsPoll extends Command {
        ActorRef<PersonDetails> replyTo;
        String personId;

        public <U> PersonDetailsPoll(ActorRef<PersonDetails> replyTo, String personId) {
            this.replyTo = replyTo;
            this.personId = personId;
        }
    }
}
