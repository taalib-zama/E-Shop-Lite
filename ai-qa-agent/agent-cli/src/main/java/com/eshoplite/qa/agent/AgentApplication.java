package com.eshoplite.qa.agent;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.eshoplite.qa.agent.story.StoryLoader;
import com.eshoplite.qa.agent.planner.PlannerService;
import com.eshoplite.qa.agent.generator.TestGeneratorService;
import com.eshoplite.qa.agent.orchestrator.TestOrchestrator;
import com.eshoplite.qa.agent.report.ReporterService;

@SpringBootApplication
public class AgentApplication implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        CliOptions options = CliOptions.parse(args);
        if (options.showHelp) {
            CliOptions.printHelp();
            return;
        }
        System.out.println("AI QA Agent (User Story Mode) starting...
");

        // 1) Load story
        var story = StoryLoader.load(options.storyPath);

        // 2) Plan
        var plan = PlannerService.createPlan(story);
        PlannerService.writePlan(plan, options.outputDir);

        // 3) Generate tests
        TestGeneratorService.generateTests(plan, options.generatedTestsDir);

        // 4) Orchestrate execution (optional dry-run)
        if (!options.dryRun) {
            TestOrchestrator.execute(options.projectRoot);
        }

        // 5) Report
        ReporterService.writeSummary(plan, options.outputDir);

        System.out.println("Done. Reports in: " + options.outputDir);
    }

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
