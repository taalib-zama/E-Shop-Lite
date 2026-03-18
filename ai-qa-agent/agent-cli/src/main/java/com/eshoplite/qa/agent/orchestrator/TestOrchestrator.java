package com.eshoplite.qa.agent.orchestrator;

public class TestOrchestrator {
    public static void execute(String projectRoot) throws Exception {
        // For skeleton: print the Maven commands the user can run
        System.out.println("[Orchestrator] Run tests with:
  mvn -q -pl generated-tests -DbaseUrl=http://localhost:8080 test
");
    }
}
