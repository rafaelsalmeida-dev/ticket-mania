package com.github.rafael_souza_de_almeida.ticket_mania.core.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.public-key}")
    private RSAPublicKey publicKey;

    @Value("${api.security.token.private-key}")
    private RSAPrivateKey privateKey;

    public String generateToken(User user) {
        Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);

        return JWT.create()
                .withIssuer("ticket-mania-api")
                .withSubject(user.getEmail())
                .withClaim("role", user.getRole().name())
                .withExpiresAt(genExpirationDate())
                .sign(algorithm);

    }

    public String validateToken(String token) {

        try {

            Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);

            return JWT.require(algorithm)
                    .withIssuer("ticket-mania-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch(JWTVerificationException e) {
            return "";
        }
    }

    private Instant genExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

}
