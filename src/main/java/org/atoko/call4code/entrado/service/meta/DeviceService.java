package org.atoko.call4code.entrado.service.meta;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import org.atoko.call4code.entrado.actors.meta.DeviceSupervisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;

import static org.atoko.call4code.entrado.actors.PersonActor.PERSON_PREFIX;

@Component
public class DeviceService {
    private ActorRef actorRef;

    private SystemInfo systemInfo = new SystemInfo();
    private String serial = systemInfo.getHardware().getComputerSystem().getBaseboard().getSerialNumber().replace(" ", "_");

    @Autowired
    private ActorSystem actorSystem;

    public DeviceService(@Autowired ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
        actorRef = actorSystem.actorOf(DeviceSupervisor.props(), "device;" + serial);
    }

    public String getDeviceId() { return serial; }

    public ActorRef get() {
        return actorRef;
    }

    public ActorSelection child(String id) {
        return actorSystem.actorSelection(actorRef.path().child(id));
    }

    public ActorSelection allPersons() {
        return actorSystem.actorSelection(actorRef.path().$div(PERSON_PREFIX + "*"));
    }
}
