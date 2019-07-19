package org.atoko.call4code.entrado.controller.views.store;

import org.atoko.call4code.entrado.controller.api.person.PersonQueryController;
import org.atoko.call4code.entrado.exception.FrontendException;
import org.atoko.call4code.entrado.model.details.PersonDetails;
import org.atoko.call4code.entrado.service.meta.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Principal;

import static org.atoko.call4code.entrado.controller.api.AuthenticationController.TOKEN_SYMBOLIC_NAME;

@ControllerAdvice
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
            ServerWebExchange serverWebExchange,
            Principal principal
    ) {
        if (principal != null) {
            return personQueryController.getPerson(principal.getName())
                    .doOnError((ex) -> {
                        serverWebExchange
                            .getResponse()
                            .getCookies()
                            .set(
                                TOKEN_SYMBOLIC_NAME,
                                ResponseCookie.from(TOKEN_SYMBOLIC_NAME, "").build()
                            );
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        throw new FrontendException(cause, "logout");
                    })
                    .map((getResponse) -> {
                        return (PersonDetails)getResponse.getBody().get("data");
                    });
        } else {
            return null;
        }
    }
}
