---
name: data-connection-outreach
description: Process CX team data connection outreach requests. Downloads Google Sheets, enriches person data via FHIR/IntegrationHub APIs, deduplicates against existing subscriptions, and produces FoundConnection Kafka events to trigger user notifications via workflow-event-service.
argument-hint: "<Google Sheet URLs or Jira ticket URL> — e.g. 'INE-560', or paste sheet URLs directly"
disable-model-invocation: false
allowed-tools: Bash, Read, Write, Agent, mcp__google-drive__download_file_from_url, mcp__google-drive__get_drive_file_contents_by_file_id, mcp__google-drive__search_drive_files, mcp__atlassian__getJiraIssue
---

# Data Connection Outreach Skill

Process data connection outreach requests from the CX team. This automates the manual workflow of downloading spreadsheets, enriching person/connection data, deduplicating against existing subscriptions, and producing Kafka events.

## Workflow Overview

```
CX Team Spreadsheet → Download → Enrich via FHIR/IntegrationHub → Dedup (Subscription check) → Produce Kafka Events
```

## Prerequisites

### Python Environment

Create a venv with the required packages:
```bash
python3 -m venv venv
source venv/bin/activate
pip install kafka-python pandas requests openpyxl python-dotenv
```

### Credentials

Create a `.env.prod` file in your working directory (DO NOT commit this file):
```bash
# FHIR API (Prod)
FHIR_TOKEN_URL=https://fhir-bwell.auth.us-east-1.amazoncognito.com/oauth2/token
FHIR_CLIENT_ID=<your-fhir-client-id>
FHIR_CLIENT_SECRET=<your-fhir-client-secret>
FHIR_BASE_URL=https://fhir.prod.bwell.zone/4_0_0

# IntegrationHub API (Prod)
INTHUB_TOKEN_URL=https://integrationhub-identity-prod.auth.us-east-1.amazoncognito.com/oauth2/token
INTHUB_CLIENT_ID=<your-inthub-client-id>
INTHUB_CLIENT_SECRET=<your-inthub-client-secret>
INTHUB_BASE_URL=https://integrationhub-service.prod-mstarvac.bwell.zone

# Kafka (Prod) — SASL_SSL with SCRAM-SHA-512
KAFKA_BOOTSTRAP_SERVERS=<broker1:9096>,<broker2:9096>
KAFKA_USERNAME=<msk-username>
KAFKA_PASSWORD=<msk-password>
```

Credentials can be found in:
- FHIR & IntegrationHub: AWS Cognito (prod app client for DQM/outreach scripts)
- Kafka: MSK prod cluster credentials (same as other DCS pipeline services)

### File Drop Directory

Create two directories in your working directory:
```
./input_csvs/       ← Drop CX team spreadsheets here (.csv or .xlsx)
./processed_csvs/   ← Completed files are archived here after processing
```

## Step-by-Step Process

### Step 0: Prompt for Configuration

Before starting, ask the user:
1. **Which client slug(s) to process** — e.g., `walgreens`. Only rows matching these client slugs will be processed. Currently only `walgreens` is live for outreach notifications. Samsung Health is not yet live.
2. **Dry run or live?** — Dry run does everything except producing Kafka events. Default to dry run for safety.

### Step 1: Get the Spreadsheet URLs

If the user provides a Jira ticket (e.g., `INE-560`), fetch it and extract Google Sheet URLs from the description. The description will contain links like:
```
https://docs.google.com/spreadsheets/d/{FILE_ID}/edit?gid=0#gid=0
```

If the user provides URLs directly, use those.

### Step 2: Download the Spreadsheets

**Primary method:** Use `mcp__google-drive__download_file_from_url` to download each sheet.

**Known limitation:** Files in b.well Shared Drives return 404 from the download API (missing `supportsAllDrives` flag). In this case, use the **search-and-manual-download fallback**:

1. Use `mcp__google-drive__search_drive_files` with `name: "Request for Connection"` to find recent sheets
2. Show the user the list of found sheets with their dates and URLs
3. Ask the user to download the relevant `.xlsx` files to `./input_csvs/`

**Alternative — if user already has the files:** Skip this step entirely. The user may have already downloaded the sheets or received them via email/Slack. Just place them in `./input_csvs/`.

The script handles both `.xlsx` and `.csv` files — `.xlsx` files are auto-converted to CSV during processing.

### Step 3: Process and Enrich the Data

There are two possible input formats:

