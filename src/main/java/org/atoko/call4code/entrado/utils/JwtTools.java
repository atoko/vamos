package org.atoko.call4code.entrado.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.atoko.call4code.entrado.security.model.User;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

@ConfigurationProperties("entrado.jwt")
public class JwtTools {
    String secret;
    String expiration;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        if (expirationAsLong == null) {
            expirationAsLong = Long.parseLong(expiration);
        }
        return expirationAsLong;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    private JwtParser parser() {
        if (jwtParser == null) {
            jwtParser = Jwts.parser().setSigningKey(
                    base64.encodeToString(secret.getBytes())
            );
        }

        return jwtParser;
    }

    public Claims getAllClaimsFromToken(String token) { return parser().parseClaimsJws(token).getBody(); }
    public String getUsernameFromToken(String token) {
        return getAllClaimsFromToken(token).getIssuer();
    }
    public Date getExpirationDateFromToken(String token) { return getAllClaimsFromToken(token).getExpiration(); }
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    public String generateToken(User user) {
        Date created = new Date();
        Date expiration = new Date(created.getTime() + getExpiration() * 1000);

        return Jwts.builder()
                .setClaims(Collections.singletonMap(
                        "role",
                        user.getRoles()
                ))
                .setIssuer(user.getId())
                .setIssuedAt(created)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, base64.encodeToString(secret.getBytes()))
                .compact();
    }


    private static java.util.Base64.Encoder base64 = Base64.getEncoder();
    private static JwtParser jwtParser;
    private static Long expirationAsLong;
}
