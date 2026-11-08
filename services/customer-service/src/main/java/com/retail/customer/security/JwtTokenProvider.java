package com.retail.customer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:RetailMicroservicesSystemSecretKeyForJwtSigningMustBeAtLeast256BitsLong2026!}")
    private String jwtSecret;

    // 15 minutes token expiration per NFR 5.3
    private static final long ACCESS_TOKEN_VALIDITY_SECONDS = 15 * 60; // 900 seconds
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String email, String roles) {
        Instant now = Instant.now();
        Instant expiry = now.plus(ACCESS_TOKEN_VALIDITY_SECONDS, ChronoUnit.SECONDS);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(Long userId, String email) {
        Instant now = Instant.now();
        Instant expiry = now.plus(REFRESH_TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("type", "REFRESH")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims validateAndExtractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(String token) {
        Claims claims = validateAndExtractClaims(token);
        return Long.parseLong(claims.getSubject());
    }
}

