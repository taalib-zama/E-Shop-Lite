package com.eshoplite.qa.agent.model;

import java.util.List;

public record TestPlan(String storyId, String storyTitle, java.nio.file.Path planFile, java.util.List<TestCase> cases) {}
