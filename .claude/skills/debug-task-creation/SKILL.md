---
description: Debug the task creation pipeline for a patient. Traces consent events, workflow-event-service processing, Kafka task.commands publishing, FHIR task creation, GetTask queries, and StarterActivityCreationService behavior. Produces a structured timeline report. Use when investigating why tasks weren't created, consent events weren't processed, starter tasks didn't fire, or to understand the task creation race condition for a specific patient.
argument-hint: <personId> <patientId> [environment] [timeRange]
---

# Debug Task Creation Pipeline

You are investigating the task creation pipeline for a specific patient. Your goal is to trace the full lifecycle from consent event through workflow-event-service processing, task creation, and GetTask queries, and produce a clear diagnostic report.

## Input Parameters

Parse from $ARGUMENTS:
- **clientFhirPersonId** (required): The FHIR Person ID
- **clientFhirPatientId** (required): The FHIR Patient ID
- **environment** (optional, default: "sandbox"): "sandbox", "dev", "staging", or "prod"/"production"
- **timeRange** (optional, default: "10m"): How far back to search (e.g., "10m", "30m", "1h")

Example invocations:
- `/debug-task-creation 4f167904-43bc-4b65-a9a2-3f337b18bac6 511ea897-9967-4fd0-9945-ffa8c29b94c9`
- `/debug-task-creation 56b3dda7-f806-4da4-9c2d-e4819265f866 25b442ba-ce77-417f-8b9c-61bdb86355e8 prod 30m`

## Environment Mapping

Map environment to the correct groundcover namespace and filter fields:
- **sandbox** → namespace: `task-service-client-sandbox`, env: `client-sandbox`
- **dev** → namespace: `task-service-dev`, env: `dev`
- **staging** → namespace: `task-service-staging`, env: `staging`
- **prod/production** → namespace: `task-service-prod`, env: `production`

**IMPORTANT**: In production, the actual namespace suffix is `prod`, NOT `production`. For example:
- `task-service-prod` (NOT `task-service-production`)
- `bsights-engine-cql-prod` (NOT `bsights-engine-cql-production`)
- `workflow-event-service-prod`
- `hp-facade-prod`

The namespace pattern is `{service}-{env-suffix}` where env-suffix values are:
- `client-sandbox` (sandbox)
- `dev` (dev)
- `staging` (staging)
- `prod` (prod/production)

**IMPORTANT**: The `k8s.namespace.name` field is often empty in groundcover. Use `namespace:` (which maps to pod-level namespace) or `service.name:` + `env:` for reliable filtering.

## Architecture: Two Task Creation Paths

### 1. Event-Driven Flow (Push-Based, Primary)
- **Trigger**: Consent event on Kafka topic `user_profile.consent_management.events` (published by consent-service when patient grants consent during registration/onboarding)
- **Chain**: consent-service → `user_profile.consent_management.events` topic → workflow-event-service → fetches Person/$graph from `fhir-pipeline.{env}.bwell.zone` → publishes `TaskCreateRequest` to `task.commands` Kafka topic → task-service consumes and creates task
- **Failure mode**: If workflow-event-service gets 504 from fhir-pipeline, the event is NOT emitted
- **Key file**: `TaskRequestEventConsumer.java`

### 2. Starter Activity Flow (Pull-Based, Fallback)
- **Trigger**: Client app performs a Task search via hp-facade
- **Chain**: Client → hp-facade → task-service (`StarterActivityCreationService`) → FHIR server
- **Logic**: On first Task search for a patient with no existing tasks, auto-creates tasks from ActivityDefinitions marked as starter activities
- **Key file**: `StarterActivityCreationService.java`
- **Guards**:
  - Query params must match `starterTriggerFilter` (org-specific or default)
  - No existing tasks for the patient (checks via `_count=1` search)
  - Race condition detection: if tasks appear during creation, deletes starters via async Kafka event

### Request Chain
```
Client App → hp-facade (task-service.{env}.bwell.zone) → task-service → FHIR server
```

