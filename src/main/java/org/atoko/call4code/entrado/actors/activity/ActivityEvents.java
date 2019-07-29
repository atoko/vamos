package org.atoko.call4code.entrado.actors.activity;

import lombok.Data;
import org.atoko.call4code.entrado.model.identifiers.PersonIdentifier;

public class ActivityEvents {

    public static ActivityStationEvent fromCommand(ActivityCommands.ActivityStationTargetedCommand command) {
        ActivityEvents.ActivityStationEvent event = new ActivityStationEvent();
        if (command instanceof ActivityCommands.ActivityStationJoinQueueCommand) {
            event = new ActivityStationQueueJoinedEvent((ActivityCommands.ActivityStationJoinQueueCommand) command);
        } else if (command instanceof ActivityCommands.ActivityStationCreateCommand) {
            event = new ActivityStationCreatedEvent((ActivityCommands.ActivityStationCreateCommand) command);
        } else if (command instanceof ActivityCommands.ActivityStationAssignCommand) {
            event = new ActivityStationQueueAssignedEvent((ActivityCommands.ActivityStationAssignCommand) command);
        }
        return event;
    }

    @Data
    public static class Event {
        String commandGuid;
        String activityId;

        public Event() {
        }

        public Event(String activityId) {
            this.activityId = activityId;
        }
    }


    @Data
    public static class ActivityCreatedEvent extends Event {
        String deviceId;
        String name;

        public ActivityCreatedEvent() {
        }

        public ActivityCreatedEvent(String deviceId, String activityId, String name) {
            super(activityId);
            this.deviceId = deviceId;
            this.name = name;
        }

        public ActivityCreatedEvent(ActivityCommands.ActivityCreateCommand command) {
            super(command.activityId);
            commandGuid = command.commandGuid;
            this.deviceId = command.deviceId;
            this.name = command.name;
        }
    }


    @Data
    public static class ActivityJoinedEvent extends Event {
        PersonIdentifier personId;

        public ActivityJoinedEvent() {
            super();
        }

        public ActivityJoinedEvent(ActivityCommands.ActivityJoinCommand command) {
            super(command.activityId);
            commandGuid = command.commandGuid;
            this.personId = command.personId;
        }
    }

    @Data
    public static class ActivityStationEvent extends Event {
        String stationId;

        public ActivityStationEvent() {
            super();
        }

        public ActivityStationEvent(String stationId, ActivityCommands.ActivityTargetedCommand command) {
            super(command.activityId);
            commandGuid = command.commandGuid;
            this.stationId = stationId;
        }
    }

    @Data
    public static class ActivityStationCreatedEvent extends ActivityStationEvent {
        String name;

        public ActivityStationCreatedEvent() {
            super();
        }

        public ActivityStationCreatedEvent(ActivityCommands.ActivityStationCreateCommand command) {
            super(command.stationId, command);
            commandGuid = command.commandGuid;
            this.name = command.name;
        }
    }

    @Data
    public static class ActivityStationQueueJoinedEvent extends ActivityStationEvent {
        PersonIdentifier personIdentifier;

        public ActivityStationQueueJoinedEvent() {
            super();
        }

        public ActivityStationQueueJoinedEvent(ActivityCommands.ActivityStationJoinQueueCommand command) {
            super(command.stationId, command);
            this.personIdentifier = command.personId;
        }
    }

    @Data
    public static class ActivityStationQueueAssignedEvent extends ActivityStationEvent {
        PersonIdentifier personIdentifier;

        public ActivityStationQueueAssignedEvent() {
            super();
        }

        public ActivityStationQueueAssignedEvent(ActivityCommands.ActivityStationAssignCommand command) {
            super(command.stationId, command);
            this.personIdentifier = command.personId;
        }
    }
}
