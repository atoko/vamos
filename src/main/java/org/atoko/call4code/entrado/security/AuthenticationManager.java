package org.atoko.call4code.entrado.security;

import org.atoko.call4code.entrado.utils.JwtTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {
    @Autowired
    JwtTools jwtTools;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();

        String username;
        try {
            username = jwtTools.getUsernameFromToken(token);
        } catch (Exception e) {
            username = null;
        }

        if (username != null && jwtTools.isTokenValid(token)) {
            List roles = jwtTools.getAllClaimsFromToken(token)
                    .get("role", List.class);

            if (roles != null) {
                roles = (List) roles
                        .stream()
                        .map((role) -> new SimpleGrantedAuthority(
                                (String) role
                        )
                        ).collect(Collectors.toList());
            } else {
                roles = List.of();
            }

            return Mono.just(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            roles
                    )
            );
        } else {
            return Mono.empty();
        }
    }
}
