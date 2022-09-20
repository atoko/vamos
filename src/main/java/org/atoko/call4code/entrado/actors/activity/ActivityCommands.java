package org.atoko.call4code.entrado.actors.activity;

import akka.actor.typed.ActorRef;
import lombok.Data;
import org.atoko.call4code.entrado.model.details.ActivityDetails;
import org.atoko.call4code.entrado.model.identifiers.PersonIdentifier;

import java.util.UUID;
import java.util.function.Supplier;

public class ActivityCommands {

    public static Command fromEvent(ActivityEvents.Event event) {
        Command command = new Command();
        if (event instanceof ActivityEvents.ActivityJoinedEvent) {
            command = new ActivityJoinCommand(
                    null,
                    event.activityId,
                    ((ActivityEvents.ActivityJoinedEvent) event).personId
            );
        }

        if (event instanceof ActivityEvents.ActivityStationCreatedEvent) {
            command = new ActivityStationCreateCommand(
                    null,
                    event.activityId,
                    ((ActivityEvents.ActivityStationCreatedEvent) event).stationId,
                    ((ActivityEvents.ActivityStationCreatedEvent) event).name
            );
        }

        command.commandGuid = event.commandGuid;
        return command;
    }

    @Data
    public static class Command {
        String commandGuid = UUID.randomUUID().toString();
    }

    @Data
    public abstract static class ActivityTargetedCommand extends Command {
        String activityId;
        ActorRef<ActivityDetails> replyTo;

        public ActivityTargetedCommand(ActorRef<ActivityDetails> replyTo, String activityId) {
            this.activityId = activityId;
            this.replyTo = replyTo;
        }
    }

    @Data
    public static class Activity extends Command {
        ActivityEvents.ActivityCreatedEvent event;

        public Activity(ActivityEvents.ActivityCreatedEvent event) {
            this.event = event;
        }
    }


    @Data
    public static class ActivityCreateCommand extends Command {
        ActorRef replyTo;
        String deviceId;
        String activityId;
        String name;

        public ActivityCreateCommand(ActorRef replyTo, String deviceId, String activityId, String name) {
            this.replyTo = replyTo;
            this.deviceId = deviceId;
            this.activityId = activityId;
            this.name = name;
        }
    }

    @Data
    public static class ActivityJoinCommand extends ActivityTargetedCommand {
        PersonIdentifier personId;

        public ActivityJoinCommand(ActorRef<ActivityDetails> replyTo, String activityId, PersonIdentifier personId) {
            super(replyTo, activityId);
            this.personId = personId;
        }
    }

    @Data
    public static class ActivityDetailsPoll extends ActivityTargetedCommand {
        public ActivityDetailsPoll(ActorRef<ActivityDetails> replyTo, String activityId) {
            super(replyTo, activityId);
        }
    }


    @Data
    public static class ActivityQueryPoll extends Command {
        ActorRef<ActivityDetails[]> replyTo;

        public ActivityQueryPoll(ActorRef<ActivityDetails[]> replyTo) {
            this.replyTo = replyTo;
        }

        public ActivityQueryPoll(Supplier<ActorRef<ActivityDetails[]>> f) {
            this.replyTo = f.get();
        }
    }

    @Data
    public static class ActivityStationTargetedCommand extends ActivityTargetedCommand{
        public String stationId;

        public ActivityStationTargetedCommand(ActorRef replyTo, String activityId, String id) {
            super(replyTo, activityId);
            this.stationId = id;
        }
    }


    @Data
    public static class ActivityStationCreateCommand extends ActivityStationTargetedCommand{
        public String name;

        public ActivityStationCreateCommand(ActorRef replyTo, String activityId, String id, String name) {
           super(replyTo, activityId, id);
            this.name = name;
        }
    }

    @Data
    public static class ActivityStationAssignCommand extends ActivityStationTargetedCommand{
        public PersonIdentifier personId;

        public ActivityStationAssignCommand(ActorRef replyTo, String activityId, String stationId, PersonIdentifier personId) {
            super(replyTo, activityId, stationId);
            this.personId = personId;
        }
    }

    @Data
    public static class ActivityStationJoinQueueCommand extends ActivityStationTargetedCommand{
        public PersonIdentifier personId;

        public ActivityStationJoinQueueCommand(ActorRef replyTo, String activityId, String stationId, PersonIdentifier personId) {
            super(replyTo, activityId, stationId);
            this.personId = personId;
        }
    }

    @Data
    public static class ActivityStationNextQueueCommand extends ActivityStationTargetedCommand{
        public PersonIdentifier personId;

        public ActivityStationNextQueueCommand(ActorRef replyTo, String activityId, String stationId, PersonIdentifier personId) {
            super(replyTo, activityId, stationId);
            this.personId = personId;
        }
    }


}
