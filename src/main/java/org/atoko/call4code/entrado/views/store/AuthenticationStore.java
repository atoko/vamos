package org.atoko.call4code.entrado.views.store;

import org.atoko.call4code.entrado.model.PersonDetails;
import org.atoko.call4code.entrado.service.meta.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class AuthenticationStore {

    @Autowired
    private DeviceService deviceService;

    @ModelAttribute("__authentication_current_deviceId")
    public String deviceId() {
        return deviceService.getDeviceId();
    }

    @ModelAttribute("__authentication_current")
    public PersonDetails currentLoggedIn(Principal principal) {
        if (principal != null) {
            return new PersonDetails(
                    principal.getName(),
                    deviceService.getDeviceId(),
                    "",
                    "",
                    "1234"
            );
        } else {
            return null;
        }
    }
}
