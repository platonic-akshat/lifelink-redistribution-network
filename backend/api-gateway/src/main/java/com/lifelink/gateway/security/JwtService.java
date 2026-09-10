package com.lifelink.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Issues and validates the starter-grade HS256 JWTs used across the gateway.
 *
 * This is intentionally minimal: there is no real user directory behind
 * {@link com.lifelink.gateway.web.AuthController} (see its javadoc). In a
 * real deployment this whole class would be replaced by token validation
 * against a proper IdP (Keycloak/Auth0/etc.) instead of the gateway minting
 * its own tokens.
 */
@Component
public class JwtService {

    private final SecretKey signingKey;
    private final long expirySeconds;

    public JwtService(
            @Value("${lifelink.jwt.secret}") String secret,
            @Value("${lifelink.jwt.expiry-seconds:86400}") long expirySeconds) {
        // NOTE: the secret is read from a yml default for local dev only.
        // In any real deployment this MUST be injected via an environment
        // variable backed by a secret manager (Vault/AWS Secrets
        // Manager/etc.) - never bake a signing secret into an image.
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirySeconds = expirySeconds;
    }

    public String issueToken(String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirySeconds)))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parses and validates (signature + expiry) the given compact JWT.
     *
     * @throws JwtException if the token is malformed, has an invalid
     *     signature, or is expired.
     */
    public Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