## Investigation Steps

### Step 0: Confirm Task Existence in FHIR

Before debugging creation, verify whether the task actually exists:
```
GET {{FHIRServerURL}}/Task?patient=Patient/{{patientId}}&status=ready&code=care-need
```

If the task EXISTS in FHIR but not visible in the app → the issue is in the **read path** (hp-facade caching, query param mismatch, timing). Skip to Step 5.

If the task does NOT exist → continue with creation path debugging.

### Step 1: Search for Patient Activity in Task Service

Query groundcover logs using **MDC field filters** (NOT `content:` filter):

```gcql
* | filter namespace:task-service-{env} mdc.clientFhirPatientId:{patientId} | sort by (_time desc) | limit 50
```

If no results with patientId, try personId:
```gcql
* | filter namespace:task-service-{env} mdc.clientFhirPersonId:{personId} | sort by (_time desc) | limit 50
```

**CRITICAL**: The `content:` filter does NOT match structured JSON body fields. Always use specific MDC field names for reliable filtering.

### Step 2: Trace Event-Driven Task Creation (Kafka Flow)

Look for TaskCreateRequest consumption events:
```gcql
* | filter namespace:task-service-{env} content:*TaskCreateRequest* content:*{patientId}* | sort by (_time desc) | limit 20
```

Also search for the actual FHIR Task creation:
```gcql
* | filter namespace:task-service-{env} content:*Creating Task* content:*{patientId}* | sort by (_time desc) | limit 20
```

**Important**: Prod runs at INFO level. Successful task creation may only log at DEBUG level, so **absence of logs does NOT mean the event wasn't processed**. If no logs found, check traces (Step 4) before concluding events weren't received.

### Step 3: Trace Consent Event (Pipeline Entry Point)

The consent event is the **entry point** for the entire task creation pipeline. When a patient grants consent via `consent-service`, it publishes to the `user_profile.consent_management.events` Kafka topic, which triggers `workflow-event-service`.

Search for workflow-event-service consuming the consent event for this patient using traces:
```gcql
* | filter namespace:{workflow-event-service-namespace} trace_id:{traceId} | sort by (_time) | limit 20
```

Or find the consent consumption directly:
```gcql
* | filter namespace:{workflow-event-service-namespace} resource:*consent_management* | sort by (_time desc) | limit 10
```

Look for these spans in the workflow-event-service trace:
1. **Kafka consumer span**: `resource_name: "Topic: user_profile.consent_management.events, Partition: N"` — confirms the consent event was received
2. **POST Person/$graph**: workflow-event-service resolving patient relationships via fhir-pipeline
3. **GET /4_0_0/Person/{personId}**: fetching the Person record

The thread name on these spans will be a Kafka consumer thread (e.g., `org.springframework.kafka.KafkaListenerEndpointContainer#N-N-C-1`).

If the consent event is NOT found:
- Check if consent-service published the event (look in consent-service logs)
- Check for Kafka consumer lag on the `user_profile.consent_management.events` topic

### Step 4: Check workflow-event-service for 504 Errors

Look for 504 errors from fhir-pipeline that block the event-driven path:
```gcql
namespace:{workflow-event-service-namespace} level:error
```

Also check traces for failed HTTP calls:
```gcql
* | filter namespace:{workflow-event-service-namespace} http_status_code:504 | sort by (_time desc) | limit 10
```

If 504s are present for the patient's org, the event-driven path is blocked for ALL patients in that org.

### Step 5: Check StarterActivityCreationService

Search for starter activity logs:
```gcql
* | filter namespace:task-service-{env} content:*StarterActivity* content:*{patientId}* | sort by (_time desc) | limit 20
```

If not found with patientId, broaden:
```gcql
* | filter namespace:task-service-{env} content:*StarterActivity* | sort by (_time desc) | limit 20
```

