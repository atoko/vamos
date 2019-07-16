package org.atoko.call4code.entrado.actors;

import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import org.atoko.call4code.entrado.model.PersonDetails;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PersonActor extends UntypedAbstractActor {
    public static String PERSON_PREFIX = "person;";
    public final String id;
    public final String firstName;
    public final String lastName;
    public final String pin;

    public PersonActor(String firstName, String lastName, String pin, String id) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.pin = pin;
    }

    public static Props props(String fname, String lname, String pin, String id) {
        // You need to specify the actual type of the returned actor
        // since Java 8 lambdas have some runtime type information erased
        return Props.create(PersonActor.class, () -> new PersonActor(fname, lname, pin, id));
    }

    // constructor

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof TellDetails) {
            PersonDetails response = new PersonDetails(
                    this,
                    ((TellDetails)message).getDeviceId()
            );
            getSender().tell(response, getSelf());
        } else if (message instanceof JoinQueue) {
            //not implemented
        } else {
            unhandled(message);
        }
    }

    public static class TellDetails {
        String deviceId;

        public TellDetails(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceId() {
            return deviceId;
        }
    }

    public static class JoinQueue {
    }
}