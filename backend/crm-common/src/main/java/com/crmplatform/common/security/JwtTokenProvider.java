package com.crmplatform.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // This is Base64 encoded and safe for HS512
    private final String secretKey = "u5jZf4iQswG1vQ6E8dB+LqAfS7P3mK4nV1rjv6yO9wXf+zC5iQb7mLqX1tYwA9oU2cD7hYk3qT5lE8sP4zV7nB6mQ2rW5tL8v"
            .getBytes() // If not encoded, we’ll encode manually below
            .length > 0 ? java.util.Base64.getEncoder().encodeToString(
                "u5jZf4iQswG1vQ6E8dB+LqAfS7P3mK4nV1rjv6yO9wXf+zC5iQb7mLqX1tYwA9oU2cD7hYk3qT5lE8sP4zV7nB6mQ2rW5tL8v".getBytes()
            ) : "";

    private final long jwtExpirationMs = 86400000; // 1 day

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, Long userId, Long tenantId, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("tenantId", tenantId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public Long getTenantIdFromToken(String token) {
        return getClaims(token).get("tenantId", Long.class);
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