Look for these specific log patterns:
- `"Creating starter activities for clientPatientId"` → Starter was triggered AND creating tasks
- `"Skipping starter activity creation as the queryParams do not contain the key [code]"` → Starter was triggered but skipped due to filter mismatch
- `"Using client-specific starter trigger filter for org"` → Shows which org filter is active
- `"Some activities already exist for"` → Tasks already exist, starter skipped
- `"No activities in any status exist for"` → Prerequisite met, will attempt creation

### Step 6: Check hp-facade and Read Path

hp-facade routes Task searches to `task-service.{env}.bwell.zone`.

```gcql
* | filter namespace:hp-facade-{env} mdc.clientFhirPersonId:{personId} | sort by (_time desc) | limit 20
```

Check for:
- Is the request reaching task-service?
- Are query params correct (`status=ready` vs `status=` empty)?
- Is there any response caching layer?

### Step 7: Check Kafka Consumer Lag (Are We Falling Behind?)

Use PromQL metrics in groundcover to check if task-service consumers are keeping up with production rate. This is critical for diagnosing delayed task creation.

**Check `task.commands` consumer lag (task-request — creates tasks for ALL orgs):**
```promql
sum(kafka_consumer_fetch_manager_records_lag_max{k8s_namespace_name="task-service-{env}", spring_id=~"not.managed.*task-request.*"})
```

**Check `cohortEvaluation` consumer lag (processes CohortMembershipEvaluated events — leading indicator):**
```promql
sum(kafka_consumer_fetch_manager_records_lag_max{k8s_namespace_name="task-service-{env}", spring_id=~"cohortEvaluation.*"})
```

**Check all consumer types at once:**
```promql
kafka_consumer_fetch_manager_records_lag_max{k8s_namespace_name="task-service-{env}"}
```

Then group results by `spring_id` prefix to identify which consumer type is lagging.

**Consumer types in task-service:**

| Consumer Factory | Topic | Impact |
|-----------------|-------|--------|
| `not.managed.by.Spring.consumer-task-service-task-request-*` | `task.commands` | Creates tasks from TaskCreateRequest events (ALL orgs share this) |
| `cohortEvaluationConsumerFactory` | `CohortMembershipEvaluatedScheduledBatch` | Processes batch CQL evaluations → can cascade into task.commands |
| `activityChangeEventConsumerFactory` | `ActivityChanged` | Processes activity definition changes |
| `questionnaireResponseConsumerFactory` | QuestionnaireResponse events | Low volume, rarely lags |

**Interpretation:**
- `task.commands` lag > 100: Moderate delay (1-3 min) for new task creation across ALL orgs
- `task.commands` lag > 500: Significant delay (5-10 min), users will notice
- `cohortEvaluation` lag growing rapidly: Leading indicator — will cascade into `task.commands` lag within minutes
- `cohortEvaluation` lag > 10K: Active incident, likely a batch run or CQL engine catch-up flooding the system

**Cascade pattern:**
```
CQL engine evaluates → publishes CohortMembershipEvaluated
→ task-service cohortEvaluation consumer processes
→ produces TaskCreateRequest on task.commands
→ task-service task-request consumer creates tasks in FHIR
```

When cohortEvaluation lag spikes, task.commands lag follows shortly after.

### Step 8: Check CQL Engine Tenant Routing

The CQL engine (`bsights-engine-cql-{env}`) has **per-tenant consumer isolation** with dedicated consumer threads per org. Each org's evaluations are prefixed with `[eval-{orgName}]` in log messages.

**Check which orgs are being evaluated:**
```gcql
* | filter namespace:bsights-engine-cql-{env} content:*eval-* | sort by (_time desc) | limit 50
```

**Check for specific org:**
```gcql
* | filter namespace:bsights-engine-cql-{env} content:*eval-{orgName}* | sort by (_time desc) | limit 20
```

**Key observations:**
- Each org gets dedicated consumer threads (e.g., `bwell-kafka-consumer-11` for samsung, `bwell-kafka-consumer-40` for walgreens)
- The `[eval-{orgName}]` prefix in messages identifies which tenant's evaluation is running
- **No logs for an org does NOT mean no processing** — successful evaluations may only log at DEBUG level
- The CQL engine's per-tenant isolation means one org's slow evaluations do NOT block another org's CQL evaluations
- However, the OUTPUT of all CQL evaluations converges on the shared `task.commands` topic — that's where cross-org interference happens

