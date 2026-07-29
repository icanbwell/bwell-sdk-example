---
name: insights-local-testing
description: Run the insights pipeline services locally (cql-engine, cohort-qualification, task-service, workflow-event-service, hp-notification-service) individually or together to test CQL evaluation, cohort qualification, task creation, and notification flows.
argument-hint: "<service(s)> — e.g. 'cql-engine', 'all', 'cql-engine + cohort', 'notification only'"
disable-model-invocation: false
allowed-tools: Bash, Read, Write, Agent
---

# Insights Pipeline Local Testing

Run the eligibility pipeline services locally to test CQL evaluation, cohort qualification, task creation, workflow triggers, and notification delivery. Unlike the DQM pipeline (which uses the orchestrator), this pipeline is event-driven through Kafka topics connecting individual services.

## When to Use

- Testing cql-engine changes (e.g., whale detection, new evaluation logic, bundle filtering)
- Testing cohort-qualification changes (membership evaluation, measure report creation)
- Testing task-service changes (task CRUD, enrichment, search)
- Testing workflow-event-service changes (event routing, data connection flows)
- Testing hp-notification-service changes (notification triggers, delivery)
- Validating cross-service event flows end-to-end locally

## Invocation

When this skill is invoked, first run the **Changeset-Aware Testing** analysis below. Based on the git diff and test analysis, determine which services are needed and present the test plan for confirmation.

If no local changes are detected (clean working tree), fall back to asking the user which services they need to run. Not all services are required for every test — the user should only start what they need.

Parse `$ARGUMENTS` to determine which services:
- `cql-engine` / `cql` / `bsights` — CQL evaluation engine only
- `cohort` / `cohort-qualification` / `qualification` — Cohort qualification only
- `task` / `task-service` — Task service only
- `workflow` / `wes` / `workflow-event` — Workflow event service only
- `notification` / `notif` / `hp-notification` — Notification service only
- `all` — All 5 services
- Combinations: `cql + cohort`, `cql-engine and task-service`, etc.

## Service Registry

| Service | Dir Name | Port | App Name | External Deps | Docker Deps |
|---------|----------|------|----------|---------------|-------------|
| CQL Engine | `bsights-engine-cql` | 8013 | `cql-engine` | FHIR, MongoDB, Auth | Kafka |
| Cohort Qualification | `cohort-qualification-service` | 8014 | `cohort-qualification` | FHIR, Auth | Kafka |
| Task Service | `task-service` | 8011 | `task-service` | FHIR, Auth | Kafka (shared) |
| Workflow Event | `workflow-event-service` | 8022 | `workflow-event-service` | FHIR, Auth | Kafka |
| HP Notification | `hp-notification-service` | 8016 | `hp-notification-service` | FHIR, MongoDB, Auth | Kafka |

All services live under a single source root. Set it once per shell (default assumes `~/src`):

```bash
export INSIGHTS_SRC_ROOT="${INSIGHTS_SRC_ROOT:-$HOME/src}"
```

Each service is then at `$INSIGHTS_SRC_ROOT/{dir-name}`. If your repos live elsewhere, export `INSIGHTS_SRC_ROOT` to that path before running the commands below.

## Event Flow Architecture

```
                          eligibility.evaluation.commands
[External Trigger] ─────────────────────────────────────────────→ [CQL Engine :8013]
                                                                         │
                                                                         │ CqlLibraryEvaluated
                                                                         ▼
                                                           ┌─────────────────────────────┐
                                                           │ health_programs.activity.    │
                                                           │ events.CqlLibraryEvaluated   │
                                                           └─────────────┬───────────────┘
                                                                         │
                            ┌────────────────────────────────────────────┼────────────────┐
                            │                                            │                │
                            ▼                                            ▼                ▼
               [Cohort Qualification :8014]              [Workflow Event :8022]    [Task Service :8011]
                            │                              (business.events)        (via workflow)
                            │ CohortMembershipEvaluated
                            ▼
              ┌──────────────────────────────┐
              │ health_programs.activity.     │
              │ events.CohortMembershipEval  │
              └──────────────┬───────────────┘
                             │
                             ▼
              [Workflow Event :8022] ──→ [Task Service :8011] ──→ [HP Notification :8016]
```

