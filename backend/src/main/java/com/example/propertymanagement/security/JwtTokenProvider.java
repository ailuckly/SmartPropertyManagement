package com.example.propertymanagement.security;

import com.example.propertymanagement.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * Central utility that issues and validates JWT access tokens as well as opaque refresh tokens.
 * The signing key is initialised once per application boot using the shared secret from configuration.
 */
@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final Key signingKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a JWT access token for the given principal. Convenience overload for frameworks that expose UserDetails.
     */
    public String generateAccessToken(UserDetails principal) {
        return generateAccessToken(principal.getUsername());
    }

    /**
     * Creates a JWT access token for the provided username.
     */
    public String generateAccessToken(String username) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiry))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Calculates the Instant when a token generated "now" would expire.
     */
    public Instant getAccessTokenExpiryInstant() {
        return Instant.now().plus(jwtProperties.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);
    }

    /**
     * Calculates the Instant when a refresh token generated "now" would expire.
     */
    public Instant getRefreshTokenExpiryInstant() {
        return Instant.now().plus(jwtProperties.getRefreshTokenExpirationDays(), ChronoUnit.DAYS);
    }

    /**
     * Validates the supplied token signature and expiry. Returns {@code false} on any parsing errors.
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Extracts the subject claim (username) from the token.
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    /**
     * Returns the parsed expiry date for introspection operations.
     */
    public Date getExpiryDate(String token) {
        return parseClaims(token).getBody().getExpiration();
    }

    /**
     * Generates a cryptographically random refresh token value.
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token);
    }
}
