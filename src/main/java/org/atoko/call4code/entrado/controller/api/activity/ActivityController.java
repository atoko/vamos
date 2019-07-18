package org.atoko.call4code.entrado.controller.api.activity;

import org.atoko.call4code.entrado.model.PersonDetails;
import org.atoko.call4code.entrado.model.request.PersonCreateRequest;
import org.atoko.call4code.entrado.service.ActivityService;
import org.atoko.call4code.entrado.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@RestController()
@RequestMapping("/api")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @PostMapping("/activity")
    public Mono<ResponseEntity<Map<String, String>>> postPerson(
            @RequestBody String name
    ) {
        return activityService.create(
                name
        ).map((activity) -> ResponseEntity.ok(Collections.singletonMap("data", activity)));
    }
}
