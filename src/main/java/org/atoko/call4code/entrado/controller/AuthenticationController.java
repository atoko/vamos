package org.atoko.call4code.entrado.controller;

import org.atoko.call4code.entrado.exception.ResponseCodeException;
import org.atoko.call4code.entrado.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;

@RestController()
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private PersonService personService;

    @PostMapping("/person")
    public Mono<ResponseEntity<Object>> authenticatePerson(
            @RequestParam(value = "filter.personId") String id,
            @RequestBody String pin
    ) {
        try {
            return personService.getById(id).map((person -> {
                if (!person.pin.equals(pin)) {
                    throw new RuntimeException();
                }

                return ResponseEntity.ok(Collections.singletonMap("data", person));
            }));
        } catch (Exception e) {
            throw new ResponseCodeException(HttpStatus.FORBIDDEN, "LOGIN_FAILED", "Login failed");
        }
    }
}
