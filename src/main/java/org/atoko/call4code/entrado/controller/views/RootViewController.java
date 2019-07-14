package org.atoko.call4code.entrado.controller.views;

import org.atoko.call4code.entrado.service.ActorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Controller
public class RootViewController {

    @Autowired
    private ActorService actorService;

    @Bean
    public RouterFunction<ServerResponse> assets() {
        return RouterFunctions.resources("/assets/**", new ClassPathResource("assets/"));
    }

    @RequestMapping(value = {"/"})
    public String index() {
        //Check JWT
        return "signin/index";
    }

    @RequestMapping(value = {"/menu"})
    public String menu() {
        return "menu/index";
    }

    @RequestMapping("/uptime")
    @ResponseBody
    public ResponseEntity<String> count() {
        return ResponseEntity.ok(Long.toString(actorService.uptime()));
    }

}
