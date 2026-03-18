package com.eshoplite.user.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {

  @Value("${security.jwt.secret:dev-secret}")
  private String secret;

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable());
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.POST, "/users", "/auth/login").permitAll()
        .requestMatchers("/actuator/**").permitAll()
        .anyRequest().authenticated()
    );
    http.addFilterBefore(new JwtFilter(secret), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  static class JwtFilter extends OncePerRequestFilter {
    private final String secret;
    JwtFilter(String s){ this.secret = s; }
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
      String h = req.getHeader("Authorization");
      if (h!=null && h.startsWith("Bearer ")){
        try {
          String token = h.substring(7);
          Claims claims = Jwts.parser().setSigningKey(secret.getBytes()).build().parseSignedClaims(token).getPayload();
          String role = String.valueOf(claims.get("role", String.class));
          var auth = new AbstractAuthenticationToken(role!=null?List.of(new SimpleGrantedAuthority("ROLE_"+role)):List.of()){
            @Override public Object getCredentials(){return token;}
            @Override public Object getPrincipal(){return claims.getSubject();}
          };
          auth.setAuthenticated(true);
          org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ignored) {}
      }
      chain.doFilter(req, res);
    }
  }
}
