package com.eshoplite.qa.agent.planner;

import com.eshoplite.qa.agent.model.*;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.nio.file.*;
import java.util.*;

public class PlannerService {
    public static TestPlan createPlan(Story story) {
        // For MVP, derive a tiny plan: map common ACs to test cases for user/catalog APIs
        List<TestCase> cases = new ArrayList<>();
        // Example mapping for Sprint 1 endpoints
        // Registration 201
        cases.add(new TestCase("register_valid_201", "POST", "/users", 201,
                Map.of("name","Taalib","email","taalib+"+System.currentTimeMillis()+"@example.com","password","S3cureP@ss!"),
                "ANON"));
        // Duplicate 409
        cases.add(new TestCase("register_duplicate_409", "POST", "/users", 409,
                Map.of("name","Dup","email","dup@example.com","password","S3cureP@ss!"),
                "ANON"));
        // Weak password 400
        cases.add(new TestCase("register_weak_400", "POST", "/users", 400,
                Map.of("name","Weak","email","weak@example.com","password","123"),
                "ANON"));

        return new TestPlan(story.id(), story.title(), null, cases);
    }

    public static void writePlan(TestPlan plan, String outputDir) throws Exception {
        Files.createDirectories(Path.of(outputDir));
        var yaml = new Yaml().dump(Map.of(
                "storyId", plan.storyId(),
                "storyTitle", plan.storyTitle(),
                "cases", plan.cases().stream().map(tc -> Map.of(
                        "name", tc.name(),
                        "method", tc.method(),
                        "path", tc.path(),
                        "expectedStatus", tc.expectedStatus(),
                        "payload", tc.payload(),
                        "role", tc.role()
                )).toList()
        ));
        try (var fw = new FileWriter(Path.of(outputDir, "test-plan.yaml").toFile())) {
            fw.write(yaml);
        }
    }
}
