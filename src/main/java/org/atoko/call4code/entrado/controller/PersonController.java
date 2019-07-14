package org.atoko.call4code.entrado.controller;

import org.atoko.call4code.entrado.model.PersonDetails;
import org.atoko.call4code.entrado.model.request.PersonCreateRequest;
import org.atoko.call4code.entrado.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController()
@RequestMapping("/api")
public class PersonController {

    @Autowired
    private PersonService personService;

    @PostMapping("/person")
    public Mono<ResponseEntity<PersonDetails>> postPerson(
            @RequestBody PersonCreateRequest request
    ) {
        return personService.create(
                request.getFirstName(),
                request.getLastName(),
                request.getPin()
        ).map((person) -> ResponseEntity.ok(person));
    }
}
