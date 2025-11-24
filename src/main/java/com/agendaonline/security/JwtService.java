package com.agendaonline.security;

import com.agendaonline.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final Key signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecret()));
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.getAccessTokenValidityMinutes(), ChronoUnit.MINUTES);
        return buildToken(subject, claims, expiry);
    }

    public String generateRefreshToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.getRefreshTokenValidityDays(), ChronoUnit.DAYS);
        return buildToken(subject, claims, expiry);
    }

    public boolean isTokenValid(String token, String expectedSubject) {
        String subject = extractAllClaims(token).getSubject();
        return subject.equals(expectedSubject) && !isExpired(token);
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    private String buildToken(String subject, Map<String, Object> claims, Instant expiry) {
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(expiry))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    private boolean isExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
