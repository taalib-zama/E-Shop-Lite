package com.eshoplite.user.service;

import com.eshoplite.user.api.RegisterRequest;
import com.eshoplite.user.domain.User;
import com.eshoplite.user.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

  private final UserRepository repo;
  private final PasswordEncoder encoder;

  public RegistrationService(UserRepository repo, PasswordEncoder encoder) {
    this.repo = repo;
    this.encoder = encoder;
  }

  public User register(RegisterRequest r) {
    if (repo.existsByEmail(r.email())) throw new IllegalStateException("DUPLICATE_EMAIL");
    User u = new User();
    u.setName(r.name());
    u.setEmail(r.email());
    u.setPasswordHash(encoder.encode(r.password()));
    return repo.save(u);
  }
}
