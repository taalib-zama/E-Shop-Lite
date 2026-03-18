package com.eshoplite.user.api;

import jakarta.validation.constraints.*;

public record LoginRequest(
  @NotBlank @Email String email,
  @NotBlank String password
){}
