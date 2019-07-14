package org.atoko.call4code.entrado.actors;

import akka.actor.Props;
import akka.actor.UntypedActor;
import org.atoko.call4code.entrado.model.PersonDetails;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class QueueActor extends UntypedActor {
    public static String PERSON_PREFIX = "person-";
    private final String id;
    private final String fname;
    private final String lname;
    private final String pin;

    public QueueActor(String fname, String lname, String pin, String id) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.pin = pin;
    }

    public static Props props(String fname, String lname, String pin, String id) {
        // You need to specify the actual type of the returned actor
        // since Java 8 lambdas have some runtime type information erased
        return Props.create(QueueActor.class, () -> new QueueActor(fname, lname, pin, id));
    }

    // constructor

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof TellDetails) {
            getSender().tell(new PersonDetails(fname, lname, id), getSelf());
        } else if (message instanceof JoinQueue) {
            //not implemented
        } else {
            unhandled(message);
        }
    }

    public static class TellDetails {
    }

    public static class JoinQueue {
    }
}