package com.eshoplite.platform.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Value("${security.jwt.secret:changeme}")
  private String jwtSecret;

  private static final List<String> PUBLIC_POST = List.of("/users", "/auth/login");
  private static final List<String> PUBLIC_GET = List.of("/products", "/products/");

  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    return http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(ex -> ex
            .pathMatchers(HttpMethod.POST, PUBLIC_POST.toArray(String[]::new)).permitAll()
            .pathMatchers(HttpMethod.GET, "/actuator/**").permitAll()
            .pathMatchers(HttpMethod.GET, "/products/**").permitAll()
            .anyExchange().authenticated()
        )
        .addFilterAt((exchange, chain) -> authenticate(exchange).flatMap(auth -> chain.filter(exchange)),
            ServerHttpSecurity.WebFilterOrder.AUTHENTICATION)
        .httpBasic(Customizer.withDefaults())
        .build();
  }

  private Mono<AbstractAuthenticationToken> authenticate(ServerWebExchange exchange) {
    var authz = exchange.getRequest().getHeaders().getFirst("Authorization");
    if (authz == null || !authz.startsWith("Bearer ")) return Mono.empty();
    try {
      String token = authz.substring(7);
      Claims claims = Jwts.parser()
          .setSigningKey(jwtSecret.getBytes())
          .build()
          .parseSignedClaims(token)
          .getPayload();
      String role = String.valueOf(claims.get("role", String.class));
      var authorities = role != null ? List.of(new SimpleGrantedAuthority("ROLE_" + role)) : List.of();
      var auth = new AbstractAuthenticationToken(authorities) {
        @Override public Object getCredentials() { return token; }
        @Override public Object getPrincipal() { return claims.getSubject(); }
      };
      auth.setAuthenticated(true);
      return Mono.just(auth);
    } catch (Exception e) {
      return Mono.empty();
    }
  }
}
