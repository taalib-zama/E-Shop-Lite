# AI QA Agent (Java Skeleton)

This is the **User Story–driven** AI QA Agent skeleton for E‑Shop Lite. It parses a story markdown file with a YAML block, creates a small **test plan**, generates **RestAssured JUnit tests**, and prints the command to execute them.

> Java 21 · Spring Boot CLI · JUnit 5 · RestAssured · SnakeYAML · Flexmark

## Structure
```
ai-qa-agent/
  pom.xml                # parent
  agent-cli/             # Spring Boot CLI app
    pom.xml
    src/main/java/com/eshoplite/qa/agent/...
  generated-tests/       # tests generated here
    pom.xml
    src/test/java/com/eshoplite/qa/generated/SampleConnectivityTest.java
  stories/
    US-01-register-user.md
```

## Usage

1) Ensure your local **E‑Shop Lite** services (gateway) are running at `http://localhost:8080`.

2) From `ai-qa-agent/`, build:
```bash
mvn -q -DskipTests package
```

3) Run the agent (defaults use `stories/US-01-register-user.md` and write to `qa/reports/US-01`):
```bash
java -jar agent-cli/target/agent-cli-0.1.0-SNAPSHOT.jar --story ../stories/US-01-register-user.md --out ../qa/reports/US-01 --gen ../generated-tests/src/test/java/com/eshoplite/qa/generated
```

4) Execute the generated tests:
```bash
mvn -q -pl generated-tests -DbaseUrl=http://localhost:8080 test
```

### Options
```
--story <path>   Path to the story markdown (default ../stories/US-01-register-user.md)
--root <path>    Project root (default ..)
--out <dir>      Report output dir (default ../qa/reports/US-01)
--gen <dir>      Directory for generated tests (default ../generated-tests/src/test/java/com/eshoplite/qa/generated)
--dry-run        Do not run tests; only generate plan & tests
-h, --help       Show help
```

## Story Format
Provide a fenced **YAML** block in your story markdown:

```yaml
id: US-01
title: Register User
description: As a visitor I want to register so that I can log in.
ac:
  - name: valid_registration
    given: I provide valid name, email, password
    when: I POST /users
    then: 201 Created
```

## Next Steps (extend the skeleton)
- Map AC → parameterized **TestCase** generation per endpoint and role (ADMIN/USER/ANON)
- Add **login** helper to fetch JWT for admin-product tests
- Add **Observer** to query Prometheus/Loki/Jaeger for anomalies around test timestamps
- Create a **Reporter** that aggregates JUnit XML, logs, traces, and writes a story-level HTML report
- Optional: add a **Karate**/Cucumber layer if you want BDD feature files

## License
MIT (or your choice)
