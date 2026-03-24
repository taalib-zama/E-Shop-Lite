package com.eshoplite.user.service;

import com.eshoplite.user.api.RegisterRequest;
import com.eshoplite.user.domain.User;
import com.eshoplite.user.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

  @Mock UserRepository repo;
  @Mock PasswordEncoder encoder;
  @InjectMocks RegistrationService service;

  private final RegisterRequest validRequest =
      new RegisterRequest("John Doe", "john@example.com", "Secret@123");

  @Test
  void register_validUser_savesAndReturnsUser() {
    when(repo.existsByEmail("john@example.com")).thenReturn(false);
    when(encoder.encode("Secret@123")).thenReturn("hashed");
    User saved = new User();
    saved.setName("John Doe");
    saved.setEmail("john@example.com");
    saved.setPasswordHash("hashed");
    when(repo.save(any(User.class))).thenReturn(saved);

    User result = service.register(validRequest);

    assertThat(result.getEmail()).isEqualTo("john@example.com");
    assertThat(result.getPasswordHash()).isEqualTo("hashed");
    assertThat(result.getRole()).isNull(); // set by @PrePersist in real DB
    verify(repo).save(any(User.class));
  }

  @Test
  void register_duplicateEmail_throwsIllegalState() {
    when(repo.existsByEmail("john@example.com")).thenReturn(true);

    assertThatThrownBy(() -> service.register(validRequest))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("DUPLICATE_EMAIL");

    verify(repo, never()).save(any());
  }

  @Test
  void register_passwordIsHashed() {
    when(repo.existsByEmail(any())).thenReturn(false);
    PasswordEncoder real = new BCryptPasswordEncoder();
    RegistrationService svc = new RegistrationService(repo, real);
    when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    User result = svc.register(validRequest);

    assertThat(result.getPasswordHash()).isNotEqualTo("Secret@123");
    assertThat(real.matches("Secret@123", result.getPasswordHash())).isTrue();
  }
}
