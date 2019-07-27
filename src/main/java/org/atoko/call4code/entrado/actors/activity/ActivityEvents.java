package org.atoko.call4code.entrado.actors.activity;

import lombok.Data;

public class ActivityEvents {
    public static class Event {
    }


    @Data
    public static class ActivityCreatedEvent extends Event {
        String deviceId;
        String activityId;
        String name;

        public ActivityCreatedEvent(String deviceId, String activityId, String name) {
            this.deviceId = deviceId;
            this.activityId = activityId;
            this.name = name;
        }

        public ActivityCreatedEvent(ActivityCommands.ActivityCreateCommand command) {
            this.deviceId = command.deviceId;
            this.activityId = command.activityId;
            this.name = command.name;
        }
    }


    @Data
    public static class ActivityJoinedEvent extends Event {
        ActivityCommands.ActivityJoinCommand command;

        public ActivityJoinedEvent(ActivityCommands.ActivityJoinCommand command) {
            this.command = command;
        }
    }

    @Data
    public static class ActivityStationEvent extends Event {
        ActivityCommands.ActivityTargetedCommand command;
        String stationId;

        public ActivityStationEvent(String stationId, ActivityCommands.ActivityTargetedCommand command) {
            this.stationId = stationId;
            this.command = command;
        }
    }

    @Data
    public static class ActivityStationCreatedEvent extends ActivityStationEvent {
        public ActivityStationCreatedEvent(ActivityCommands.ActivityStationCreateCommand command) {
            super(command.id, command);
        }


    }

}