**Cross-org delay diagnosis:**
If org A's tasks are delayed but the CQL engine shows no issues for org A:
1. Check if org A uses `workflow-event` eligibility source (not CQL) — look at `eligibility-source-config` in application.yaml
2. Check `task.commands` consumer lag — this is the shared bottleneck where ALL orgs compete
3. Look for high-volume orgs (samsung, walgreens) flooding `task.commands` via their CQL evaluations

### Step 9: Check eBPF Traces (Cross-Service)

Use traces WITHOUT service filter to see the full request chain:
```gcql
http.url contains "Patient/{patientId}"
```

For FHIR server MongoDB queries:
```gcql
db.statement contains "care-need"
```

**Pitfall**: `response_size_bytes: 0` in traces is often a tracing artifact when responses use br/gzip compression. It does NOT mean an empty response was returned.

## Key MDC Fields for Filtering

| Field | Description |
|-------|-------------|
| `mdc.clientFhirPersonId` | Person ID (best cross-service identifier) |
| `mdc.clientFhirPatientId` | Patient ID |
| `mdc.managingOrganization` | Org UUID |
| `mdc.subject` | Subject reference (e.g., `Patient/{id}`) |
| `mdc.activityType` | Activity type (e.g., `care-need`) |

## Key Services and Namespaces

Namespace pattern: `{service}-{env}` where `{env}` is one of: `client-sandbox`, `dev`, `staging`, `production`

FHIR pipeline URL pattern: `fhir-pipeline.{env}.bwell.zone` (e.g., `fhir-pipeline.client-sandbox.bwell.zone`, `fhir-pipeline.dev.bwell.zone`)

| Service | Namespace pattern | Role |
|---------|-----------|------|
| consent-service | `consent-service-{env}` | Publishes consent events to `user_profile.consent_management.events` topic |
| workflow-event-service | `workflow-event-service-{env}` | Consumes consent events, resolves Person/$graph, triggers task creation |
| task-service | `task-service-{env}` | Creates/updates tasks, serves Task searches |
| bsights-engine-cql | `bsights-engine-cql-{env}` | CQL evaluation engine; per-tenant consumers with `[eval-{org}]` log prefix |
| hp-facade | `hp-facade-{env}` | Routes client requests, proxies to task-service for Task resources |
| fhir-pipeline | `fhir-server-pipeline-{env}` | FHIR server (used by workflow-event-service for existence checks) |
| hp-notification-service | `hp-notification-service-{env}` | Downstream consumer of task events from `fhir.task` topic |
| analytics-sync-service | `analytics-sync-service-{env}` | Consumes consent events for analytics (separate from task creation) |

## Report Format

After gathering all data, produce this structured report:

