package org.atoko.call4code.entrado.controller;

import org.atoko.call4code.entrado.model.PersonDetails;
import org.atoko.call4code.entrado.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;

@Controller()
@RequestMapping("/arrivals")
public class ArrivalsViewController {

    @Autowired
    private PersonService personService;


    @RequestMapping(value = {"/"})
    public String index(
            @Value("classpath:/templates/index.html") Resource html
    )
    {
        return "index";
    }

    @PostMapping("/post-signin")
    public Mono<String> postSignin(
            ServerWebExchange webExchange,
            Model model
    ) {

        return webExchange.getFormData().flatMap((fd) -> {
            String pin = fd.getFirst("checkin-pin");
            return personService.get(pin)
                    .map((person) -> {
                        model.addAttribute("fname", person.fname);
                        model.addAttribute("lname", person.lname);
                        model.addAttribute("time", new Date());
                        return "arrivals/post-signin";
                    });
        });

    }

    @RequestMapping(value = {"/register"})
    public String register(
            @Value("classpath:/templates/arrivals/register.html") Resource html
    )
    {
        return "arrivals/register";
    }

    @PostMapping("/post-register")
    public Mono<String> postRegister(
            ServerWebExchange webExchange,
            Model model
    ) {

        return webExchange.getFormData().flatMap((fd) -> {
            String fname = fd.getFirst("first-name");
            String lname = fd.getFirst("last-name");
            String pin = "0000";//fd.getFirst("checkin-pin");
            return personService.create(fname, lname, pin)
                    .map((person) -> {
                        model.addAttribute("fname", fname);
                        model.addAttribute("lname", lname);
                        model.addAttribute("pin", person.id);
                        return "arrivals/post-register";
                    });
        });

    }

    @RequestMapping(value = {"/list"})
    public String list(
            @Value("classpath:/templates/arrivals/list.html") Resource html
    )
    {
        return "arrivals/list";
    }


    @Bean
    public RouterFunction<ServerResponse> assets() {
        return RouterFunctions.resources("/assets/**", new ClassPathResource("assets/"));
    }
}
