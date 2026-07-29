# AGENTS.md — HP Validation Tests

This document describes the conventions, patterns, and execution instructions for authoring and running the Karate-based end-to-end validation tests in this project.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Repository Structure](#repository-structure)
3. [Configuration (`karate-config.js`)](#configuration-karate-configjs)
4. [Required Credentials](#required-credentials)
5. [Utility Features Reference](#utility-features-reference)
6. [Authoring CQL Evaluation Tests — Patterns & Conventions](#authoring-cql-evaluation-tests--patterns--conventions)
7. [Payload Files](#payload-files)
8. [Executing Tests](#executing-tests)
9. [Test Reports](#test-reports)

---

## Project Overview

This project validates health-plan (HP) CQL measure libraries deployed to the bSights CQL Engine. Each feature file exercises a named CQL library (`libraryId`) against a synthetic patient with specific FHIR resources loaded, then asserts that the CQL evaluation returns the expected cohort membership, numerator, and completion date values.

The test suite also covers DQM workflows, questionnaire service flows, and task lifecycle scenarios, but the primary focus documented here is the `feature/cql/` suite.

---

## Repository Structure

```
src/test/java/
├── karate-config.js               # Global Karate configuration, env switching, shared helpers
├── runner/
│   └── KarateTestRunner.java      # JUnit5 parallel runner (entry point for Gradle)
├── feature/
│   └── cql/                       # One .feature file per CQL measure library
│       ├── bcs1-evaluation.feature
│       ├── bcs3-evaluation.feature
│       └── ...
├── util/                          # Reusable callable feature utilities
│   ├── get-dates.feature
│   ├── get-token.feature
│   ├── get-user-token.feature
│   ├── create-resource.feature
│   ├── load-resource.feature
│   ├── delete-resource.feature
│   ├── execute-cql.feature
│   ├── cql-evaluation.feature
│   ├── identifier-validators.feature
│   ├── task-constants.feature
│   ├── task-setup.feature
│   ├── task-search.feature
│   ├── task-search-rest.feature
│   ├── consent-creation.feature
│   └── user-creation.feature
└── payload/
    ├── cqlrequest.json            # CQL engine request template
    └── cql/                       # FHIR resource payload templates
        ├── observation-mammography.json
        ├── condition-diabetes.json
        └── ...
```

---

## Configuration (`karate-config.js`)

`karate-config.js` is the global Karate bootstrap. It runs before every feature and populates a `config` object that is available as top-level variables in every scenario.

### Environment selection

The active environment is set via the `karate.env` system property (defaults to `dev`). Supported values:

| Value | Description |
|-------|-------------|
| `dev` | Development environment (`*.dev.bwell.zone`) |
| `staging` | Staging environment (`*.staging.bwell.zone`) |
| `client-sandbox` | Client sandbox (`*.client-sandbox.bwell.zone`) |
| `prod` | Production (`*.prod.bwell.zone`) |

### Key config variables

| Variable | Description |
|----------|-------------|
| `fhirServerUrl` | Base URL for the FHIR R4 server (used by resource utilities) |
| `cqlEngineUrl` | Base URL for the bSights CQL Engine (`/api/v1/library`) |
| `apiGatewayUrl` | API Gateway base URL (used by task/questionnaire tests) |
| `tokenUrl` | Cognito OAuth2 token endpoint |
| `bigUrl` | bWell Identity Gateway (BIG) base URL |
| `bwellIdentityGatewayUrl` | Alternate identity gateway URL used by `user-creation.feature` |
| `clientId` | OAuth2 client ID (from `-Dclient.id`) |
| `clientSecret` | OAuth2 client secret (from `-Dclient.secret`) |
| `clientKey` | Client key used for JWE patient token generation (from `-Dclient.key`) |
| `generateCorrelationId(prefix)` | Helper function — returns `"PREFIX-<8-char-uuid>"` |

### Global retry defaults

```js
karate.configure('retry', { count: 45, interval: 3000 });
```

Individual utilities may override these defaults inline.

---

## Required Credentials

| Property | Gradle flag | How to obtain |
|----------|-------------|---------------|
| OAuth2 Client ID | `-Dclient.id=…` | Request from the team |
| OAuth2 Client Secret | `-Dclient.secret=…` | Request from the team |
| Client Key | `-Dclient.key=…` | Retrieve from the b.well admin tools UI — see [README.md](README.md) |

---

## Utility Features Reference

All utilities are tagged `@ignore` so they are never executed directly. They are invoked via `call read('classpath:util/<name>.feature')` from within a scenario or background.

---

### `util/get-dates.feature`

Populates a set of pre-calculated date variables using Java's `ZonedDateTime` (UTC). Call this once in the `Background`.

**Variables exposed after call:**

| Variable | Format | Example |
|----------|--------|---------|
| `currentDate` | `yyyy-MM-dd` | `2026-02-26` |
| `currentDateTime` | ISO-8601 with offset | `2026-02-26T13:00:00+00:00` |
| `yesterdayDate` | `yyyy-MM-dd` | `2026-02-25` |
| `yesterdayDateTime` | ISO-8601 with offset | `2026-02-25T13:00:00+00:00` |
| `yesterdayDateTimeNoTimezone` | `yyyy-MM-dd'T'HH:mm:ss` | `2026-02-25T13:00:00` |
| `threeDaysAgo` | ISO-8601 with offset | |
| `threeDaysAgoNoTimezone` | no offset | |
| `oneWeekAgo` | ISO-8601 with offset | |
| `oneMonthAgo` | ISO-8601 with offset | |
| `threeMonthsAgo` | ISO-8601 with offset | |
| `oneYearAgo` | ISO-8601 with offset | |
| `twoYearsAgo` | ISO-8601 with offset | |
| `threeYearsAgo` | ISO-8601 with offset | |

---

### `util/get-token.feature`

Obtains a service-level OAuth2 access token from Cognito using the `client_credentials` grant.

**Requires (from config):** `clientId`, `clientSecret`, `tokenUrl`

**Returns:** `access_token`

**Usage:**
```gherkin
* def auth_service_token_response = call read('classpath:util/get-token.feature')
* def serviceToken = 'Bearer ' + auth_service_token_response.access_token
```

---

### `util/get-user-token.feature`

Creates a synthetic patient via the BIG identity gateway and returns a user access token along with FHIR person/patient identifiers.

**Input parameters (passed as JSON argument):**

| Parameter | Required | Description |
|-----------|----------|-------------|
| `patientAge` | Yes | Age of the synthetic patient (drives cohort eligibility) |
| `gender` | Yes | `'male'` or `'female'` |

**Returns (as `user_token_response`):**

| Field | Description |
|-------|-------------|
| `accessToken` | JWT access token object |
| `clientPersonId` | Client-scoped FHIR Person ID |
| `clientPatientId` | Client-scoped FHIR Patient ID |
| `bwellPersonId` | bWell FHIR Person ID |
| `bwellPatientId` | bWell FHIR Patient ID |
| `managingOrganizationId` | Managing organization ID |

**Usage:**
```gherkin
* def user_token_response = call read('classpath:util/get-user-token.feature') { patientAge: 45, gender: 'female' }
* def clientPersonId = user_token_response.clientPersonId
* def patientId = user_token_response.clientPatientId
```

---

### `util/load-resource.feature`

POSTs a FHIR resource from a payload template in `src/test/java/payload/cql/` to the FHIR server, associating it with the current patient.

**Input parameters:**

| Parameter | Required | Description |
|-----------|----------|-------------|
| `filename` | Yes | Filename within `payload/cql/` (e.g., `'observation-mammography.json'`) |
| `resourceType` | Yes | FHIR resource type (e.g., `'Observation'`, `'Condition'`) |
| `date` | Yes | Date/datetime string to inject into the payload (typically a variable from `get-dates`) |

**Returns:** `resourceId` — the server-assigned ID of the created resource.

**Usage:**
```gherkin
* call read('classpath:util/load-resource.feature') { filename: 'observation-mammography.json', resourceType: 'Observation', date: #(yesterdayDateTime) }
* def observationId = resourceId
```

> **Note:** The `date` argument uses the Karate expression syntax `#(variableName)` to pass a variable by reference.

---

### `util/create-resource.feature`

Lower-level FHIR resource creation utility. POSTs an arbitrary `payload` object you supply directly.

**Input parameters:**

| Parameter | Required | Description |
|-----------|----------|-------------|
| `resourceType` | Yes | FHIR resource type |
| `payload` | Yes | Full JSON payload object |

**Returns:** `resourceId`

---

### `util/delete-resource.feature`

DELETEs a FHIR resource by ID.

**Input parameters:**

| Parameter | Required | Description |
|-----------|----------|-------------|
| `resourceId` | Yes | Server-assigned resource ID |
| `resourceType` | Yes | FHIR resource type |

**Usage:**
```gherkin
* call read('classpath:util/delete-resource.feature') { resourceId: #(observationId), resourceType: 'Observation' }
```

> **Cleanup convention:** The last scenario in each feature file is responsible for deleting all shared resources (loaded in `Background`) **and** the patient record itself (`resourceType: 'Patient'`, `resourceId: #(patientId)`).

---

### `util/execute-cql.feature`

Sends a POST request to the CQL Engine and makes the evaluation results available as `results`.

**Requires (in scope):** `cqlEngineUrl`, `serviceToken`, `libraryId`, `clientPersonId`, `patientId`

**Optional overrides:**

| Variable | Default | Description |
|----------|---------|-------------|
| `createResources` | `false` | Whether the CQL engine should create task resources |
| `maxRetries` | `4` | Retry count for the HTTP call |
| `retryInterval` | `1000` | Retry interval in ms |

**Returns:** `results` — the `evaluationResults` object from the CQL Engine response.

**Usage:**
```gherkin
* call read('classpath:util/execute-cql.feature')
And match results.InCohort_Out_Bool == true
And match results.Numerator == false
And match results.Completed_Out_Date == null
```

The request body is built from `payload/cqlrequest.json`:
```json
{
  "clientPersonId": "#(clientPersonId)",
  "libraryId": "#(libraryId)",
  "createResources": "#(createResources)",
  "refreshLibraryCache": true,
  "refreshClientPersonCache": true
}
```

---

### `util/cql-evaluation.feature`

Higher-level CQL evaluation wrapper with built-in logging. Used for scenarios that need more orchestration around the CQL call.

**Requires:** `cqlEngineUrl`, `serviceToken`, `libraryId`, `clientPersonId`, `patientId`

**Optional inputs:** `shouldCreateResources` (default `false`), `correlationId`

---

### `util/identifier-validators.feature`

Provides a reusable JavaScript function `validateIdentifier` for asserting FHIR identifier values.

**Usage:**
```gherkin
* def identifierUtils = call read('classpath:util/identifier-validators.feature')
* def validateIdentifier = identifierUtils.validateIdentifier
* call validateIdentifier(identifierArray, 'some-id', 'https://system.url', 'expected-value')
```

---

### `util/task-constants.feature`

Defines shared constants used by task-related tests.

**Variables exposed:**

```js
TASK_STATUSES  = { READY, COMPLETED, CANCELLED }
TASK_CODES     = { CARE_NEED, HEALTH_ACTIVITY }
IDENTIFIERS    = { ELIGIBILITY_SOURCE, ACTIVITY_TITLE, WORKFLOW_EVENT, CQL_ENGINE }
ACTIVITIES     = { ANNUAL_PHYSICAL }
```

---

### `util/task-setup.feature`

Bootstraps the full task-test environment in a single call: loads constants, obtains a service token, loads identifier validators, and exposes helper functions (`sleep`, `timestamp`).

---

### `util/task-search.feature` (GraphQL)

Searches for Tasks via the HP Facade GraphQL endpoint with retry-until logic.

**Input parameters:**

| Parameter | Default | Description |
|-----------|---------|-------------|
| `userToken` | required | Bearer token |
| `clientPatientId` | required | Patient ID |
| `status` | — | Task status filter |
| `payloadFile` | `payload/task/get-tasks-graphql.json` | GraphQL request payload |
| `activityTitle` | — | Optional filter |
| `activityType` | — | Optional filter |
| `eligibilitySource` | — | Optional filter |
| `expectedCount` | `1` | Minimum task count to satisfy retry |
| `expectEmpty` | `false` | When `true`, retries until result is empty |

**Returns:** `tasks` — array of task resources.

---

### `util/task-search-rest.feature` (REST)

Searches for Tasks via the FHIR REST endpoint with retry-until logic.

**Input parameters:**

| Parameter | Default | Description |
|-----------|---------|-------------|
| `userToken` | required | Bearer token |
| `clientPatientId` | required | Patient ID |
| `status` | — | Task status filter |
| `code` | — | Task code filter |
| `identifier` | — | FHIR identifier filter |
| `expectedCount` | `1` | Minimum entry count to satisfy retry |
| `expectEmpty` | `false` | When `true`, retries until result is empty |

**Returns:** `tasks` — array of FHIR Bundle entry objects.

---

### `util/consent-creation.feature`

Creates a Terms-of-Service consent via the consent GraphQL endpoint.

**Requires:** `userToken`, `apiGatewayUrl`

---

### `util/user-creation.feature`

Standalone user/patient creation utility (alternative to `get-user-token.feature`). Uses `NewUserPayloadGenerator` Java helper.

**Input parameters:** `patientAge`, `gender` (optional, default `'male'`)

**Returns:** `userToken`, `accessToken`, `clientPersonId`, `clientPatientId`, `bwellPersonId`, `bwellPatientId`

---

## Authoring CQL Evaluation Tests — Patterns & Conventions

### 1. Feature-level tags

Every CQL feature file carries two tags:

```gherkin
@e2e @cql
Feature: Evaluate <LibraryName>
```

Use `@e2e` to run all end-to-end tests; use `@cql` to run only CQL measure tests.

---

### 2. Standard Background block

Every CQL feature `Background` follows this exact sequence:

```gherkin
Background:
  # 1. Set the CQL library ID — must match the library name in the CQL Engine
  * def libraryId = 'YourLibraryId'

  # 2. Load date variables
  * call read('classpath:util/get-dates.feature')

  # 3. Get a service token
  * def auth_service_token_response = call read('classpath:util/get-token.feature')
  * def serviceToken = 'Bearer ' + auth_service_token_response.access_token

  # 4. Create a synthetic patient; specify age and gender to satisfy cohort criteria
  * def user_token_response = call read('classpath:util/get-user-token.feature') { patientAge: 45, gender: 'female' }

  # 5. Extract patient identifiers
  * def clientPersonId = user_token_response.clientPersonId
  * def patientId = user_token_response.clientPatientId

  # 6. (Optional) Load shared FHIR resources needed by all scenarios
  * call read('classpath:util/load-resource.feature') { filename: 'condition-diabetes.json', resourceType: 'Condition', date: #(yesterdayDateTime) }
  * def conditionId = resourceId
```

> **Note:** The `Background` runs before *every* scenario. Resources loaded here are shared across all scenarios. Use scenario-local `call load-resource` when a resource should only exist for that scenario.

---

### 3. Correlation IDs for traceability

Every scenario begins by generating a correlation ID and logging the start state:

```gherkin
Scenario: Care need is open
  * def correlationId = generateCorrelationId('LIBRARY_PREFIX')
  * print correlationId, '| Starting scenario: Care need is open | clientPersonId:', clientPersonId, '| patientId:', patientId
```

The `generateCorrelationId(prefix)` helper (defined in `karate-config.js`) returns `"PREFIX-<8-char-uuid>"`. Use a short, readable prefix (e.g., `'BCS1'`, `'KED1'`, `'STATIN_CVD1'`).

---

### 4. Loading FHIR test data

Use `load-resource.feature` to POST a FHIR resource and capture its ID:

```gherkin
* call read('classpath:util/load-resource.feature') { filename: 'observation-egfr.json', resourceType: 'Observation', date: #(yesterdayDateTime) }
* def observationId = resourceId
* print correlationId, '| Loaded Observation | observationId:', observationId, '| date:', yesterdayDateTime
```

- **`filename`** — relative to `src/test/java/payload/cql/`
- **`date`** — use the `#(variable)` expression syntax to pass a pre-calculated date from `get-dates`
- Always assign `resourceId` to a uniquely named variable immediately after the call so it can be cleaned up later

---

### 5. Executing CQL and asserting results

After loading all required FHIR resources, invoke the CQL engine and assert the response fields:

```gherkin
* call read('classpath:util/execute-cql.feature')
And match results.InCohort_Out_Bool == true
And match results.Numerator == false
And match results.Completed_Out_Date == null
* print correlationId, '| Results | InCohort:', results.InCohort_Out_Bool, '| Numerator:', results.Numerator
```

**Common result fields:**

| Field | Type | Description |
|-------|------|-------------|
| `InCohort_Out_Bool` | `Boolean` | Whether the patient is in the measure cohort |
| `Numerator` | `Boolean` | Whether the numerator condition is met |
| `Numerator_Output_MR` | `Integer` | Count of numerator-qualifying records |
| `Completed_Out_Bool` | `Boolean` | Whether the care need is completed |
| `Completed_Out_Date` | `String \| null` | Completion date (`yyyy-MM-dd`) or `null` |
| `Exclusions` | `Boolean` | Whether an optional exclusion is present |
| `RequiredExclusions_Output_MR` | `Integer` | Count of required exclusion records |
| `HasHospice` | `Boolean` | Hospice exclusion flag (measure-specific) |
| `HasExclusion` | `Boolean` | Generic exclusion flag (measure-specific) |
| `TotalRiskFactor` | `Integer` | Count of risk factors (measure-specific) |
| `InAgeCohort` | `Boolean` | Age-specific cohort flag (measure-specific) |

Use `contains` for partial date matches when only the date portion (without timezone) needs to be asserted:

```gherkin
And match results.Completed_Out_Date contains threeDaysAgoNoTimezone
```

---

### 6. Scenario coverage pattern

Every CQL feature covers three scenario types in order:

| Scenario | Setup | Expected outcome |
|----------|-------|-----------------|
| **Care need is open** | Patient meets cohort criteria but no numerator data | `InCohort == true`, `Numerator == false`, `Completed_Out_Date == null` |
| **Care need is closed** | All numerator-satisfying FHIR resources present | `InCohort == true`, `Numerator == true`, `Completed_Out_Date` set |
| **Exclusion** | Exclusion-triggering resource present | `InCohort == false` or exclusion flag `== true` |

---

### 7. Resource cleanup

Clean up all FHIR resources created during a scenario immediately after assertions, and log each deletion:

```gherkin
* call read('classpath:util/delete-resource.feature') { resourceId: #(observationId), resourceType: 'Observation' }
* print correlationId, '| Cleaned up | observationId:', observationId
```

**The last scenario in every feature file is responsible for final full cleanup:**

```gherkin
# Remove all shared Background resources and the patient itself
* call read('classpath:util/delete-resource.feature') { resourceId: #(conditionId), resourceType: 'Condition' }
* call read('classpath:util/delete-resource.feature') { resourceId: #(patientId), resourceType: 'Patient' }
* print correlationId, '| ✅ Scenario completed with full cleanup'
```

Mark the final scenario's closing print with the `✅ Scenario completed with full cleanup` convention to make it easy to identify in logs.

---

### 8. Scenario completion logging

End every scenario with a completion log line:

```gherkin
* print correlationId, '| ✅ Scenario completed'
```

For the final scenario (with full patient cleanup):

```gherkin
* print correlationId, '| ✅ Scenario completed with full cleanup'
```

---

### 9. Overriding patient demographics mid-scenario

When a scenario requires a different patient than the one created in `Background` (e.g., testing an age-based exclusion), override `clientPersonId` and `patientId` locally:

```gherkin
Scenario: Exclusion - under age 18
  * def correlationId = generateCorrelationId('METAB_SYN')
  * def user_token_response_minor = call read('classpath:util/get-user-token.feature') { patientAge: 16, gender: 'male' }
  * def clientPersonId = user_token_response_minor.clientPersonId
  * def patientId = user_token_response_minor.clientPatientId
```

This creates a second patient scoped to that scenario. Remember to delete this patient during cleanup.

---

## Payload Files

FHIR resource templates live in `src/test/java/payload/cql/`. Each file is a FHIR resource JSON with Karate expression placeholders:

- **`subject.reference`** is automatically injected by `load-resource.feature` as `"Patient/<patientId>"`
- The `date` field in the payload is replaced by the `date` argument passed to `load-resource.feature`

Available payload files:

| File | Resource Type | Description |
|------|---------------|-------------|
| `condition-diabetes.json` | Condition | Diabetes diagnosis |
| `condition-esrd.json` | Condition | End-stage renal disease |
| `condition-primary-htn.json` | Condition | Primary hypertension |
| `condition-secondary-htn.json` | Condition | Secondary hypertension |
| `encounter-er.json` | Encounter | Emergency room encounter |
| `medicationrequest-statin.json` | MedicationRequest | Statin medication request |
| `observation-a1c.json` | Observation | HbA1c lab result |
| `observation-bp-controlled-code.json` | Observation | Controlled BP (code-based) |
| `observation-bp-controlled-value.json` | Observation | Controlled BP (value-based) |
| `observation-bp-uncontrolled.json` | Observation | Uncontrolled BP |
| `observation-bp.json` | Observation | Generic BP |
| `observation-cancer.json` | Observation | Cancer diagnosis |
| `observation-cirrhosis.json` | Observation | Cirrhosis |
| `observation-egfr.json` | Observation | eGFR kidney function |
| `observation-fbg-high.json` | Observation | High fasting blood glucose |
| `observation-hdl-low.json` | Observation | Low HDL cholesterol |
| `observation-mammectomy.json` | Observation | Mastectomy (BCS exclusion) |
| `observation-mammography.json` | Observation | Mammography screening |
| `observation-mi.json` | Observation | Myocardial infarction |
| `observation-obesity.json` | Observation | Obesity/BMI |
| `observation-prostate-dysplasia.json` | Observation | Prostate dysplasia |
| `observation-psa-abnormal.json` | Observation | Abnormal PSA |
| `observation-psa-value.json` | Observation | PSA value |
| `observation-qua.json` | Observation | QUA observation |
| `observation-retinal-exam.json` | Observation | Retinal exam |
| `observation-triglycerides.json` | Observation | Triglycerides |
| `observation-uacr.json` | Observation | Urine albumin-to-creatinine ratio |
| `observation-uc.json` | Observation | Ulcerative colitis |
| `observation-waist-high.json` | Observation | High waist circumference |
| `procedure-hospice.json` | Procedure | Hospice care (common exclusion) |
| `procedure-prostectomy.json` | Procedure | Prostatectomy |

---

## Executing Tests

### Prerequisites

- **Java 17** — the Gradle 8.0 wrapper does not support Java 21+ (fails with "Unsupported class file major version"). If your default JVM is newer, set `JAVA_HOME` explicitly:
  ```bash
  export JAVA_HOME=$(/usr/libexec/java_home -v 17)
  ```
- Gradle (use the included `./gradlew` wrapper — do not use a system-installed Gradle)
- Valid `client.id`, `client.secret`, and `client.key` (see [Required Credentials](#required-credentials))

---

### Run all tests (default env: `dev`)

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew test \
  -Dkarate.env=dev \
  -Dclient.id=YOUR_CLIENT_ID \
  -Dclient.key=YOUR_CLIENT_KEY \
  -Dclient.secret=YOUR_CLIENT_SECRET
```

Tests run in parallel with 3 threads (configured in `KarateTestRunner.java`).

---

### Run all tests against a specific environment

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew test \
  -Dkarate.env=staging \
  -Dclient.id=YOUR_CLIENT_ID \
  -Dclient.key=YOUR_CLIENT_KEY \
  -Dclient.secret=YOUR_CLIENT_SECRET
```

Supported values for `-Dkarate.env`: `dev`, `staging`, `client-sandbox`, `prod`

---

### Run a single feature file

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew clean test \
  -Dkarate.options="classpath:feature/cql/bcs1-evaluation.feature" \
  -Dkarate.env=dev \
  -Dclient.id=YOUR_CLIENT_ID \
  -Dclient.key=YOUR_CLIENT_KEY \
  -Dclient.secret=YOUR_CLIENT_SECRET
```

---

### Run by tag

Filter by a Karate tag using `--tags`:

```bash
# Run all CQL measure tests
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew clean test \
  -Dkarate.options="--tags @cql" \
  -Dkarate.env=dev \
  -Dclient.id=YOUR_CLIENT_ID \
  -Dclient.key=YOUR_CLIENT_KEY \
  -Dclient.secret=YOUR_CLIENT_SECRET

# Run all end-to-end tests
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew clean test \
  -Dkarate.options="--tags @e2e" \
  -Dkarate.env=dev \
  -Dclient.id=YOUR_CLIENT_ID \
  -Dclient.key=YOUR_CLIENT_KEY \
  -Dclient.secret=YOUR_CLIENT_SECRET
```

---

### Clean build before running

Use `clean` to avoid Gradle's up-to-date checks (the `build.gradle` already sets `outputs.upToDateWhen { false }`, but `clean` is recommended for CI):

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew clean test ...
```

---

## Test Reports

After a test run, HTML and JUnit XML reports are generated at:

```
build/reports/tests/test/         # Gradle HTML report
build/karate-reports/             # Karate HTML report (karate-summary.html)
build/surefire-reports/           # JUnit XML reports
```

Open the Karate summary report:

```bash
open build/karate-reports/karate-summary.html