```markdown
## Task Creation Pipeline Report

**Patient:** {personId} / {patientId}
**Environment:** {environment}
**Time Window:** {startTime} → {endTime}

### Task Existence Check
- **FHIR query result:** {exists/does not exist}
- **Task ID (if found):** {taskId}

### Events Timeline

| Time | Service | Event | Details |
|------|---------|-------|---------|
| HH:MM:SS | consent-service | Consent granted | Published to `user_profile.consent_management.events` |
| HH:MM:SS | workflow-event-service | Consent event consumed | Topic: `user_profile.consent_management.events`, Partition: N |
| HH:MM:SS | workflow-event-service | POST Person/$graph | fhir-pipeline → {status} ({duration}ms) |
| HH:MM:SS | workflow-event-service | GET /4_0_0/Person/{personId} | fhir-pipeline → {status} ({duration}ms) |
| HH:MM:SS | task-service | Task existence checks | N queries by activityId → all {status} |
| HH:MM:SS | task-service | Tasks published to fhir.task | hp-notification-service consuming |
| HH:MM:SS | task-service | GetTask client queries | code={codes}, status={status} |

### Event-Driven Path (Kafka Flow)
- **Tasks created:** {count} tasks via TaskCreateRequest events
- **Source:** workflow-event-service → task.commands topic
- **Timing:** Tasks created at {timestamp}
- **504 errors:** {yes/no, if yes: from fhir-pipeline for org {orgName}}

### StarterActivityCreationService Status
- **Triggered:** Yes/No
- **Result:** Created tasks / Skipped (reason) / Never invoked
- **Reason:** {explanation}
- **Org Filter:** {org name} requires code={filter value}

### Kafka Consumer Lag Health
- **task.commands (task-request):** {current lag} messages ({healthy/moderate/critical})
- **cohortEvaluation:** {current lag} messages ({healthy/growing/spiking})
- **Trend:** {stable/growing/recovering}
- **Cross-org impact:** {yes/no — if yes, which orgs are flooding}

### Race Condition Analysis
- **First task creation (async):** {timestamp}
- **First GetTask with matching code:** {timestamp}
- **Gap:** {duration}
- **Verdict:** Tasks existed before GetTask arrived → StarterActivity never fires
  OR: GetTask arrived before async tasks → StarterActivity should have fired

### Diagnosis
{Clear explanation of what happened and why tasks are or aren't visible}

### Recommended Action
{What to do next}
```

## Common Pitfalls

| Pitfall | Explanation |
|---------|-------------|
| No logs ≠ no processing | Prod runs at INFO level. Successful task creation only logs at DEBUG. Absence of logs does NOT mean the event wasn't processed. |
| `content:` filter empty | Structured JSON log fields aren't searchable via `content:`. Use MDC field filters. |
| `response_size_bytes: 0` | Often a tracing artifact when responses use br/gzip compression. Does not mean empty response. |
| Large org 504s | workflow-event-service may timeout calling fhir-pipeline for orgs with large patient datasets. Blocks ALL event-driven creation for that org. |
| Starter filter mismatch | `shouldNoOpBasedOnQueryParamsPresent()` silently skips creation if query params don't match the org-specific `starterTriggerFilter`. |
| Task exists but not visible | Task may exist in FHIR but app query uses different params (e.g., empty `status=` vs `status=ready`). Always verify with direct FHIR query first. |
| Wrong prod namespace | Production namespaces use `-prod` suffix, NOT `-production`. Use `task-service-prod`, `bsights-engine-cql-prod`, etc. |
| Cross-org delay via shared topic | CQL engine has per-tenant isolation, but `task.commands` topic is shared. High-volume orgs (samsung, walgreens) can flood `task.commands` and delay task creation for all orgs. |
| cohortEvaluation cascade | Rising `cohortEvaluationConsumerFactory` lag is a leading indicator — it cascades into `task.commands` lag within minutes as processed events generate TaskCreateRequests. |
| task-service restart flood | When task-service restarts, it re-publishes `CqlEvaluationRequested` events for ALL patients/orgs, causing a burst that overwhelms downstream consumers. |

## Key Knowledge

### Consent Event as Pipeline Entry Point

The consent event is what kicks off the entire task creation pipeline. When a patient grants consent during registration/onboarding:
1. `consent-service` publishes to `user_profile.consent_management.events` Kafka topic
2. `workflow-event-service` consumes the event (on a `KafkaListenerEndpointContainer` thread)
3. It calls `POST /4_0_0/Person/{personId}/$graph` on fhir-pipeline to resolve patient relationships
4. It calls `GET /4_0_0/Person/{personId}` to fetch the Person record
5. Based on the graph, it publishes `TaskCreateRequest` events to `task.commands` topic
6. `task-service` consumes and creates tasks (checking existence first by activityId)
7. Created tasks are published to `fhir.task` topic for downstream consumers (e.g., hp-notification-service)

