package org.atoko.call4code.entrado.controller.api;

import org.atoko.call4code.entrado.exception.ResponseCodeException;
import org.atoko.call4code.entrado.model.details.PersonDetails;
import org.atoko.call4code.entrado.security.model.User;
import org.atoko.call4code.entrado.service.PersonService;
import org.atoko.call4code.entrado.utils.JwtTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@RestController()
@RequestMapping("/authentication")
public class AuthenticationController {
    public static final String AUTHENTICATION_TOKEN_NAME = "awt";
    private static Logger logger = LoggerFactory.getLogger(AuthenticationController.class.getName());
    @Autowired
    private PersonService personService;
    @Autowired
    private JwtTools jwtTools;

    @PostMapping("/signin")
    public Mono<ResponseEntity<Map<String, PersonDetails>>> authenticatePerson(
            @RequestParam(value = "filter.personId") String id,
            @RequestBody String pin
    ) {
        try {
            return personService.getById(id).map((person -> {
                if (person instanceof PersonDetails.PersonNullDetails) {
                    throw new RuntimeException();
                }
                if (!person.pin.equals(pin)) {
                    throw new RuntimeException();
                }

                String jwt = jwtTools.generateToken(
                        User.person(id, "")
                );

                ResponseEntity<Map<String, PersonDetails>> response =
                        ResponseEntity.ok()
                                .header(AUTHENTICATION_TOKEN_NAME, jwt)
                                .body(Collections.singletonMap("data", person));
                return response;
            }));
        } catch (Exception e) {
            logger.error("LOGIN_FAILED", e);
            throw new ResponseCodeException(HttpStatus.FORBIDDEN, "LOGIN_FAILED", "Login failed");
        }
    }
}
