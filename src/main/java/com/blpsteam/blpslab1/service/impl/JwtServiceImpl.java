package com.blpsteam.blpslab1.service.impl;

import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.lifetime}")
    private long lifetime;

    @Override
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+lifetime))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }



    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}

