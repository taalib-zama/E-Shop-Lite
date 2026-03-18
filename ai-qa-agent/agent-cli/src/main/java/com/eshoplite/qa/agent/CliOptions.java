package com.eshoplite.qa.agent;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CliOptions {
    public final String storyPath;
    public final String projectRoot;
    public final String outputDir;
    public final String generatedTestsDir;
    public final boolean dryRun;
    public final boolean showHelp;

    private CliOptions(String storyPath, String projectRoot, String outputDir, String generatedTestsDir, boolean dryRun, boolean showHelp) {
        this.storyPath = storyPath;
        this.projectRoot = projectRoot;
        this.outputDir = outputDir;
        this.generatedTestsDir = generatedTestsDir;
        this.dryRun = dryRun;
        this.showHelp = showHelp;
    }

    public static CliOptions parse(String[] args) {
        String story = "../stories/US-01-register-user.md"; // default sample
        String root = ".."; // parent directory as default project root
        String out = "../qa/reports/US-01";
        String gen = "../generated-tests/src/test/java/com/eshoplite/qa/generated";
        boolean dry = false;
        boolean help = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--story": story = args[++i]; break;
                case "--root": root = args[++i]; break;
                case "--out": out = args[++i]; break;
                case "--gen": gen = args[++i]; break;
                case "--dry-run": dry = true; break;
                case "-h": case "--help": help = true; break;
                default: System.err.println("Unknown arg: " + args[i]);
            }
        }
        return new CliOptions(story, root, out, gen, dry, help);
    }

    public static void printHelp() {
        System.out.println("Usage: java -jar agent-cli.jar [--story <path>] [--root <projectRoot>] [--out <reportDir>] [--gen <testsDir>] [--dry-run]
" +
                           "Defaults: --story ../stories/US-01-register-user.md --root .. --out ../qa/reports/US-01 --gen ../generated-tests/src/test/java/com/eshoplite/qa/generated
");
    }
}
