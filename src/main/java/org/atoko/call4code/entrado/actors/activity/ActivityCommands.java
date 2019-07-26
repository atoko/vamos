package org.atoko.call4code.entrado.actors.activity;

import akka.actor.typed.ActorRef;
import lombok.Data;
import org.atoko.call4code.entrado.model.details.ActivityDetails;

import java.util.function.Supplier;

public class ActivityCommands {

    public static class Command {
    }

    @Data
    public abstract static class ActivityTargetedCommand extends Command {
        String activityId;
        ActorRef<ActivityDetails> replyTo;
    }

    @Data
    public static class ActivityGenesis extends Command {
        ActivityEvents.ActivityCreatedEvent event;

        public ActivityGenesis(ActivityEvents.ActivityCreatedEvent event) {
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
        String personId;

        public ActivityJoinCommand(ActorRef<ActivityDetails> replyTo, String activityId, String personId) {
            this.replyTo = replyTo;
            this.activityId = activityId;
            this.personId = personId;
        }
    }

    @Data
    public static class ActivityDetailsPoll extends ActivityTargetedCommand {
        public ActivityDetailsPoll(ActorRef<ActivityDetails> replyTo, String activityId) {
            this.replyTo = replyTo;
            this.activityId = activityId;
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


}
