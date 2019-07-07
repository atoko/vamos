package org.atoko.call4code.entrado.controller;

import akka.actor.ActorSystem;
import org.atoko.call4code.entrado.service.ActorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.Request;

@Controller
public class SystemController {

    @Autowired
    private ActorService actorService;

    @RequestMapping(value = {"/"})
    public String index(
            @Value("classpath:/templates/index.html") Resource html
    )
    {
        return "index";
    }

    @RequestMapping("/uptime")
    @ResponseBody
    public ResponseEntity<String> count() {
        return ResponseEntity.ok(Long.toString(actorService.uptime()));
    }

}
