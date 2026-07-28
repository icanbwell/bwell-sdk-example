# Groundcover Query Patterns for ATS

## MCP Tools Reference

All groundcover queries are executed via MCP tools. Choose the right tool for your investigation:

| Tool | Purpose | Key Parameters |
|------|---------|----------------|
| `mcp__groundcover__query_logs` | Log investigation (errors, refresh, OAuth, user activity) | `query`, `period`, `start`, `end` |
| `mcp__groundcover__query_traces` | Latency, 5xx errors, verifying callbacks, full URL inspection | `query`, `period`, `start`, `end` |
| `mcp__groundcover__query_events` | Pod crashes, OOMKills, restarts, K8s events | `query`, `period`, `start`, `end` |
| `mcp__groundcover__query_monitors` | List monitor definitions and their health | `query`, `limit`, `skip` |
| `mcp__groundcover__query_issues` | Active/historical alert firings | `query`, `period`, `start`, `end` |

### Parameter Notes

- **`query`** (required): gcQL query string. Must start with `*` or a filter. Always include `| limit N`.
- **`period`**: ISO 8601 duration. Tool default is `PT1H`. **Always override to `P7D`** for ATS investigation.
- **`start`** / **`end`**: RFC3339 timestamps. Use instead of `period` for specific time ranges.
- Time range is set via parameters, NOT in the query string itself.

## gcQL Syntax Quick Reference

- Queries must start with a filter or `*` (match-all)
- Stats grouping: `stats by (field) count()` (NOT `stats count() by field`)
- Sort syntax: `sort by (field desc)` (NOT `sort field desc`)
- Numeric comparisons: NO colon — use `duration_seconds>1` not `duration_seconds:>1`
- Always use `_time` for timestamps
- Always include `| limit N` to bound results

## ATS Service Identifiers (CONFIRMED)

ATS has **TWO separate workloads** in groundcover:
- **`aperture-token-service`** — the main API server (handles GraphQL, OAuth callbacks, REST endpoints)
- **`aperture-token-service-pipelines`** — the batch pipeline (handles token refresh, scheduled jobs)

**CRITICAL**: Token refresh logs (`process_refresh_tokens_results`, `handle_token_refresh_error`, `handle_token_refresh_response`) are logged by the **pipelines** workload, NOT the main API workload.

Workload filter usage:
- **API issues**: `workload:aperture-token-service`
- **Refresh/pipeline issues**: `workload:aperture-token-service-pipelines`
- **Both**: omit workload filter, or use `workload:*aperture-token-service*`
- **Wildcard alternative**: `workload:*aperture-token*` (catches both)
- For traces: `service.name:aperture-token-service`

Environments confirmed in logs:
- `env:production` (cluster: `prod-ue1`)
- `env:client-sandbox` (cluster: `client-sandbox-ue1`)

## Structured Attribute Filters (CRITICAL)

ATS logs emit structured `extra` fields that become **directly filterable attributes** in groundcover. These are MORE reliable than `content:*...*` wildcard matching.

### Available structured attribute filters:

| Attribute | Where It's Logged | Example |
|-----------|-------------------|---------|
| `client_fhir_person_id` | refresh results, callback errors | `client_fhir_person_id:83e2d2c3-77d6-461e-83c0-5f8c565830bc` |
| `service_slug` | refresh results, callback errors | `service_slug:evgh` |
| `member_id` | refresh results, callback errors | `member_id:<uuid>` |
| `bwell_fhir_person_id` | refresh results | `bwell_fhir_person_id:<uuid>` |
| `funcName` | all structured logs | `funcName:process_refresh_tokens_results` |
| `code` | refresh results | `code:RefreshError` |
| `status` | refresh results | `status:Expired` |
| `prev_status` | refresh results | `prev_status:Data Retrieved` |

### How to use structured attributes:

```gcql
# Find refresh results for a specific user + service_slug
* | filter client_fhir_person_id:<person_id> service_slug:<slug> | sort by (_time desc) | limit 20

# Find all refresh errors
* | filter workload:aperture-token-service-pipelines funcName:handle_token_refresh_error | sort by (_time desc) | limit 20

# IMPORTANT: Do NOT combine workload:aperture-token-service with these attributes for refresh logs
# Refresh logs come from workload:aperture-token-service-pipelines
# Either omit the workload filter or use the pipelines workload
```

### Gotcha: workload filter + attribute filter combination

