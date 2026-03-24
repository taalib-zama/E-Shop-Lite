package com.eshoplite.user.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

  @Value("${security.jwt.secret:dev-secret}")
  private String secret;

  @Value("${security.jwt.expMinutes:15}")
  private long expMinutes;

  public String generateToken(String subject, String role) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .subject(subject)
        .claim("role", role)
        .issuedAt(new Date(now))
        .expiration(new Date(now + expMinutes * 60_000))
        .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
        .compact();
  }
}
