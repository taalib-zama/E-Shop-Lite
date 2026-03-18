package com.eshoplite.user.api;

public record JwtResponse(String accessToken, String tokenType, long expiresIn) {}
