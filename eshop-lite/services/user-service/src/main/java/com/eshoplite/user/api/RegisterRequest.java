package com.eshoplite.user.api;

import jakarta.validation.constraints.*;

public record RegisterRequest(
  @NotBlank @Size(min=2,max=100) String name,
  @NotBlank @Email String email,
  @NotBlank @Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,255}$",
    message = "must be at least 8 characters with upper, lower, digit and special character"
  ) String password
){}