**Key trace pattern**: The trace ID propagates from the consent event through workflow-event-service, into the task creation, and all the way to hp-notification-service consuming from `fhir.task`. Use the trace ID from workflow-event-service spans to follow the complete chain.

### Why StarterActivityCreationService Often Doesn't Fire

The starter mechanism checks `tasks.isEmpty()` during a GetTask request. But the async Kafka flow (consent event → workflow-event-service → task.commands → task-service consumer) typically creates tasks at user registration/onboarding time — well before the user opens the app and triggers a GetTask.

The only scenario where starter tasks fire:
1. A GetTask request arrives with query params matching the `starterTriggerFilter`
2. AND zero tasks exist for that patient

This is effectively a race condition that the async flow almost always wins.

### Org-Specific Starter Filter

Organizations can configure a `starterTriggerFilter` via `ActivityProperties.clientConfig`. The filter restricts which GetTask query parameters trigger starter creation.

Resolution flow: `managingOrganization` UUID → `OrganizationProvider.findAllBwellTenantOrganizations()` → match by ID → extract org name from `SECURITY_TAG` identifier → look up `clientConfig` by name.

### Cross-Org Task Creation Delay (Noisy Neighbor)

The task creation pipeline has a shared bottleneck at the `task.commands` Kafka topic. Even though the CQL engine has per-tenant consumer isolation, all orgs' `TaskCreateRequest` events converge on `task.commands`:

```
┌──────────────────────────────┐
│ CQL Engine (tenant-isolated) │
│  [eval-samsung] ─────────────┼──┐
│  [eval-walgreens] ───────────┼──┤
│  [eval-wellsense] ───────────┼──┤   ┌────────────────────┐     ┌─────────────────────┐
│                              │  ├──→│ task.commands topic │────→│ task-service         │
└──────────────────────────────┘  │   │ (ALL orgs mixed)   │     │ taskRequest consumer │
                                  │   └────────────────────┘     │ concurrency: 10      │
┌──────────────────────────────┐  │                              │ max-poll-records: 5   │
│ workflow-event-service        │──┘                              └─────────────────────┘
│  (consent → TaskCreateRequest)│
└──────────────────────────────┘
```

**Why org A delays org B:**
1. task-service restart re-publishes `CqlEvaluationRequested` events for ALL orgs
2. CQL engine processes them in parallel (per-tenant), producing many `TaskCreateRequest` events simultaneously
3. All `TaskCreateRequest` events land on `task.commands` — a shared topic with limited consumer capacity (concurrency: 10, max-poll-records: 5)
4. High-volume orgs (samsung with 50K+ resources, multiple CQL libraries) produce many more TaskCreateRequests
5. Small orgs (bwell_demo) queue behind the flood

**Diagnosis checklist for cross-org delays:**
1. Check `task.commands` consumer lag (Step 7)
2. If lag is high, check which orgs are producing the most events via CQL engine logs (Step 8)
3. Check if task-service recently restarted (`content:*Started TaskServiceApplication*`)
4. Check `cohortEvaluation` lag as a leading indicator of upcoming `task.commands` pressure

### CQL Engine Architecture

The CQL engine (`bsights-engine-cql`) is a separate service that:
- Consumes from `health_programs.activity.commands.CqlEvaluationRequested` (30 partitions)
- Also consumes consent events from `user_profile.consent_management.events`
- Also consumes connection data events from `access_to_health_data.connection_data.events`
- Has **per-tenant consumer threads** with `[eval-{orgName}]` prefix in logs
- Evaluates CQL libraries against patient FHIR data
- Produces results that eventually become `TaskCreateRequest` events on `task.commands`

**Known failure modes:**
- Large patients (50K+ resources) take 30s+ per library evaluation
- FHIR 504 timeouts on Medication/Observation queries block consumer threads
- CQL library compilation errors (e.g., "Could not load source for library BWellCommon") fail silently per-library
- `PersonBundleRetrievalException` when Person/$graph fails — skips entire patient

**Namespace:** `bsights-engine-cql-{env}` (e.g., `bsights-engine-cql-prod`)
