package com.eshoplite.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordPolicyValidator implements ConstraintValidator<PasswordPolicy, String> {
  @Override
  public boolean isValid(String p, ConstraintValidatorContext c) {
    if (p == null || p.length() < 8 || p.length() > 64) return false;
    boolean up = false, low = false, dig = false, sym = false;
    for (char ch : p.toCharArray()) {
      if (Character.isUpperCase(ch)) up = true;
      else if (Character.isLowerCase(ch)) low = true;
      else if (Character.isDigit(ch)) dig = true;
      else if (!Character.isLetterOrDigit(ch)) sym = true;
    }
    return up && low && dig && sym;
  }
}