If `client_fhir_person_id:<id>` returns empty with `workload:aperture-token-service`, try:
1. Remove the workload filter entirely (attributes work across workloads)
2. Use `workload:aperture-token-service-pipelines` for refresh-related queries

## Critical Query Gotchas (Learned from Experience)

### 1. Queries with too many `content:*...*` filters often FAIL
- **BAD**: `* | filter workload:aperture-token-service content:*cde27c3f* content:*error*` — tends to fail
- **GOOD**: `* | filter workload:aperture-token-service content:*cde27c3f* level:error` — use `level:` field instead of content matching for log level
- **GOOD**: `* | filter workload:aperture-token-service content:*cde27c3f*` — then filter results programmatically

### 2. Use `level:error` field filter, NOT `content:*error*`
- The `level` field is indexed and reliable: `level:error`, `level:info`, `level:warning`
- Content matching for "error" picks up false positives (URLs with "error" in them, etc.)

### 3. Large results get saved to files — ALWAYS parse them with Python/jq
- When results exceed token limits, they're saved to a `.txt` file in JSON format
- The file format is: `[{"type": "text", "text": "[...actual JSON array of log entries...]"}]`
- Parse with: `json.loads(data[0]['text'])` to get the inner array
- Each log entry has: `timestamp`, `level`, `env`, `body` (raw JSON string), `content` (text summary)
- The `body` field contains the structured JSON log — parse it for `message`, `funcName`, extra fields

### 4. Keep queries simple — one or two content filters max
- Queries with 3+ `content:*...*` filters frequently fail with "failed to query logs"
- If you need complex filtering, fetch broader results and filter programmatically

### 5. Results are limited — use `| limit N` wisely
- Default limit if not specified varies; always specify explicitly
- For investigation, `limit 20-30` is usually sufficient
- For time-based analysis, combine with `period` parameter

## Recommended Investigation Approach

**Default time range**: Always set `period: "P7D"` in the MCP tool call unless investigating a specific recent incident.

**Tool selection**:
- Start with `mcp__groundcover__query_logs` for all log-based investigation
- Switch to `mcp__groundcover__query_traces` for latency/5xx/callback verification
- Use `mcp__groundcover__query_events` only for pod-level health issues

### For a specific user/connection issue:

**Step 1**: Use structured attribute filter for the user's `client_fhir_person_id` (most reliable):
```gcql
* | filter client_fhir_person_id:<client_fhir_person_id> | sort by (_time desc) | limit 30
```
Note: Omit workload filter here — it catches logs from both API and pipelines workloads.

**Step 2**: If Step 1 returns empty or you need API-level activity (GraphQL calls, OAuth callbacks), use content search:
```gcql
* | filter workload:aperture-token-service content:*<client_person_id>* | sort by (_time desc) | limit 30
```

**Step 3**: If results are large, parse the saved file and look for:
- `funcName: "handle_callback_generic"` — shows OAuth callback results (success/failure)
- `funcName: "before_request"` — shows what API calls the user made
- `funcName: "fetch_batch"` — shows SubscriptionStatus lookups
- `funcName: "make_request_with_retries"` — shows Connection Hub config fetches
- `funcName: "process_refresh_tokens_results"` — shows refresh outcomes
- `funcName: "handle_token_refresh_error"` — shows refresh failures
- `level: "error"` entries — actual errors

**Step 4**: For error details, query with `level:error`:
```gcql
* | filter workload:aperture-token-service content:*<client_person_id>* level:error | sort by (_time desc) | limit 10
```

**Step 5**: For service_slug-specific issues (use structured attribute):
```gcql
* | filter service_slug:<service_slug> | sort by (_time desc) | limit 20
```

**Step 6**: For refresh-specific investigation:
```gcql
* | filter client_fhir_person_id:<person_id> service_slug:<slug> | sort by (_time desc) | limit 20
```

### Key log patterns to look for in ATS:

| Log Pattern | What It Means |
|-------------|---------------|
| `handle_callback_generic` + `error=access_denied` | Provider rejected the OAuth request |
| `handle_callback_generic` + `code=...` | Successful OAuth callback |
| `[EXTRACT_PATIENT_ID] Unable to get patient_id` | Token response missing patient identifier |
| `make_request_with_retries` + `status_code: 200` | Connection Hub config fetched OK |
| `fetch_batch` + `SubscriptionStatusServiceHelper` | Looking up connection status |
| `getOauthUrl` in kwargs | User initiated a connection flow |
| `getDataSource` in query body | User checking connection details |
| `get_member_connections` | User listing their connections |
| `error_description=Policy+evaluation+failed` | Provider-side policy rejection |
| `error_description=User+refused` | User cancelled the OAuth flow |

