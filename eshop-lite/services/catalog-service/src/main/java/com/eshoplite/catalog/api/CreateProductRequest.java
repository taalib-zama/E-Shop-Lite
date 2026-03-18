package com.eshoplite.catalog.api;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
  @NotBlank String name,
  @NotBlank String sku,
  @NotNull @DecimalMin("0.01") BigDecimal price,
  String currency,
  String description,
  String category
){}
