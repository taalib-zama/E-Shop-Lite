package com.eshoplite.user.api;

import jakarta.validation.constraints.*;

public record RegisterRequest(
  @NotBlank @Size(min=2,max=100) String name,
  @NotBlank @Email String email,
  @NotBlank @Size(min=8,max=255) String password
){}
