package com.example.backend.security;

import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtils {

    private final String jwtSecret = "CAMBIA-ESTE-SECRET-MUY-LARGO"; // usa variable de entorno en prod
    private final long jwtExpirationMs = 24 * 60 * 60 * 1000; // 1 día

    public String generateToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
            .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
            .setSigningKey(jwtSecret.getBytes())
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret.getBytes()).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}