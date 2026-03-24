package com.eshoplite.user.api;

import com.eshoplite.user.domain.User;
import com.eshoplite.user.repo.UserRepository;
import com.eshoplite.user.service.JwtService;
import com.eshoplite.user.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UserController {

  private final RegistrationService registrationService;
  private final UserRepository repo;
  private final JwtService jwtService;
  private final PasswordEncoder encoder;

  public UserController(RegistrationService rs, UserRepository repo, JwtService jwt, PasswordEncoder encoder) {
    this.registrationService = rs;
    this.repo = repo;
    this.jwtService = jwt;
    this.encoder = encoder;
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(@Valid @RequestBody RegisterRequest r) {
    try {
      User u = registrationService.register(r);
      return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole());
    } catch (IllegalStateException ex) {
      if ("DUPLICATE_EMAIL".equals(ex.getMessage()))
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
      throw ex;
    }
  }

  @PostMapping("/auth/login")
  public JwtResponse login(@Valid @RequestBody LoginRequest r) {
    User u = repo.findByEmail(r.email())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    if (!encoder.matches(r.password(), u.getPasswordHash()))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    return new JwtResponse(jwtService.generateToken(u.getEmail(), u.getRole()), "Bearer", 15 * 60);
  }

  @GetMapping("/users/me")
  public UserResponse me(Authentication auth) {
    if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    User u = repo.findByEmail(String.valueOf(auth.getPrincipal()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole());
  }
}
