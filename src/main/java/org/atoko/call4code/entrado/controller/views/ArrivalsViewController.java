package org.atoko.call4code.entrado.controller.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.var;
import org.atoko.call4code.entrado.model.PersonDetails;
import org.atoko.call4code.entrado.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;

@Controller
@RequestMapping("/arrivals")
public class ArrivalsViewController {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonService sessionService;



    @RequestMapping(value = {"/", "/signin"})
    public String index()
    {
        return "signin/index";
    }

    @PostMapping("/signin/post")
    public Mono<String> postSignin(
            ServerWebExchange webExchange,
            Model model
    ) {

        return webExchange.getFormData().flatMap((fd) -> {
            String pin = fd.getFirst("checkin-pin");
            return personService.get(pin)
                    .map((personDetailsList) -> {
                        PersonDetails person = personDetailsList.get(0);
                        model.addAttribute("fname", person.fname);
                        model.addAttribute("lname", person.lname);
                        model.addAttribute("time", new Date());
                        return "signin/index";
                    }).onErrorReturn("signin");
        });

    }

    @RequestMapping(value = {"/register"})
    public String register()
    {
        return "arrivals/register/index";
    }

    @PostMapping("/register/post")
    public Mono<String> postRegister(
            ServerWebExchange webExchange,
            Model model
    ) {

        return webExchange.getFormData().flatMap((fd) -> {
            String fname = fd.getFirst("first-name");
            String lname = fd.getFirst("last-name");
            String pin = fd.getFirst("checkin-pin");
            return personService.create(fname, lname, pin)
                    .map((person) -> {
                        model.addAttribute("fname", fname);
                        model.addAttribute("lname", lname);
                        model.addAttribute("pin", pin);
                        return "arrivals/register/post";
                    });
        });

    }

    static private ObjectMapper objectMapper = new ObjectMapper();
    @RequestMapping(value = {"/list"})
    public Mono<String> list(
            Model model
    )
    {
        return personService.get(null).map((people) -> {
            try {
                model.addAttribute("people", objectMapper.writeValueAsString(people));
            } catch (Exception e) {}

            return "arrivals/list/index";
        });
    }
}
