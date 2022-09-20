package org.atoko.call4code.entrado.actors.person;

import akka.actor.typed.ActorRef;
import lombok.Data;
import org.atoko.call4code.entrado.model.details.PersonDetails;

import java.util.List;

public class PersonCommands {
    public static class Command {
    }

    @Data
    public static class PersonRegister extends Command {
        PersonEvents.PersonCreatedEvent event;

        public PersonRegister(PersonEvents.PersonCreatedEvent event) {
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
        List<String> ids;

        public PersonQueryPoll(ActorRef<PersonDetails[]> replyTo, List<String> ids) {
            this.replyTo = replyTo;
            this.ids = ids;
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
