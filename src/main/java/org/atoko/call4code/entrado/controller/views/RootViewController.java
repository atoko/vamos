package org.atoko.call4code.entrado.controller.views;

import org.atoko.call4code.entrado.controller.PersonQueryController;
import org.atoko.call4code.entrado.security.model.User;
import org.atoko.call4code.entrado.service.ActorService;
import org.atoko.call4code.entrado.utils.JwtTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Controller
public class RootViewController {

    @Autowired
    private ActorService actorService;

    @Bean
    public RouterFunction<ServerResponse> assets() {
        return RouterFunctions.resources("/assets/**", new ClassPathResource("assets/"));
    }

    @RequestMapping(value = {"/"})
    public String index(Principal principal) {
        if (principal != null) {
            return "redirect:/menu";
        } else {
            return "redirect:/arrivals/signin";
        }
    }

    @RequestMapping(value = {"/menu"})
    public String menu(Principal principal) {
        if (principal != null) {
            return "menu/index";
        } else {
            return "redirect:/";
        }
    }

    @RequestMapping("/uptime")
    @ResponseBody
    public ResponseEntity<String> count() {
        return ResponseEntity.ok(Long.toString(actorService.uptime()));
    }

}
