package org.atoko.call4code.entrado.controller.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atoko.call4code.entrado.controller.api.activity.ActivityController;
import org.atoko.call4code.entrado.controller.views.store.ActivityStore;
import org.atoko.call4code.entrado.model.details.ActivityDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/www/activity")
public class ActivityViewController {

    static private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ActivityController activityController;

    @RequestMapping(value = {"/"})
    public Mono<String> index(Model model) {
        return Mono.just(null).map((people) -> {
            try {
                model.addAttribute("_activities", objectMapper.writeValueAsString(people));
            } catch (Exception e) {
            }

            return "www/activity/list";
        });
    }

    @RequestMapping(value = {"/details/{activityId}"})
    public Mono<String> index(
            Model model,
            @PathVariable String activityId
    ) {
        return activityController.get(activityId).map((response) -> {
            ActivityDetails activityDetails = (ActivityDetails) response.getBody().get("data");
            model.addAttribute("_activity", activityDetails);

            return "www/activity/details";
        });
    }

    @RequestMapping(value = {"/create"})
    public String create() { return "www/activity/create/index"; }

    @PostMapping("/create/post")
    public Mono<String> postCreate(
            ServerWebExchange webExchange,
            WebSession webSession
    ) {
        return webExchange.getFormData().flatMap((fd) -> {
            return activityController.post(fd.getFirst("name"))
                    .map((response) -> {
                        ActivityDetails activityDetails = response.getBody().get("data");
                        ActivityStore.setCurrentActivity(webSession, activityDetails);
                        return "redirect:/www/activity/details/" + activityDetails.getActivityId();
                    });
        });

    }
//
//    @RequestMapping(value = {"/list"})
//    public Mono<String> list(
//            Model model
//    ) {
//        return personService.get(null).map((people) -> {
//            try {
//                model.addAttribute("people", objectMapper.writeValueAsString(people));
//            } catch (Exception e) {
//            }
//
//            return "arrivals/list/index";
//        });
//    }
}
