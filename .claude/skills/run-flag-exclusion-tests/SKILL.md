---
name: run-flag-exclusion-tests
description: Run the @flag-exclusion Karate e2e tests for the task-service Flag exclusion feature (INE-406). Use this skill whenever the user wants to run flag exclusion tests, test the flag exclusion feature, or verify that the task-service correctly blocks task creation when a Flag resource exists. Triggers on phrases like "run flag tests", "test flag exclusion", "run @flag-exclusion", or "verify flag blocking".
---

# Run Flag Exclusion Tests

Runs the `@flag-exclusion` tagged Karate BDD tests that validate the task-service Flag exclusion feature.

## What it tests

1. **Scenario 1:** Flag prevents task creation via workflow-event path
2. **Scenario 2:** Existing task cancelled after Flag creation

## Usage

The user may specify an environment. If not specified, default to `dev`.

Accepted environments: `dev`, `staging`

## Steps

1. **Determine environment** from user input (default: `dev`)

2. **Load credentials** from the appropriate `.env` file in the project root:
   - `dev` → `.env`
   - `staging` → `.env.staging`

   Each file contains: `CLIENT_ID`, `CLIENT_SECRET`, `CLIENT_KEY`, `KARATE_ENV`

3. **Switch to Java 17** (required for Lombok/Karate compatibility):
   ```bash
   source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 17.0.10-tem
   ```

4. **Run the tests:**
   ```bash
   ./gradlew test \
     -Dkarate.options="--tags @flag-exclusion" \
     -Dkarate.env=<env> \
     -Dclient.id=<CLIENT_ID> \
     -Dclient.secret=<CLIENT_SECRET> \
     -Dclient.key=<CLIENT_KEY>
   ```

5. **Show results summary** by extracting key lines from the Karate HTML report:
   ```bash
   cat build/karate-reports/feature.task.task-flag-exclusion.html | \
     sed -n 's/.*\[print\] //p' | sed 's/ *<.*//' | \
     grep -E "(SCENARIO.*PASSED|SCENARIO.*FAILED|VALIDATE|STEP-5|STEP-6|flagged)"
   ```

6. **Verify exclusion log in Groundcover** — confirm task-service actually processed and blocked the request:
   - Extract the Scenario 1 test patientId from the report:
     ```bash
     cat build/karate-reports/feature.task.task-flag-exclusion.html | \
       sed -n 's/.*\[print\] //p' | sed 's/ *<.*//' | \
       grep "STEP-2.*Test user created" | grep -oE '[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}'
     ```
   - Query Groundcover using the patientId as the initial filter (NOT inside `content:"..."`):
     ```
     <patientId> | filter level:"info" namespace:"task-service-staging" | limit 10
     ```
     For dev, use `namespace:"task-service-dev"`.
   - Look for the log: `"Patient <patientId> is excluded from activity <activityId> by Flag resource, skipping task creation"`
   - If the log is NOT found, the test may be a false positive (event was never sent or Flag didn't match)

7. **Report outcome** to the user:
   - If BUILD SUCCESSFUL AND exclusion log found: both scenarios passed with server-side confirmation
   - If BUILD SUCCESSFUL but NO exclusion log: warn user — test passed but Flag exclusion may not have been exercised
   - If BUILD FAILED: identify which scenario failed and show relevant error output

## Important notes

- The `.env` and `.env.staging` files are gitignored and must exist locally with valid credentials
- Tests take ~30s on dev, ~30s on staging
- Use `timeout: 300000` for the gradle command (5 min max)
- If Java version error occurs (`NoSuchFieldError`), sdkman needs to switch to Java 17