**Format A — Standard CX Team Sheets (most common):**
```
FHIR ID, ConnectHub Organization ID, Client Name
```
(Plus many other columns like NPI, Institution Name, Ticket IDs, etc. — all ignored)

These require the full enrichment process below.

**Format B — Pre-enriched Sheets (rare, from DCON Outreach Review):**
```
service_slug, displayLabel, bwell_prsn_uuid, client_prsn_uuid, managing_organization_reference_value, pat_uuid
```
These already have all the enriched data. Skip enrichment and go directly to Step 4 (review) then Step 5 (produce events). Map columns as:
- `bwell_prsn_uuid` → `bwellFhirPersonId`
- `client_prsn_uuid` → `clientFhirPersonId`
- `managing_organization_reference_value` → `managingOrganization`
- `pat_uuid` → `clientPatientId`
- `service_slug` → `dataSourceId`
- `displayLabel` → `dataSourceName`

**Detecting format:** Check the CSV headers. If `service_slug` and `bwell_prsn_uuid` are present, it's Format B. Otherwise it's Format A.

For each row in the CSV, the enrichment process:

1. **Filter by supported client slugs** — currently only `walgreens` (from `Client Name` column, lowercased)
2. **Get Person Graph** — Call FHIR `$graph` on the master person to find the client-specific Person and Patient resources
3. **Get Connection Info** — Call IntegrationHub `/clientconnection/{org_id}` to get the `clientConnectionName` (slug) and `displayLabel` from the `.data` object
4. **Get FHIR Organization ID** — Look up the Organization resource with `name={clientSlug}&type=bwell-tenant` (**CRITICAL: must include `type=bwell-tenant`** — without it, you'll get pharmacy/provider Organizations instead of the tenant Organization that workflow-event-service uses for ActivityDefinition lookup)
5. **Check for Existing Subscription** — Query FHIR for a Subscription with the client person ID and service slug. This is the **deduplication gate** (prevents re-notifying users who already have the connection)

Output two CSVs:
- `results_with_subscription.csv` — already connected, skip these
- `results_without_subscription.csv` — need outreach notification

### Step 4: Review Results Before Producing Events

**ALWAYS** show the user a summary before producing events:
- Total rows in input
- Rows filtered out (unsupported client slug)
- Rows skipped (missing graph data / missing connect hub slug)
- Rows with existing subscription (will NOT be notified)
- Rows without subscription (WILL be notified)

Get explicit user confirmation before proceeding to event production.

### Step 5: Produce Kafka Events

For each row in `results_without_subscription.csv`, produce a CloudEvents message to the `connections.workflow.events` topic on the **prod** Kafka cluster.

Event format:
```json
{
  "specversion": "1.0",
  "id": "<uuid>",
  "source": "script",
  "type": "FoundConnection",
  "datacontenttype": "application/json",
  "time": "<ISO-8601 UTC>",
  "data": {
    "bwellFhirPersonId": "<master_person_id>",
    "clientFhirPersonId": "<client_person_id>",
    "managingOrganization": "<fhir_organization_id>",
    "clientPatientId": "<client_patient_id>",
    "dataSourceId": "<connect_hub_slug (lowercased)>",
    "dataSourceName": "<institution_display_label>"
  }
}
```

Key: `client_person_id`
Topic: `connections.workflow.events`
Headers: `content-type: application/cloudevents+json`

### Step 6: Verification (Spot Check)

After producing events, verify the end-to-end pipeline processed them:

#### 6a. Confirm Kafka Consumption

Check that the `workflow-event-service-requested-data-source` consumer group committed the expected offset:

```python
from kafka import KafkaConsumer, KafkaAdminClient, TopicPartition

admin = KafkaAdminClient(**kafka_config)
consumer = KafkaConsumer(**kafka_config)

partitions = consumer.partitions_for_topic('connections.workflow.events') or set()
tps = [TopicPartition('connections.workflow.events', p) for p in partitions]

committed = admin.list_consumer_group_offsets('workflow-event-service-requested-data-source')
end_offsets = consumer.end_offsets(tps)

lag = sum(end_offsets[tp] - committed[tp].offset for tp in tps if tp in committed)
print(f'Total lag across {len(tps)} partitions: {lag}')
# Lag should be ~0 after consumption catches up

consumer.close()
admin.close()
```

#### 6b. Confirm Task Creation

Pick 3-5 sample rows from `results_without_subscription.csv` and verify FHIR Tasks were created. Search by the `connection-id` identifier (the `connect_hub_slug` value, NOT the client slug):

```
GET /Task?identifier=urn:system:data-connection/connection-id|{connect_hub_slug}&patient=Patient/{client_patient_id}&_count=1
```

Expected: Task with `status: ready`, `for: Patient/{client_patient_id}`, and identifiers including `activityTitle: "Connect to {displayLabel}"`.

**Important:** The `connection-id` on the Task is the `dataSourceId` from the event (e.g., `dignity_health_yavapai_regional_medical_center`), NOT the client slug (`walgreens`).

#### 6c. Confirm Notification Delivery (optional)

Check hp-notification-service logs for `TaskChangeEvent` processing related to the created tasks. Look for events flowing to Iterable without duplicates.

## Implementation Details

### FHIR $graph Call

POST to `{FHIR_BASE_URL}/Person/{person_id}/$graph` with:
```json
{
  "resourceType": "Parameters",
  "parameter": [{
    "name": "graph",
    "resource": {
      "resourceType": "GraphDefinition",
      "name": "hpGraphDefinition",
      "status": "active",
      "start": "Person",
      "link": [{
        "path": "link[x].target",
        "description": "linked Persons",
        "target": [{
          "type": "Person",
          "link": [{
            "path": "link[x].target",
            "description": "linked Patient",
            "target": [{"type": "Patient"}]
          }]
        }]
      }]
    }
  }]
}
```

From the response, find resources matching the client slug by checking `meta.security` for `system: "https://www.icanbwell.com/owner"` and `code: <client_slug>`.

### IntegrationHub Client Connection Lookup

```
GET {INTHUB_BASE_URL}/clientconnection/{connect_hub_org_id}
Authorization: Bearer {inthub_token}
```

Response structure:
```json
{
  "data": {
    "clientConnectionName": "slug_value",
    "displayLabel": "Human-Readable Name"
  }
}
```

**NOT** `/organization/` — that path returns 404.

### FHIR Organization Lookup

```
GET {FHIR_BASE_URL}/Organization?name={client_slug}&type=bwell-tenant&_count=1
```

Returns the **tenant** Organization ID used by workflow-event-service for ActivityDefinition resolution.

### Subscription Check (Dedup)

```
GET {FHIR_BASE_URL}/Subscription?extension=https://icanbwell.com/codes/client_person_id|{client_person_id}&extension=https://icanbwell.com/codes/service_slug|{connect_hub_slug}
```

If a result is returned, the user already has the connection — skip them.

### Client Slug Filtering

The `Client Name` column value is lowercased and used as the client slug for FHIR lookups (e.g., "Samsung Health" → "samsung health"). Only rows matching the user-configured slug(s) are processed.

Known valid slugs: `walgreens` (live for outreach), `samsung health` (not yet live)

If the CSV contains a client slug the user didn't specify, warn them and skip those rows.

### Kafka Producer Configuration

```python
from kafka import KafkaProducer

producer = KafkaProducer(
    bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS.split(','),
    security_protocol='SASL_SSL',
    sasl_mechanism='SCRAM-SHA-512',
    sasl_plain_username=KAFKA_USERNAME,
    sasl_plain_password=KAFKA_PASSWORD,
    value_serializer=lambda v: json.dumps(v).encode('utf-8'),
    key_serializer=lambda k: k.encode('utf-8') if k else None,
    request_timeout_ms=15000,
    api_version=(2, 8, 0),  # REQUIRED — prevents version probe hang on MSK
)
```

**ALWAYS** set `api_version=(2, 8, 0)` — the auto version probe hangs on MSK.
**ALWAYS** use `signal.alarm(60)` as a hard timeout to prevent zombie processes.

## Safety Checks

1. **NEVER produce events without user confirmation** — always show the count and a sample of what will be sent
2. **Deduplication is critical** — the subscription check prevents sending duplicate notifications (INE-597)
3. **Token refresh** — tokens expire after ~60 minutes. For large batches, refresh tokens mid-process
4. **Rate limiting** — Add a small delay between FHIR API calls to avoid overwhelming the server. 100ms between calls is sufficient.
5. **Dry run option** — If the user says "dry run" or "test", do everything except the Kafka produce step
6. **Organization ID** — Always use `type=bwell-tenant` when looking up the Organization. The wrong org ID causes `ActivityDefinitionNotFoundException` in workflow-event-service.

## Output

After completion, report:
- Number of events produced
- Consumer group committed offset (confirming consumption)
- Sample Task verification result (Task ID, status, patient reference)
- Any errors/skipped rows and why
- Path to the output CSVs for audit trail

Archive the input CSVs to `./processed_csvs/` when done.
