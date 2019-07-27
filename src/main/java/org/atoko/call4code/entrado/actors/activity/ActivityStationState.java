package org.atoko.call4code.entrado.actors.activity;

import java.util.ArrayDeque;
import java.util.Queue;

public class ActivityStationState {
    public String id = "";
    public String name = "";
    public String assignedTo = "";
    public Queue<String> queue = new ArrayDeque<>();


    public ActivityStationState(ActivityStationState state, ActivityEvents.ActivityStationEvent event) {
        this.id = state.id;
        this.name = state.name;
    }

    public ActivityStationState(ActivityCommands.ActivityStationCreateCommand command) {
        this.id = command.id;
        this.name = command.name;
    }
}
