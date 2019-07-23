package org.atoko.call4code.entrado.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.atoko.call4code.entrado.controller.api.AuthenticationController.AUTHENTICATION_TOKEN_NAME;

@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

    @Autowired
    AuthenticationManager authenticationManager;


    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.error(new UnsupportedOperationException(""));
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(AUTHENTICATION_TOKEN_NAME);
        if (cookie != null) {
            return this.authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            "JWT",
                            cookie.getValue()
                    ))
                    .map((SecurityContextImpl::new));
        }
        return Mono.empty();
    }
}
