package org.atoko.call4code.entrado.controller.views;

import org.atoko.call4code.entrado.controller.api.AuthenticationController;
import org.atoko.call4code.entrado.model.details.PersonDetails;
import org.atoko.call4code.entrado.service.meta.ActorSystemService;
import org.atoko.call4code.entrado.utils.JwtTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.Date;
import java.util.Map;

import static org.atoko.call4code.entrado.controller.api.AuthenticationController.TOKEN_SYMBOLIC_NAME;

@Controller
public class RootViewController {

    @Autowired
    private ActorSystemService actorSystemService;

    @Autowired
    private AuthenticationController authenticationController;

    @Bean
    public RouterFunction<ServerResponse> assets() {
        return RouterFunctions.resources("/assets/**", new ClassPathResource("assets/"));
    }

    @RequestMapping(value = {"/", "/www"})
    public String index(ServerWebExchange serverWebExchange, Principal principal) {
        if (principal != null) {
            return "redirect:/www/menu";
        } else {
            return "redirect:/www/signin";
        }
    }

    @Autowired
    private JwtTools jwtTools;

    @RequestMapping(value = {"/www/signin"})
    public String index(
            @RequestParam(required = false) String error
    ) {
        return "www/signin/index";
    }

    @PostMapping("/www/signin")
    public Mono<String> postSignin(
            ServerWebExchange webExchange
    ) {

        return webExchange.getFormData().flatMap((fd) -> {
            String id = fd.getFirst("unique-personId");
            String pin = fd.getFirst("checkin-pin");

            return authenticationController.authenticatePerson(
                    id,
                    pin
            ).map((authResponse) -> {
                webExchange.getResponse().addCookie(
                        ResponseCookie.from(
                                TOKEN_SYMBOLIC_NAME,
                                authResponse.getHeaders().getFirst(TOKEN_SYMBOLIC_NAME)
                        )
                        .path("/")
                        .build()
                );

                return "redirect:/www?query=";
            }).onErrorReturn("redirect:/www/signin/?error=LOGIN_FAILED");
        });
    }

    @RequestMapping(value = {"/www/logout"})
    public Mono<String> logout(
            ServerWebExchange serverWebExchange,
            WebSession webSession
    ) {
        serverWebExchange
                .getResponse()
                .getCookies()
                .remove(TOKEN_SYMBOLIC_NAME);

        return webSession
            .invalidate()
            .map((v) -> "redirect:/www?query=");
    }
    @RequestMapping(value = {"/www/menu"})
    public String menu(Principal principal) {
        if (principal != null) {
            return "www/menu/index";
        } else {
            return "redirect:/www";
        }
    }

    @RequestMapping("/.well-known/uptime")
    @ResponseBody
    public ResponseEntity<String> count() {
        return ResponseEntity.ok(Long.toString(actorSystemService.uptime()));
    }

}
