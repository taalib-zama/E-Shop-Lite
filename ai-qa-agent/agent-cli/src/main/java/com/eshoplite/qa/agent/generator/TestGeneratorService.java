package com.eshoplite.qa.agent.generator;

import com.eshoplite.qa.agent.model.TestPlan;
import com.eshoplite.qa.agent.model.TestCase;

import java.io.FileWriter;
import java.nio.file.*;

public class TestGeneratorService {
    public static void generateTests(TestPlan plan, String genDir) throws Exception {
        Path base = Path.of(genDir);
        Files.createDirectories(base);
        String className = plan.storyId().replaceAll("[^A-Za-z0-9]", "") + "Tests";
        Path file = base.resolve(className + ".java");
        try (var w = new FileWriter(file.toFile())) {
            w.write("package com.eshoplite.qa.generated;

");
            w.write("import org.junit.jupiter.api.*;
");
            w.write("import static io.restassured.RestAssured.*;
");
            w.write("import static org.hamcrest.Matchers.*;
");
            w.write("import java.util.*;

");
            w.write("public class " + className + " {

");
            w.write("    private String baseUrl = System.getProperty("baseUrl", "http://localhost:8080");

");
            for (TestCase tc : plan.cases()) {
                w.write("    @Test
");
                w.write("    public void " + tc.name() + "() {
");
                w.write("        given().contentType("application/json")
");
                if (tc.payload() != null && !tc.payload().isEmpty()) {
                    w.write("            .body(new java.util.LinkedHashMap() {{
");
                    for (var e : tc.payload().entrySet()) {
                        var key = e.getKey();
                        var val = e.getValue();
            w.write("                put(""+key+"", ""+String.valueOf(val).replace("\\","\\\\").replace(""","\\"")+"");
");
                    }
                    w.write("            }})
");
                }
                w.write("        .when().request("" + tc.method() + "", baseUrl + "" + tc.path() + "")
");
                w.write("        .then().statusCode(" + tc.expectedStatus() + ");
");
                w.write("    }

");
            }
            w.write("}
");
        }
    }
}
