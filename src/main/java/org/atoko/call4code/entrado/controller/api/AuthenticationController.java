package org.atoko.call4code.entrado.controller.api;

import org.atoko.call4code.entrado.exception.ResponseCodeException;
import org.atoko.call4code.entrado.model.details.PersonDetails;
import org.atoko.call4code.entrado.security.model.User;
import org.atoko.call4code.entrado.service.PersonService;
import org.atoko.call4code.entrado.utils.JwtTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@RestController()
@RequestMapping("/authentication")
public class AuthenticationController {
    public static final String TOKEN_SYMBOLIC_NAME = "awt";

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
                if (!person.pin.equals(pin)) {
                    throw new RuntimeException();
                }

                String jwt = jwtTools.generateToken(
                        User.person(id, "")
                );

                ResponseEntity<Map<String, PersonDetails>> response =
                        ResponseEntity.ok()
                        .header(TOKEN_SYMBOLIC_NAME, jwt)
                        .body(Collections.singletonMap("data", person));
                return response;
            }));
        } catch (Exception e) {
            throw new ResponseCodeException(HttpStatus.FORBIDDEN, "LOGIN_FAILED", "Login failed");
        }
    }
}
