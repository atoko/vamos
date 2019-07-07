package org.atoko.call4code.entrado.actors;


import akka.actor.Props;
import akka.actor.UntypedActor;
import org.atoko.call4code.entrado.model.PersonDetails;
import org.atoko.call4code.entrado.service.PersonService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static org.atoko.call4code.entrado.actors.PersonActor.PERSON_PREFIX;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SessionActor extends UntypedActor {
    private final PersonService personService;

    public static Props props(PersonService personService) {
        // You need to specify the actual type of the returned actor
        // since Java 8 lambdas have some runtime type information erased
        return Props.create(SessionActor.class, () -> new SessionActor(personService));
    }

    public SessionActor(PersonService personService) {
        this.personService = personService;
    }

    // constructor

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof AddPerson) {
            AddPerson ap = (AddPerson)message;
            getContext().actorOf(PersonActor.props(ap.fname, ap.lname, ap.pin, ap.id), PERSON_PREFIX + ((AddPerson) message).id);
            getSender().tell(true, getSelf());
        } else if (message instanceof TellPersonList) {
            ArrayList<PersonDetails> list = new ArrayList<PersonDetails>();
            getContext().getChildren().forEach((c) -> {

            });
            getSender().tell(list, getSelf());

        }else {
            unhandled(message);
        }
    }

    public static class AddPerson {
        public String id;
        public String pin;
        public String fname;
        public String lname;

        public AddPerson(String fname, String lname, String pin, String id) {
            this.fname = fname;
            this.lname = lname;
            this.pin = pin;
            this.id = id;
        }
    }

    public static class TellPersonList {}
}