**Key Topics:**
- `eligibility.evaluation.commands` — Triggers CQL evaluation (consumed by cql-engine)
- `large.eligibility.evaluation.commands` — Whale-routed evaluations (same consumer, different topic)
- `health_programs.activity.events.CqlLibraryEvaluated` — CQL results (produced by cql-engine)
- `health_programs.activity.events.CqlLibraryEvaluatedScheduledBatch` — Batch CQL results
- `health_programs.activity.events.CohortMembershipEvaluated` — Cohort results (produced by cohort-qualification)
- `business.events` — General business events (consumed by workflow-event-service)
- `access_to_health_data.connections.events` — Data connection events

## Changeset-Aware Testing

Every time this skill is invoked, automatically analyze the local changeset to generate targeted test scenarios. This ensures local testing exercises the specific code paths that changed, not just generic happy-path flows.

### Step 1: Analyze the changeset

Run these in parallel to understand what changed:

```bash
# What files changed (unstaged + staged + untracked)
git status
git diff --name-only
git diff --cached --name-only

# Full diff for understanding the nature of changes
git diff
git diff --cached
```

### Step 2: Analyze related tests

For each changed source file, find its corresponding tests:
- Unit tests: `src/test/java/.../<ClassName>Test.java`
- Integration tests: `src/itest/java/.../<ClassName>IT.java` or files in `src/itest/`
- Docker-compose test configs: `src/itest/resources/docker-compose-test.yml`

Read the test files to understand:
- What scenarios/edge cases are being validated
- What assertions define "correct" behavior
- What test data patterns are used (inputs, expected outputs)
- What Kafka messages or REST calls the itests exercise

### Step 3: Generate a test plan

Based on the changeset analysis, determine:
1. **Which services need to run** — only start services relevant to the changed code paths
2. **What scenarios to exercise** — derive from the code changes + test assertions
3. **What to monitor** — specific log patterns, Kafka messages, or state changes that prove the change works
4. **Expected outcomes** — what success looks like for each scenario

### Step 4: Present the plan for confirmation

Before executing, present a clear summary to the developer:

```
## Local Test Plan

### Changes detected:
- [file1.java]: <brief description of what changed>
- [file2.java]: <brief description of what changed>

### Services to start:
- CQL Engine (needed because: ...)
- Cohort Qualification (needed because: ...)

### Test scenarios:
1. <Scenario name> — Tests <what>, expects <outcome>
   Why: validates <specific code path from the changeset>
2. <Scenario name> — Tests <what>, expects <outcome>
   Why: validates <specific code path from the changeset>

### What I'll monitor:
- Logs: <specific patterns>
- Kafka topics: <expected messages>

Does this look right? Should I add/remove any scenarios?
```

Wait for the developer to confirm or adjust before proceeding.

### Step 5: Execute and validate

