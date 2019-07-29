package org.atoko.call4code.entrado.controller.api.activity;

import org.atoko.call4code.entrado.exception.ResponseCodeException;
import org.atoko.call4code.entrado.model.details.ActivityDetails;
import org.atoko.call4code.entrado.model.details.ActivityStationDetails;
import org.atoko.call4code.entrado.service.ActivityService;
import org.atoko.call4code.entrado.service.ActivityStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@RestController()
@RequestMapping("/api/activity")
public class StationController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityStationService activityStationService;


    @PostMapping("/station")
    public Mono<ResponseEntity<Map<String, ActivityDetails>>> post(
            @RequestParam(value= "filter.activityId") String activityId,
            @RequestBody String name
    ) {
        return activityService.getById(activityId)
                .flatMap(activityDetails -> activityStationService.create(activityDetails, name)
                .map((station) -> ResponseEntity.ok(Collections.singletonMap("data", station))));
    }

//
    @PostMapping("{activityId}/station/assign/")
    public Mono<ResponseEntity<Map<String, ActivityDetails>>> assign(
            @PathVariable String activityId,
            @PathVariable String stationId,
            @RequestBody String personId
    ) {

        return activityService.getById(activityId)
            .flatMap(activityDetails -> activityStationService.assign(activityId, stationId, personId)
            .map((station) -> ResponseEntity.ok(Collections.singletonMap("data", station))));
    }
//
    @PostMapping("/{activityId}/station/join/{stationId}")
    public Mono<ResponseEntity<Map<String, ActivityDetails>>> join(
            @PathVariable String activityId,
            @PathVariable String stationId,
            @RequestBody String personId
    ) {
        return activityService.getById(activityId)
                .flatMap(activityDetails -> activityStationService.join(activityId, stationId, personId)
                .map((station) -> ResponseEntity.ok(Collections.singletonMap("data", station))));
    }
}
