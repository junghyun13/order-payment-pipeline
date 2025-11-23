package com.example.orderpay.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long expire = 1000 * 60 * 60; // 1시간

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(key)
                .compact();
    }

    public String getUsername(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    public boolean isExpired(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token)
                    .getBody().getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    public long getExpirationMillis(String token) {
        try {
            Date exp = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return exp.getTime() - System.currentTimeMillis();
        } catch (JwtException e) {
            return 0;
        }
    }

    public String extractUsername(String token) {
        return getUsername(token); // 기존 getUsername() 사용
    }

}
