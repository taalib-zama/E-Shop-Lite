package com.eshoplite.user.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
  @Value("${security.jwt.secret:dev-secret}")
  private String secret;

  @Value("${security.jwt.expMinutes:15}")
  private long expMinutes;

  public String generateToken(String subject, String role){
    long now = System.currentTimeMillis();
    Date iat = new Date(now);
    Date exp = new Date(now + expMinutes * 60_000);
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    return Jwts.builder()
        .setSubject(subject)
        .claim("role", role)
        .setIssuedAt(iat)
        .setExpiration(exp)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }
}