### Parsing the `body` field:

ATS logs are JSON-structured. The `body` field in each log entry is a JSON string with these useful fields:
```json
{
  "funcName": "handle_callback_generic",
  "pathname": "/app/aperture_token_service/oauth/views.py",
  "message": "the actual log message",
  "name": "aperture_token_service.oauth.interactor",
  "service_slug": "example_provider",
  "connection_version": 1,
  "otelTraceID": "...",
  "level": "ERROR"
}
```

Extra fields beyond the standard ones are **context-specific** and very useful (e.g., `service_slug`, `connection_version`, `client_slug`).

### For the `before_request` middleware logs:

The request body is logged in the message for GraphQL calls:
```
[BEFORE_REQUEST] Started: POST /graphql <request_id> b'{"variables":{"connectionId":"..."},"query":"query(...){...}"}'
```

This tells you exactly which GraphQL operation the user called.

## Common Investigation Scenarios (all use `mcp__groundcover__query_logs` with `period: "P7D"` unless noted)

### 1. "User can't connect to a provider"

```gcql
# Get all activity for the user
* | filter workload:aperture-token-service content:*<client_person_id>* | sort by (_time desc) | limit 30

# Check for OAuth callback errors (provider rejections)
* | filter workload:aperture-token-service content:*<service_slug>* content:*callback* | sort by (_time desc) | limit 10
```

Common provider errors in callbacks:
- `error=access_denied&error_description=Policy+evaluation+failed` — provider policy rejection
- `error=access_denied&error_description=User+refused` — user cancelled
- `error=server_error` — provider internal error

### 2. "Why did this connection expire?"

```gcql
* | filter workload:aperture-token-service content:*EXPIRED* | sort by (_time desc) | limit 30
* | filter workload:aperture-token-service content:*refresh* level:error | sort by (_time desc) | limit 30
* | filter workload:aperture-token-service content:*<service_slug>* level:error | sort by (_time desc) | limit 30
```

Look for: "Invalid refresh token", "invalid_grant", "Refresh token expired", HTTP 401/403 from provider.

### 3. "Connection creation failed"

```gcql
* | filter workload:aperture-token-service content:*create_connection* level:error | sort by (_time desc) | limit 30
* | filter workload:aperture-token-service content:*connect_to_data_source* level:error | sort by (_time desc) | limit 30
* | filter workload:aperture-token-service content:*AuthenticationError* | sort by (_time desc) | limit 30
```

Look for: INVALID_CREDENTIALS, TFA_REQUIRED, THIRD_PARTY_ERROR, PROFILE_SELECTION_REQUIRED.

### 4. "OAuth flow is broken"

```gcql
* | filter workload:aperture-token-service content:*callback* level:error | sort by (_time desc) | limit 30
* | filter workload:aperture-token-service content:*generate_url* level:error | sort by (_time desc) | limit 30
* | filter workload:aperture-token-service content:*CSRF* | sort by (_time desc) | limit 20
```

Look for: CSRF mismatches, expired JWTs, missing portal_redirect_url, provider errors.

### 5. "Service is returning 5xx errors"

```gcql
# Check error logs (mcp__groundcover__query_logs)
* | filter workload:aperture-token-service level:error | sort by (_time desc) | limit 50

# Check traces for 5xx (mcp__groundcover__query_traces)
* | filter service.name:aperture-token-service http.status_code:5* | sort by (_time desc) | limit 50

# Check for OOM/crashes (mcp__groundcover__query_events)
* | filter involved_object.name:*aperture-token* type:Warning | sort by (_time desc) | limit 10
```

### 6. "Token refresh is failing" (use pipelines workload!)

```gcql
# For a specific user's refresh results (PREFERRED — use structured attributes):
* | filter client_fhir_person_id:<person_id> service_slug:<slug> | sort by (_time desc) | limit 20

# For global refresh errors:
* | filter workload:aperture-token-service-pipelines level:error | sort by (_time desc) | limit 50

# For specific refresh error function:
* | filter workload:aperture-token-service-pipelines funcName:handle_token_refresh_error | sort by (_time desc) | limit 30

# For refresh results by service_slug:
* | filter service_slug:<slug> funcName:process_refresh_tokens_results | sort by (_time desc) | limit 20
```

