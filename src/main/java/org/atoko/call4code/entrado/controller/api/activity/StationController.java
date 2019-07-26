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
    public Mono<ResponseEntity<Map<String, ActivityStationDetails>>> post(
            @RequestBody String name,
            @RequestParam(value= "filter.activityId") String activityId
    ) {
        return activityService.getById(activityId)
                .flatMap(activityDetails -> activityStationService.create(activityDetails, name)
                .map((station) -> ResponseEntity.ok(Collections.singletonMap("data", station))));
    }

    @GetMapping("/station")
    public Mono<ResponseEntity<Map<String, Object>>> get(
            @RequestParam(value = "filter.stationId") String id
    ) {
        return activityService.get(id).map((activityList -> {
            if (!StringUtils.isEmpty(id)) {
                if (activityList.isEmpty()) {
                    throw new ResponseCodeException(HttpStatus.NOT_FOUND, "ACTIVITY_NOT_FOUND");
                }

                ActivityDetails activity = activityList.get(0);
                if (activity instanceof ActivityDetails.ActivityNullDetails) {
                    throw new ResponseCodeException(HttpStatus.NOT_FOUND, "ACTIVITY_NOT_FOUND");
                }

                return ResponseEntity.ok(Collections.singletonMap("data", activityList.get(0)));
            } else {
                return ResponseEntity.ok(Collections.singletonMap("data", activityList));
            }
        }));
    }
//
//    @PostMapping("/station/assign")
//    public Mono<ResponseEntity<Map<String, ActivityDetails>>> join(
//            @RequestParam(value = "filter.activityId") String activityId,
//            @RequestBody String personId
//    ) {
//        return activityService.join(activityId, personId)
//                .map((activity) -> {
//                    if (activity instanceof ActivityDetails.ActivityNullDetails) {
//                        throw new ResponseCodeException(HttpStatus.NOT_FOUND, "ACTIVITY_NOT_FOUND");
//                    }
//
//                    return ResponseEntity.ok(Collections.singletonMap("data", activity));
//                });
//    }
//
//    @PostMapping("/station/join")
//    public Mono<ResponseEntity<Map<String, ActivityDetails>>> join(
//            @RequestParam(value = "filter.activityId") String activityId,
//            @RequestBody String personId
//    ) {
//        return activityService.join(activityId, personId)
//                .map((activity) -> {
//                    if (activity instanceof ActivityDetails.ActivityNullDetails) {
//                        throw new ResponseCodeException(HttpStatus.NOT_FOUND, "ACTIVITY_NOT_FOUND");
//                    }
//
//                    return ResponseEntity.ok(Collections.singletonMap("data", activity));
//                });
//    }
}
