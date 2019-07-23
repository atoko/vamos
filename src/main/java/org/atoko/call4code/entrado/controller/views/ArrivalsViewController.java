package org.atoko.call4code.entrado.controller.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atoko.call4code.entrado.controller.api.person.PersonController;
import org.atoko.call4code.entrado.controller.api.person.PersonQueryController;
import org.atoko.call4code.entrado.model.details.PersonDetails;
import org.atoko.call4code.entrado.model.request.PersonCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/www/arrivals")
public class ArrivalsViewController {

    static private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private PersonController personController;
    @Autowired
    private PersonQueryController personQueryController;

    @RequestMapping(value = {"/register"})
    public String register() {
        return "www/arrivals/register/index";
    }

    @PostMapping("/register/post")
    public Mono<String> postRegister(
            ServerWebExchange webExchange,
            Model model
    ) {

        return webExchange.getFormData().flatMap((fd) -> {
            PersonCreateRequest request = new PersonCreateRequest(fd);
            return personController.postPerson(request)
                    .map((response) -> {
                        PersonDetails person = response.getBody().get("data");
                        model.addAttribute("firstName", person.firstName);
                        model.addAttribute("lastName", person.lastName);

                        model.addAttribute("username", person.getPersonId());
                        model.addAttribute("pin", request.getPin());

                        return "www/arrivals/register/post";
                    });
        });

    }

    @RequestMapping(value = {"", "/"})
    public Mono<String> list(
            Model model
    ) {
        return personQueryController.getPerson(null).map((people) -> {
            try {
                model.addAttribute("_arrivals_people", objectMapper.writeValueAsString(people));
            } catch (Exception e) {
            }

            return "arrivals/list/index";
        });
    }
}
