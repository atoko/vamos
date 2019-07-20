package org.atoko.call4code.entrado.controller.views.store;

import org.atoko.call4code.entrado.controller.api.person.PersonQueryController;
import org.atoko.call4code.entrado.exception.FrontendException;
import org.atoko.call4code.entrado.model.details.PersonDetails;
import org.atoko.call4code.entrado.service.meta.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import reactor.core.publisher.Mono;

import javax.annotation.Priority;
import java.security.Principal;

@ControllerAdvice
@Priority(100)
public class AuthenticationStore {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private PersonQueryController personQueryController;

    @ModelAttribute("__authentication_current_deviceId")
    public String deviceId() {
        return deviceService.getDeviceId();
    }

    @ModelAttribute("__authentication_current")
    public Mono<PersonDetails> currentLoggedIn(
            Principal principal
    ) {
        if (principal != null) {
            return personQueryController.getPerson(principal.getName())
                    .doOnError((ex) -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        throw new FrontendException(cause, "/www/logout");
                    })
                    .map((getResponse) -> {
                        return (PersonDetails)getResponse.getBody().get("data");
                    });
        } else {
            return null;
        }
    }
}
