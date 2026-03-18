package com.eshoplite.user.service;

import com.eshoplite.user.api.RegisterRequest;
import com.eshoplite.user.domain.User;
import com.eshoplite.user.repo.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {
  private final UserRepository repo;
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  public RegistrationService(UserRepository repo){ this.repo = repo; }

  public User register(RegisterRequest r){
    if (repo.existsByEmail(r.email())) throw new IllegalStateException("DUPLICATE_EMAIL");
    User u = new User();
    u.setName(r.name());
    u.setEmail(r.email());
    u.setPasswordHash(encoder.encode(r.password()));
    u.setRole("USER");
    return repo.save(u);
  }
}
