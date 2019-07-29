package org.atoko.call4code.entrado.actors.activity;

import org.atoko.call4code.entrado.model.identifiers.PersonIdentifier;

import java.util.ArrayDeque;
import java.util.Queue;

public class ActivityStationState {
    public String stationId = "";
    public String name = "";
    public PersonIdentifier assignedTo = null;
    public Queue<PersonIdentifier> queue = new ArrayDeque<>();

    public static ActivityStationState empty = new ActivityStationState();

    public ActivityStationState(ActivityStationState state, ActivityEvents.ActivityStationEvent event) {
        this.stationId = state.stationId;
        this.name = state.name;
        this.assignedTo = state.assignedTo;
        this.queue = state.queue;

        if (event instanceof ActivityEvents.ActivityStationCreatedEvent) {
            this.stationId = event.stationId;
            this.name = ((ActivityEvents.ActivityStationCreatedEvent) event).name;
            this.queue = new ArrayDeque<>();
        } else if (event instanceof ActivityEvents.ActivityStationQueueJoinedEvent) {
            //Check if person is in queue already
            if (!state.queue.contains(((ActivityEvents.ActivityStationQueueJoinedEvent) event).personIdentifier)) {
                state.queue.add(((ActivityEvents.ActivityStationQueueJoinedEvent) event).personIdentifier);
            }
            this.queue = new ArrayDeque<>(state.queue);
        }else if (event instanceof ActivityEvents.ActivityStationQueueAssignedEvent) {
            this.assignedTo = ((ActivityEvents.ActivityStationQueueAssignedEvent) event).personIdentifier;
        }
    }

    public ActivityStationState() {

    }
}