**Known issue — Race condition in refresh**: If you see two `process_refresh_tokens_results` logs for the same user within seconds (one `Refreshed`, one `RefreshError`), this is a known race condition where two refresh calls use the same stale refresh_token. The first call succeeds and rotates the token; the second fails because the old refresh_token is now invalid. Look for timestamps within 1-5 seconds of each other.

### 7. "Device connection issues"

```gcql
* | filter workload:aperture-token-service content:*device* level:error | sort by (_time desc) | limit 30
* | filter workload:aperture-token-service content:*marketplace* level:error | sort by (_time desc) | limit 30
```

### 8. "Direct connection issues"

```gcql
* | filter workload:aperture-token-service content:*direct_connection* level:error | sort by (_time desc) | limit 30
* | filter workload:aperture-token-service content:*activate* level:error | sort by (_time desc) | limit 30
```

### 9. "FHIR/Subscription issues"

```gcql
* | filter workload:aperture-token-service content:*subscription* level:error | sort by (_time desc) | limit 30
* | filter workload:aperture-token-service content:*fhir* level:error | sort by (_time desc) | limit 30
```

### 10. "JWT/Auth issues"

```gcql
* | filter workload:aperture-token-service content:*JWT* level:error | sort by (_time desc) | limit 30
* | filter workload:aperture-token-service content:*JWKS* level:error | sort by (_time desc) | limit 30
* | filter workload:aperture-token-service content:*Forbidden* | sort by (_time desc) | limit 30
```

### 11. "OAuth flow — provider never calls back" (use traces!)

When a user initiates an OAuth connection but it never completes, the provider may not be calling back to ATS.

**Step 1: Check if getOauthUrl succeeded** (`mcp__groundcover__query_logs`, period: P7D)
```gcql
# The @commons_logging.log decorator logs getOauthUrl with funcName:wrapper
* | filter workload:aperture-token-service content:*<service_slug>* content:*generate_url* | sort by (_time desc) | limit 10
```
The decorator log has `funcName: wrapper` and the body contains `{"function": "...", "kwargs": {...}, "return": {...}}` — the `return` field shows the generated OAuth URL.

**Step 2: Check for callback traces** (`mcp__groundcover__query_traces`, period: P7D)
```gcql
# Look for ANY callback to ATS for this service_slug
* | filter service.name:aperture-token-service http.target:*callback* | sort by (_time desc) | limit 20
```
If the `resource_name` field in trace results does NOT contain the service_slug's callback within the expected time window (minutes to hours after getOauthUrl), the provider never called back.

**Step 3: Check resource_name for the authorize URL details** (`mcp__groundcover__query_traces`)
```gcql
# Traces show full URLs in resource_name — verify client_id, redirect_uri, PKCE params
* | filter service.name:aperture-token-service resource_name:*<service_slug>* | sort by (_time desc) | limit 10
```

**Step 4: Diagnosis**
- No callback trace → issue is at provider side (revoked client_id, redirect_uri mismatch, PKCE failure, user abandoned)
- Callback trace exists but with error → check logs for `handle_callback_generic` with `level:critical`
- Check if existing refresh tokens also fail → `* | filter service_slug:<slug> funcName:handle_token_refresh_error | sort by (_time desc) | limit 10`

### 12. "Who is calling this endpoint?" / "Find source of concurrent requests" (trace origin)

When you see duplicate/concurrent requests (e.g., race conditions) or unexpected API calls, trace back to the origin:

**Step 1: Get the trace ID from the log**

ATS logs include `otelTraceID` in the structured body. Find it in the log entry for the event you're investigating.

**Step 2: Find the root span** (`mcp__groundcover__query_traces`)
```gcql
# Jump directly to the root span — this is the entry point
* | filter trace_id:<otelTraceID> is_root_span:true | limit 5
```

**Step 3: Read the root span attributes**

The root span is always a traefik `EntryPoint` (kind:server). Key fields:
- `client.address` — IP of the calling pod (identifies which service/pod made the request)
- `user_agent.original` — calling framework (e.g., `Python/3.12 aiohttp/3.13.3` = another b.well service)
- `server.address` — target host (e.g., `aperture-token-service-pipelines.prod.bwell.zone`)
- `url.path` — endpoint called (e.g., `/api/v1.0/refresh-tokens`)
- `http.request.body.size` — request body size (same size from different IPs = same payload)

**Step 4: Compare origins for concurrent requests**

