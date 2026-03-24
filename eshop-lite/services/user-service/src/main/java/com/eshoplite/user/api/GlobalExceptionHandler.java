package com.eshoplite.user.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest req) {
    List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> Map.of("field", fe.getField(), "message", String.valueOf(fe.getDefaultMessage())))
        .toList();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
        "type", "https://eshop-lite/errors/validation",
        "title", "Validation failed",
        "status", 400,
        "detail", "Field constraints violated",
        "errors", errors
    ));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
    return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
        "type", "https://eshop-lite/errors/" + ex.getStatusCode().value(),
        "title", ex.getReason() != null ? ex.getReason() : ex.getMessage(),
        "status", ex.getStatusCode().value()
    ));
  }
}
