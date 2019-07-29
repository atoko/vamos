package org.atoko.call4code.entrado.controller.api.person;

import org.atoko.call4code.entrado.exception.ResponseCodeException;
import org.atoko.call4code.entrado.model.details.PersonDetails;
import org.atoko.call4code.entrado.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api")
public class PersonQueryController {

    @Autowired
    private PersonService personService;

    @GetMapping("/person")
    public Mono<ResponseEntity<Map<String, List<PersonDetails>>>> getPerson(
            @RequestParam(value = "filter.personId", required = false) List<String> ids
    ) {
        if (ids == null) {
            ids = Collections.emptyList();
        }
        List<String> finalIds = ids;
        return personService.get(ids).map((personList -> {
            if (personList.isEmpty()) {
                throw new ResponseCodeException(HttpStatus.NOT_FOUND, "PERSON_NOT_FOUND");
            }

            PersonDetails person = personList.get(0);
            if (person instanceof PersonDetails.PersonNullDetails) {
                throw new ResponseCodeException(HttpStatus.NOT_FOUND, "PERSON_NOT_FOUND");
            }

            return ResponseEntity.ok(Collections.singletonMap("data", personList));
        }));
    }
}
