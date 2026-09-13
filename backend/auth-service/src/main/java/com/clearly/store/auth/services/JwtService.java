package com.clearly.store.auth.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.clearly.store.auth.repositories.AuthRepository.UserAccount;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long expiryHours;

    public JwtService(@Value("${auth.jwt.secret}") String secret, @Value("${auth.jwt.expiry-hours:24}") long expiryHours) {
        if (secret == null || secret.length() < 32) throw new IllegalStateException("JWT secret must contain at least 32 characters");
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).withIssuer("clearly-auth").build();
        this.expiryHours = expiryHours;
    }

    public String issue(UserAccount user) {
        Instant now = Instant.now();
        String subject = user.email() != null ? user.email() : user.phone();
        return JWT.create().withIssuer("clearly-auth").withSubject(subject).withClaim("uid", user.id())
            .withClaim("name", user.name()).withClaim("role", user.role()).withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plus(expiryHours, ChronoUnit.HOURS))).sign(algorithm);
    }

    public DecodedJWT verify(String token) { return verifier.verify(token); }
    public long expirySeconds() { return expiryHours * 3600; }
}
