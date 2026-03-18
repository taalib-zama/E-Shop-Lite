package com.eshoplite.qa.agent.model;

import java.util.List;

public record Story(String id, String title, String description, List<AcceptanceCriterion> acceptanceCriteria) {}