Run the test plan:
1. Start infrastructure + services (only what's needed)
2. Execute each scenario (produce Kafka messages, trigger REST calls)
3. Capture evidence (log snippets, Kafka consumer output, curl responses)
4. Compare actual outcomes to expected outcomes

### Step 6: Produce a validation summary

After all scenarios complete, produce a summary:

```
## Validation Summary

### Changeset: <branch-name> (<N files changed>)

### Results:
| # | Scenario | Status | Evidence |
|---|----------|--------|----------|
| 1 | <name>   | PASS   | <brief proof> |
| 2 | <name>   | FAIL   | <what went wrong> |

### What this validates:
- <Specific assertion about the PR>
- <Another assertion>

### What this does NOT validate (out of scope for local testing):
- <e.g., "Concurrent evaluation ordering — requires multi-partition Kafka">
```

This summary is intended to be copy-pasteable into a PR description or Slack message as evidence of local validation.

---

## Prerequisites

- Docker Desktop running
- Java 21+ (`java -version`)
- Dev environment credentials configured in `application-local.yaml` for each service
- All services connect to **dev FHIR** (`fhir.dev.bwell.zone`) and **dev Auth** (Cognito) when running locally

## Infrastructure Setup

### Step 1: Start Kafka

All 5 services share a single local Kafka broker. Use ANY service's docker-compose to start it (they all define the same `local-kafka` container):

```bash
# Pick whichever service you're testing — they all start the same Kafka
cd "$INSIGHTS_SRC_ROOT/bsights-engine-cql"
docker compose -f docker-compose-local.yml up -d
```

### Step 2: Verify Kafka is running

```bash
docker exec local-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list 2>/dev/null && echo "Kafka ready"
```

If you get "Error: No such container: local-kafka", the container name might differ. Check:
```bash
docker ps --format "{{.Names}} {{.Ports}}" | grep 9092
```

### Step 3: Check for port conflicts

```bash
for port in 8011 8013 8014 8016 8022; do
  pid=$(lsof -ti :$port 2>/dev/null)
  if [ -n "$pid" ]; then
    echo "Port $port in use by PID $pid"
  fi
done
```

## Starting Services

### CQL Engine (bsights-engine-cql)

```bash
cd "$INSIGHTS_SRC_ROOT/bsights-engine-cql"
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Verify: `curl -sf http://localhost:8013/actuator/health | python3 -m json.tool`

**Notes:**
- Connects to dev MongoDB for person execution context cache
- Connects to dev FHIR for person bundle retrieval
- Produces to `health_programs.activity.events.CqlLibraryEvaluated`
- Consumes from `eligibility.evaluation.commands` and `large.eligibility.evaluation.commands`

### Cohort Qualification

```bash
cd "$INSIGHTS_SRC_ROOT/cohort-qualification-service"
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Verify: `curl -sf http://localhost:8014/actuator/health | python3 -m json.tool`

**Notes:**
- Consumes from `health_programs.activity.events.CqlLibraryEvaluated`
- Produces to `health_programs.activity.events.CohortMembershipEvaluated`
- Connects to dev FHIR for MeasureReport creation

### Task Service

```bash
cd "$INSIGHTS_SRC_ROOT/task-service"
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Verify: `curl -sf http://localhost:8011/actuator/health | python3 -m json.tool`

**Notes:**
- No docker-compose-local.yml — relies on Kafka started by another service
- Connects to dev FHIR for task CRUD
- Consumes various workflow events

### Workflow Event Service

```bash
cd "$INSIGHTS_SRC_ROOT/workflow-event-service"
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Verify: `curl -sf http://localhost:8022/actuator/health | python3 -m json.tool`

**Notes:**
- Consumes from `business.events`, data connection topics
- Connects to dev FHIR for workflow triggers
- Coordinates between CQL evaluation results and task/notification creation

### HP Notification Service

```bash
cd "$INSIGHTS_SRC_ROOT/hp-notification-service"
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Verify: `curl -sf http://localhost:8016/actuator/health | python3 -m json.tool`

**Notes:**
- Connects to dev MongoDB for notification state/dedup
- Consumes task events and data connection events
- Triggers notifications (email via SendGrid, push via Firebase)

## Running in Background

```bash
# Start any service in background with log capture
cd "$INSIGHTS_SRC_ROOT/{dir-name}"
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun > /tmp/{service-name}.log 2>&1 &
echo "PID: $!"
```

Monitor:
```bash
tail -f /tmp/{service-name}.log | grep -E "ERROR|WARN|Started|Listening"
```

## Triggering CQL Evaluation Locally

To test the cql-engine specifically, produce a message to the evaluation commands topic:

### Option A: Via kafka-python (recommended — handles CloudEvents binary format correctly)

The cql-engine consumers use CloudEvents **binary content mode** (CE attributes as Kafka headers, data as the value body). `kafka-console-producer` can't reliably produce this format. Use kafka-python:

```python
"$INSIGHTS_SRC_ROOT/python_scripts/venv/bin/python3" << 'EOF'
import signal, json, uuid
from datetime import datetime, timezone
from kafka import KafkaProducer

signal.signal(signal.SIGALRM, lambda s, f: (_ for _ in ()).throw(SystemExit(1)))
signal.alarm(30)

producer = KafkaProducer(
    bootstrap_servers=['localhost:9092'],
    api_version=(2, 8, 0),
    request_timeout_ms=10000,
    value_serializer=lambda v: json.dumps(v).encode('utf-8'),
    key_serializer=lambda k: k.encode('utf-8') if k else None,
)

try:
    data = {
        "clientFhirPersonId": "<PERSON_ID>",
        "libraryId": "BCS1"
    }

    headers = [
        ('ce_specversion', b'1.0'),
        ('ce_id', str(uuid.uuid4()).encode()),
        ('ce_type', b'RequestEvaluation'),
        ('ce_source', b'local-test'),
        ('ce_time', datetime.now(timezone.utc).isoformat().encode()),
        ('content-type', b'application/json'),
    ]

    future = producer.send(
        'eligibility.evaluation.commands',
        key='<PERSON_ID>',
        value=data,
        headers=headers
    )
    result = future.get(timeout=10)
    print(f"Produced to partition {result.partition} offset {result.offset}")
    producer.flush()
finally:
    producer.close()
    signal.alarm(0)
EOF
```

### Option B: Via REST endpoint (if service exposes one)

Check the service's Swagger/AsyncAPI docs:
```bash
curl -sf http://localhost:8013/swagger-ui.html 2>/dev/null
```

### Option C: Via the existing evaluation command topic (dev-pointed)

If cql-engine is running locally pointing to dev FHIR and dev Kafka, you can trigger evaluations from the dev environment by producing to the local Kafka topic. The service will fetch person data from dev FHIR.

## Common Test Scenarios

### Testing CQL Engine Only (e.g., whale detection)

1. Start Kafka + cql-engine
2. Produce an evaluation command with a known person ID
3. Watch logs for whale detection routing:
   ```bash
   tail -f /tmp/cql-engine.log | grep -E "Whale|large.eligibility|WhaleDetected"
   ```

### Testing CQL → Cohort Flow

1. Start Kafka + cql-engine + cohort-qualification
2. Trigger an evaluation command
3. Watch cohort-qualification consume the CqlLibraryEvaluated event:
   ```bash
   tail -f /tmp/cohort-qualification.log | grep -E "CqlLibraryEvaluated|CohortMembership|InCohort"
   ```

### Testing Full Pipeline

1. Start all 5 services
2. Trigger an evaluation command
3. Follow events through the chain:
   - cql-engine evaluates → produces CqlLibraryEvaluated
   - cohort-qualification consumes → produces CohortMembershipEvaluated
   - workflow-event-service routes events
   - task-service creates/updates tasks
   - hp-notification-service triggers notifications

## Teardown

### Stop specific services
```bash
lsof -i :8013 -t | xargs kill -9 2>/dev/null  # cql-engine
lsof -i :8014 -t | xargs kill -9 2>/dev/null  # cohort-qualification
lsof -i :8011 -t | xargs kill -9 2>/dev/null  # task-service
lsof -i :8022 -t | xargs kill -9 2>/dev/null  # workflow-event-service
lsof -i :8016 -t | xargs kill -9 2>/dev/null  # hp-notification-service
```

### Stop all services + Kafka
```bash
for port in 8011 8013 8014 8016 8022; do
  lsof -i :$port -t 2>/dev/null | xargs kill -9 2>/dev/null
done
docker stop local-kafka 2>/dev/null
```

### Full cleanup (including Kafka volumes)
```bash
cd "$INSIGHTS_SRC_ROOT/bsights-engine-cql"
docker compose -f docker-compose-local.yml down -v
```

### Kill Gradle daemons (if needed)
```bash
pkill -f "GradleDaemon"
```

## Critical Local Dev Tips

### Kafka broker config
The `docker-compose-local.yml` in `bsights-engine-cql` uses a simplified single-listener config with `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1` and `KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1`. These are **required** for consumer groups to work on a single-broker setup. Without them, the `__consumer_offsets` internal topic can't be created and consumers will hang in `ensureCoordinatorReady` forever.

### Broker address
Only use `localhost:9092` in `application-local.yaml`. The `kafka:29092` Docker-internal hostname is NOT resolvable from the host and causes consumer coordinator failures. The current `application-local.yaml` already has `brokers: "localhost:9092"`.

### Consumer concurrency for local testing
The cql-engine defaults to 20 concurrent consumers. With only 1 local Kafka partition, this causes rebalance storms. Override with:
```bash
evaluation_request_command_concurrency=1 large_evaluation_request_command_concurrency=1 SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```
Note: The large evaluation consumer concurrency var is `large_evaluation_request_command_concurrency` (NOT `large_evaluation_command_concurrency`).

### Whale detection testing
To test whale detection locally, set the blocklist env var:
```bash
whale_detection_person_blocklist="whale-person-123,whale-person-456" evaluation_request_command_concurrency=1 SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```
Then produce a message with `clientFhirPersonId` matching a blocklist entry and watch for: `Whale detected — routing to large evaluation topic`

### Progressive lookback testing (INE-749)
Test the progressive reduction from 3yr→2yr→1yr on the large evaluation consumer:
1. Create temporal test data using the `whale-test-data` skill (scenario: `progressive`)
2. Start cql-engine with low threshold:
```bash
source ~/.dqm-local-env.sh && export auth_client_id auth_client_secret && \
  large_evaluation_max_evaluable_resource_count=300 \
  large_evaluation_lookback_filter_enabled=true \
  large_evaluation_lookback_filter_steps=3,2,1 \
  whale_detection_resource_threshold=100 \
  evaluation_request_command_concurrency=1 \
  large_evaluation_request_command_concurrency=1 \
  SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```
3. Produce to `large.eligibility.evaluation.commands` (not the main topic)
4. Watch logs for: `Progressive lookback`, `step_used`, filter reduction counts
5. Verify: CqlLibraryEvaluated event produced to Kafka

### Auth credential export
When running bootRun locally, `source ~/.dqm-local-env.sh` alone does NOT make `auth_client_id`/`auth_client_secret` available to the Gradle process. You must also export them:
```bash
source ~/.dqm-local-env.sh && export auth_client_id auth_client_secret
```
Or pass directly on the command line as env var assignments.

### Zombie consumer sessions after unclean shutdown
If bootRun is killed (Ctrl+C, OOM, `kill -9`), consumer sessions stay registered in Kafka for `session.timeout.ms` (default 5 min on local). This causes "N consumers for 1 partition" rebalance issues. Fix:
```bash
docker compose -f docker-compose-local.yml down -v && docker compose -f docker-compose-local.yml up -d
```
This fully resets Kafka state including consumer groups.

## Troubleshooting

### Service fails to start — "Address already in use"
```bash
lsof -i :{port} | grep LISTEN
# Kill the conflicting process
```

### Service fails to start — Kafka connection refused
Kafka container not running or not ready yet. Check:
```bash
docker ps | grep kafka
docker exec local-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

### Service fails to start — Auth/FHIR connection failures
Services connect to dev environment externally. Check:
- VPN connected (if required for dev access)
- `application-local.yaml` has correct auth credentials
- Dev Cognito token endpoint is reachable:
  ```bash
  curl -sf "https://bwell-dev.auth.us-east-1.amazoncognito.com/.well-known/openid-configuration" | python3 -m json.tool
  ```

### MongoDB connection failures (cql-engine, hp-notification)
These services use dev MongoDB. The `application-local.yaml` must have a valid `mongodb.uri`. If it's empty or placeholder, you need to set it to the dev cluster connection string.

### Messages not flowing between services
Check that topics exist and services are consuming:
```bash
# List all topics
docker exec local-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list

# Check consumer groups
docker exec local-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list

# Check lag for a specific group
docker exec local-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --describe --group cql-engine-request-evaluation
```

### "Whale detected" but no message on large topic
Verify the outbound producer is configured correctly. Check:
```bash
docker exec local-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list | grep large
```
If topic doesn't exist, Kafka auto-creation should handle it, but you can create manually:
```bash
docker exec local-kafka kafka-topics.sh --bootstrap-server localhost:9092 \
  --create --topic large.eligibility.evaluation.commands --partitions 10 --replication-factor 1
```

## Key Configuration

| Service | Port | Profile | FHIR URL | Auth URL | Database |
|---------|------|---------|----------|----------|----------|
| CQL Engine | 8013 | local | fhir.dev.bwell.zone | bwell-dev Cognito | Dev MongoDB |
| Cohort Qualification | 8014 | local | fhir.dev.bwell.zone | bwell-dev Cognito | FHIR only |
| Task Service | 8011 | local | fhir.dev.bwell.zone | bwell-dev Cognito | FHIR only |
| Workflow Event | 8022 | local | fhir.dev.bwell.zone | bwell-dev Cognito | FHIR only |
| HP Notification | 8016 | local | fhir.dev.bwell.zone | bwell-dev Cognito | Dev MongoDB |

| Infrastructure | Port | Container Name |
|----------------|------|----------------|
| Kafka | 9092 | local-kafka |

## Related Skills

- `java-local-dev` — Single-service local dev (build, test, start)
- `dqm-local-testing` — DQM pipeline (orchestrator, bundler, evaluator, normalizer)
- `kafka` — Interact with deployed MSK clusters for troubleshooting
- `whale-test-data` — Generate whale-sized patients with temporal bands for progressive lookback testing
