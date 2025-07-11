package com.example.inventoryManagementSystem.util;

import com.example.inventoryManagementSystem.service.impl.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final SecretKey jwtSecretKey;
    private final int jwtExpirationMs;
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String JWT_COOKIE_NAME = "jwtToken";

    public JwtUtils(
            @Value("${app.jwtSecret}") String jwtSecret,
            @Value("${app.jwtExpirationMs}") int jwtExpirationMs) {
        // Convert the base64 encoded secret key to a SecretKey object
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return buildToken(userPrincipal.getUsername());
    }

    public String generateJwtTokenFromUsername(String username) {
        return buildToken(username);
    }

    private String buildToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(jwtSecretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getJwtFromHeader(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(BEARER_PREFIX)) {
            return headerAuth.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public boolean validateJwtToken(String authToken) {
        try {
            if (tokenBlacklist.contains(authToken)) {
                logger.error("JWT token is blacklisted");
                return false;
            }

            Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public void invalidateToken(String token) {
        if (token != null) {
            tokenBlacklist.add(token);
            logger.info("Token invalidated and added to blacklist");
        }
    }

    public void cleanExpiredTokens() {
        tokenBlacklist.removeIf(token -> {
            try {
                Date expiration = getExpirationDateFromToken(token);
                return expiration.before(new Date());
            } catch (Exception e) {
                // If we can't parse the token, it's probably invalid anyway
                return true;
            }
        });
        logger.info("Cleaned expired tokens from blacklist");
    }
}