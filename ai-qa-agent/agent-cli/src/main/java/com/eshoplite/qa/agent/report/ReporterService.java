package com.eshoplite.qa.agent.report;

import com.eshoplite.qa.agent.model.TestPlan;

import java.io.FileWriter;
import java.nio.file.*;

public class ReporterService {
    public static void writeSummary(TestPlan plan, String outDir) throws Exception {
        Path p = Path.of(outDir);
        Files.createDirectories(p);
        try (var w = new FileWriter(p.resolve("qa-conclusion.md").toFile())) {
            w.write("# QA Summary for Story: " + plan.storyId() + "

");
            w.write("**Title:** " + plan.storyTitle() + "

");
            w.write("## Planned Test Cases

");
            for (var tc : plan.cases()) {
                w.write("- " + tc.name() + " → expect " + tc.expectedStatus() + " on " + tc.method() + " " + tc.path() + "
");
            }
            w.write("
> Execute: `mvn -q -pl generated-tests -DbaseUrl=http://localhost:8080 test`
");
        }
    }
}
