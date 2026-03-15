package com.money.kark_profit.utils;

import com.money.kark_profit.cache.ConfigurationCache;
import com.money.kark_profit.constants.ApplicationCache;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(
                ConfigurationCache.getByKeyName(ApplicationCache.MASTER_KEY)
                        .toString()
                        .getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        long tokenLifeSpan = Long.parseLong(
                ConfigurationCache.getByKeyName(ApplicationCache.TOKEN_LIFE_SPAN).getValue()
        );

        Date issuedAt = new Date();
        Date expiration = new Date(System.currentTimeMillis() + tokenLifeSpan);

        log.info("🔐 Token generated for user: {} - Expires: {}", username, expiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();

        } catch (ExpiredJwtException e) {
            log.warn("⚠️ Token is expired! Expired at: {}", e.getClaims().getExpiration());
            return e.getClaims().getSubject(); // Still return username for logging
        } catch (JwtException e) {
            log.error("❌ Invalid token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * ✅ NEW METHOD: Validate token (checks expiration!)
     * This is what your filter is calling
     */
    public boolean validateToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);

            Date expiration = claimsJws.getBody().getExpiration();
            Date now = new Date();

            boolean isValid = !expiration.before(now);

            if (isValid) {
                long secondsLeft = (expiration.getTime() - now.getTime()) / 1000;
                log.debug("✅ Token valid for user: {} - {} seconds remaining",
                        claimsJws.getBody().getSubject(), secondsLeft);
            } else {
                log.warn("❌ Token expired at: {}", expiration);
            }

            return isValid;

        } catch (ExpiredJwtException e) {
            log.warn("❌ Token expired at: {}", e.getClaims().getExpiration());
            return false;
        } catch (JwtException e) {
            log.error("❌ Invalid token signature: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("❌ Token validation error: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Helper method to check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            return expiration.before(new Date());

        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * Get expiration date from token
     */
    public Date getExpirationDate(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

        } catch (ExpiredJwtException e) {
            return e.getClaims().getExpiration();
        } catch (JwtException e) {
            return null;
        }
    }
}