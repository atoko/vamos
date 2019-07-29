package org.atoko.call4code.entrado.controller.views.store;

import org.atoko.call4code.entrado.controller.api.person.PersonQueryController;
import org.atoko.call4code.entrado.exception.FrontendException;
import org.atoko.call4code.entrado.model.details.PersonDetails;
import org.atoko.call4code.entrado.service.meta.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Priority;
import java.security.Principal;
import java.util.List;

@ControllerAdvice
@Priority(100)
public class AuthenticationStore {

    public static final String AUTHENTICATION_CURRENT_DEVICE_ID = "__authentication_current_deviceId";
    public static final String AUTHENTICATION_CURRENT = "__authentication_current";

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private PersonQueryController personQueryController;

    @ModelAttribute(AUTHENTICATION_CURRENT_DEVICE_ID)
    public String deviceId() {
        return deviceService.getDeviceId();
    }

    @ModelAttribute(AUTHENTICATION_CURRENT)
    public Mono<PersonDetails> currentLoggedIn(
            Principal principal,
            ServerWebExchange serverWebExchange
    ) {
        Boolean isLoggingOut = serverWebExchange.getRequest().getPath().value().contains("/www/logout");

        if (!isLoggingOut && principal != null) {
            return personQueryController.getPerson(List.of(principal.getName()))
                    .doOnError((ex) -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        throw new FrontendException(cause, "/www/logout");
                    })
                    .map((getResponse) -> {
                        return getResponse.getBody().get("data").get(0);
                    });
        } else {
            return null;
        }
    }
}