If two log entries for the same token have different `otelTraceID`s, find both root spans and compare:
- Different `client.address` + same `user_agent` = same external service with multiple replicas calling independently
- Same `client.address` = same caller pod made duplicate requests (possible retry logic)

**Example: Diagnosing refresh race condition**
```gcql
# 1. Find the two conflicting refresh results
* | filter client_fhir_person_id:<id> service_slug:<slug> funcName:process_refresh_tokens_results | sort by (_time desc) | limit 10

# 2. Extract otelTraceID from each (in body field), then:
* | filter trace_id:<trace_id_1> is_root_span:true | limit 5
* | filter trace_id:<trace_id_2> is_root_span:true | limit 5

# 3. Compare client.address — if different IPs, the calling service has multiple replicas
#    that aren't coordinating their refresh requests
```

**Fallback: If `is_root_span:true` returns empty**, walk up manually:
```gcql
# Filter for the workload span, then follow parent_id up
* | filter trace_id:<trace_id> workload:aperture-token-service-pipelines | sort by (_time) | limit 5
# Take the parent_id from the top-level span, then:
* | filter span_id:<parent_id> | limit 5
# Repeat until you reach parent_id:"" (the root)
```

### 13. "OAuth callback crashes with 'str' object has no attribute 'get'" (authlib bug)

This is a known bug where authlib returns a string from `fetch_access_token()` when the provider's token endpoint returns HTTP 400 with non-standard JSON body.

**Step 1: Find affected callbacks** (`mcp__groundcover__query_logs`, period: P7D)
```gcql
* | filter workload:aperture-token-service level:critical content:*Unable to authenticate request passed to callback* | sort by (_time desc) | limit 30
```

**Step 2: Parse results to identify which have the 'str' bug vs other errors**
```python
# In the body JSON, look for:
# - "error": "'str' object has no attribute 'get'" → this bug
# - "query_params": ["code", "state"] → valid callback (not provider rejection)
# Provider rejections have query_params with "error" key instead
```

**Step 3: Confirm via traces** (`mcp__groundcover__query_traces`)
```gcql
# Use otelTraceID from the error log entry
* | filter trace_id:<otelTraceID> | limit 20
```
Look for a span with `resource_name:*/token*` and check `http.status_code`. If it's 400, this confirms the bug.

**Step 4: Identify affected providers**
Group by `service_slug` in the log body to see which providers are affected.

**Root cause**: Provider returns 400 with body like `"invalid_grant"` (JSON string, not object). Authlib doesn't raise because status < 500, and `'error' in "invalid_grant"` is True for strings (substring check), but if body is e.g. `"some_opaque_value"` without "error" substring, it returns the string as the token.

---

## Time Range Patterns

| Scenario | Period Parameter |
|----------|----------------|
| Last 15 minutes | `PT15M` |
| Last hour (default) | `PT1H` |
| Last 6 hours | `PT6H` |
| Last 24 hours | `PT24H` |
| Last 7 days | `P7D` |
| Last 30 days | `P30D` |
| Specific range | Use `start` and `end` in RFC3339 format |

## Processing Large Results

When groundcover returns results too large for context, they're saved to a file. Use this Python pattern to parse:

```python
import json, sys
raw = sys.stdin.read()
data = json.loads(raw)
# Unwrap the [{type, text}] envelope
if isinstance(data, list) and len(data) > 0 and isinstance(data[0], dict) and 'text' in data[0]:
    inner = json.loads(data[0]['text'])
else:
    inner = data
for item in inner:
    ts = item.get('timestamp', '')
    level = item.get('level', '')
    env = item.get('env', '')
    try:
        body = json.loads(item.get('body', '{}'))
        msg = body.get('message', '')
        func = body.get('funcName', '')
        # Extract extra context fields
        extra_keys = [k for k in body.keys() if k not in ('lineno','funcName','pathname','message','name','taskName','otelSpanID','otelTraceID','otelTraceSampled','otelServiceName','timestamp','level')]
        print(f'{ts} [{level}] [{func}] {msg[:300]}')
    except:
        print(f'{ts} [{level}] {item.get("content", "")[:300]}')
```

## What ATS Does NOT Log (Known Gaps)

- **Extracted patient_id values** — only logs when empty, not when malformed
- **Raw token response from providers** — masked for security
- **Decrypted token values** — never logged
- **Full OAuth provider responses** — only status codes logged
- The `response_data` field stored in MongoDB contains the raw token response but is not logged
