package com.gdg.sprint.team1.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.gdg.sprint.team1.config.JwtProperties;
import com.gdg.sprint.team1.entity.User.UserRole;
import com.gdg.sprint.team1.exception.AuthExpiredException;
import com.gdg.sprint.team1.exception.AuthInvalidException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE = "role";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public String createToken(Integer userId, UserRole role) {
        long now = System.currentTimeMillis();
        Date expiry = new Date(now + jwtProperties.getAccessExpireMinutes() * 60 * 1000);
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim(CLAIM_USER_ID, userId)
            .claim(CLAIM_ROLE, role.name())
            .issuedAt(new Date(now))
            .expiration(expiry)
            .signWith(secretKey)
            .compact();
    }

    /** Refresh Token 생성 (만료: refreshExpireDays 일) */
    public String createRefreshToken(Integer userId, UserRole role) {
        long now = System.currentTimeMillis();
        long refreshMinutes = jwtProperties.getRefreshExpireDays() * 24 * 60;
        Date expiry = new Date(now + refreshMinutes * 60 * 1000);
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim(CLAIM_USER_ID, userId)
            .claim(CLAIM_ROLE, role.name())
            .issuedAt(new Date(now))
            .expiration(expiry)
            .signWith(secretKey)
            .compact();
    }

    public TokenPayload parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            Integer userId = claims.get(CLAIM_USER_ID, Integer.class);
            String roleStr = claims.get(CLAIM_ROLE, String.class);
            UserRole role = roleStr != null ? UserRole.valueOf(roleStr) : UserRole.USER;
            return new TokenPayload(userId, role);
        } catch (ExpiredJwtException e) {
            throw new AuthExpiredException();
        } catch (JwtException e) {
            throw new AuthInvalidException();
        }
    }

    public record TokenPayload(Integer userId, UserRole role) {}
}
