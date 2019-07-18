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
    public final String deviceId;
    public final String personId;
    public final String firstName;
    public final String lastName;
    public final String pin;

    public PersonActor(String deviceId, String personId, String firstName, String lastName, String pin) {
        this.deviceId = deviceId;
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.pin = pin;
    }

    public static Props props(String deviceId, String personId, String firstName, String lastName, String pin) {
        // You need to specify the actual type of the returned actor
        // since Java 8 lambdas have some runtime type information erased
        return Props.create(PersonActor.class, () -> new PersonActor(personId, deviceId, firstName, lastName, pin));
    }

    // constructor

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof PersonDetailsPoll) {
            PersonDetails response = new PersonDetails(
                    this
            );
            getSender().tell(response, getSelf());
        } else {
            unhandled(message);
        }
    }

    public static class PersonDetailsPoll {
    }
}