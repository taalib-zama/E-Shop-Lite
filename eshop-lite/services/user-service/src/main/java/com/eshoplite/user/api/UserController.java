package com.eshoplite.user.api;

import com.eshoplite.user.domain.User;
import com.eshoplite.user.repo.UserRepository;
import com.eshoplite.user.service.JwtService;
import com.eshoplite.user.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
  private final RegistrationService registrationService;
  private final UserRepository repo;
  private final JwtService jwtService;

  public UserController(RegistrationService rs, UserRepository repo, JwtService jwt){
    this.registrationService = rs; this.repo = repo; this.jwtService = jwt;
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(@Valid @RequestBody RegisterRequest r){
    try {
      User u = registrationService.register(r);
      return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole());
    } catch (IllegalStateException ex){
      if ("DUPLICATE_EMAIL".equals(ex.getMessage())){
        throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
      }
      throw ex;
    }
  }

  @PostMapping("/auth/login")
  public JwtResponse login(@Valid @RequestBody LoginRequest r){
    var u = repo.findByEmail(r.email()).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED));
    // Password check omitted for brevity in skeleton
    String token = jwtService.generateToken(u.getEmail(), u.getRole());
    return new JwtResponse(token, "Bearer", 15*60);
  }

  @GetMapping("/users/me")
  public UserResponse me(Authentication auth){
    if (auth==null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED);
    var u = repo.findByEmail(String.valueOf(auth.getPrincipal())).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED));
    return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole());
  }
}
