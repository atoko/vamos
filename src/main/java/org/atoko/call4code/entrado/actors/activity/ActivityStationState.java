package org.atoko.call4code.entrado.actors.activity;

public class ActivityStationState {
    public String id;
    public String name;

    public ActivityStationState(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public ActivityStationState(ActivityCommands.ActivityStationCreateCommand command) {
        this.id = command.id;
        this.name = command.name;
    }
}
