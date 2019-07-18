package org.atoko.call4code.entrado.controller.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atoko.call4code.entrado.controller.AuthenticationController;
import org.atoko.call4code.entrado.controller.api.person.PersonController;
import org.atoko.call4code.entrado.model.PersonDetails;
import org.atoko.call4code.entrado.model.request.PersonCreateRequest;
import org.atoko.call4code.entrado.security.model.User;
import org.atoko.call4code.entrado.utils.JwtTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Map;

@Controller
@RequestMapping("/arrivals")
public class ArrivalsViewController {

    static private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private PersonController personController;

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    private JwtTools jwtTools;

    @RequestMapping(value = {"/"})
    public String index(
            @RequestParam(required = false) String error
    ) {
        return "signin/index";
    }

    @PostMapping("/signin")
    public Mono<String> postSignin(
            ServerWebExchange webExchange,
            Model model
    ) {

        return webExchange.getFormData().flatMap((fd) -> {
            String id = fd.getFirst("unique-personId");
            String pin = fd.getFirst("checkin-pin");

            return authenticationController.authenticatePerson(
                    id,
                    pin
            ).map((authResponse) -> {
                    PersonDetails person = ((Map<String, PersonDetails>)authResponse.getBody()).get("data");
                    model.addAttribute("firstName", person.firstName);
                    model.addAttribute("lastName", person.lastName);
                    model.addAttribute("time", new Date());
                    webExchange.getResponse().addCookie(
                            ResponseCookie.from("awt",
                                    jwtTools.generateToken(
                                            User.person(
                                                    person.getId(),
                                                    ""
                                            )
                                    ))
                            .path("/")
                            .build()
                    );

                    return "redirect:/?query=";
            }).onErrorReturn("redirect:/arrivals/?error=NOT_FOUND");
        });

    }

    @RequestMapping(value = {"/register"})
    public String register() {
        return "arrivals/register/index";
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

                        model.addAttribute("username", person.getId());
                        model.addAttribute("pin", request.getPin());

                        return "arrivals/register/post";
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
