package com.eshoplite.qa.agent.model;

import java.util.Map;

public record TestCase(String name, String method, String path, int expectedStatus, Map<String,Object> payload, String role) {}
