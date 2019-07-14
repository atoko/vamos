package org.atoko.call4code.entrado.controller;

import org.atoko.call4code.entrado.model.PersonDetails;
import org.atoko.call4code.entrado.model.request.PersonCreateRequest;
import org.atoko.call4code.entrado.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;

@RestController()
@RequestMapping("/api")
public class PersonQueryController {

    @Autowired
    private PersonService personService;

    @GetMapping("/person")
    public Mono<ResponseEntity<Object>> getPerson(
            @RequestParam(value = "filter.personId", required = false) String id
    ) {
        return personService.get(id).map((personList -> {
            if (!StringUtils.isEmpty(id)) {
                return ResponseEntity.ok(Collections.singletonMap("data", personList.get(0)));
            } else {
                return ResponseEntity.ok(Collections.singletonMap("data", personList));
            }
        }));
    }
}
