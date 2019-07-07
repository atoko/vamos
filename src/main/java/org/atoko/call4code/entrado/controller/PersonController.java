package org.atoko.call4code.entrado.controller;

import org.atoko.call4code.entrado.model.PersonDetails;
import org.atoko.call4code.entrado.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController()
@RequestMapping("/api")
public class PersonController {

    @Autowired
    private PersonService personService;

    @PostMapping("/person")
    public Mono<ResponseEntity<PersonDetails>> postPerson(
            @RequestBody Object name
    ) {
        return personService.create("a", "b", "c").map((person) -> ResponseEntity.ok(person));
    }

    @GetMapping("/person")
    public Mono<ResponseEntity<PersonDetails>> getPerson(
            @RequestParam String id
    ) {
        return personService.get(id).map((person -> {
            return ResponseEntity.ok(person);
        }));
    }

}
