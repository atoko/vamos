package org.atoko.call4code.entrado.controller.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/activity")
public class ActivityViewController {

    static private ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(value = {"/"})
    public Mono<String> index(Model model) {
        return Mono.just(null).map((people) -> {
            try {
                model.addAttribute("activities", objectMapper.writeValueAsString(people));
            } catch (Exception e) {
            }

            return "activity/list";
        });
    }

    @RequestMapping(value = {"/onboard"})
    public String onboard() { return "activity/onboard"; }

    @RequestMapping(value = {"/create"})
    public String create() { return "activity/create"; }
//
//    @PostMapping("/register/post")
//    public Mono<String> postRegister(
//            ServerWebExchange webExchange,
//            Model model
//    ) {
//
//        return webExchange.getFormData().flatMap((fd) -> {
//            PersonCreateRequest request = new PersonCreateRequest(fd);
//            return personController.postPerson(request)
//                    .map((response) -> {
//                        PersonDetails person = response.getBody().get("data");
//                        model.addAttribute("firstName", person.firstName);
//                        model.addAttribute("lastName", person.lastName);
//
//                        model.addAttribute("username", person.getId());
//                        model.addAttribute("pin", request.getPin());
//
//                        return "arrivals/register/post";
//                    });
//        });
//
//    }
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
