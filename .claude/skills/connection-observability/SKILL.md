---
name: connection-observability
description: >
  Troubleshoot and investigate healthcare connection issues using groundcover observability tools. Use this skill
  whenever someone asks about ATS errors, log investigation, token refresh failures, OAuth flow problems, GraphQL
  errors, or any question related to the aperture-token-service. Also use when someone mentions connection statuses
  (EXPIRED, DISCONNECTED, ACCESS_ENDED), error codes, token refresh, or wants to investigate ATS logs in
  groundcover. Trigger even if the user just says "why is my connection expired" or "what does this ATS error
  mean" or "check ATS logs" or "what's happening with this connection".
---

# ATS Troubleshooting Guide

You are a troubleshooting expert for the **Aperture Token Service (ATS)** — a Flask-based Python microservice that manages OAuth tokens and healthcare provider connections for the b.well platform.

## How to Use This Skill

When a user asks about an ATS error, connection issue, or behavior:

1. **Understand the question** — What flow is involved? (OAuth callback, token refresh, API call, connection creation)
2. **Know the logs** — Read `references/logging-patterns.md` to know exactly which log statements exist for that flow, their `funcName`, content patterns, and structured attributes
3. **Determine the workload** — API issues → `aperture-token-service`, refresh/pipeline issues → `aperture-token-service-pipelines`
4. **Query groundcover using MCP tools** — Use the appropriate tool (see below) with structured attribute filters when possible
5. **Default time range: P7D** — Always use 7 days unless told otherwise
6. **Provide actionable guidance** — explain what happened (with log evidence), why, and how to fix

---

## Groundcover MCP Tools — Which to Use When

ATS investigation uses these groundcover MCP tools:

| Tool | Use When |
|------|----------|
| `mcp__groundcover__query_logs` | **Primary tool.** All log investigation — errors, refresh failures, OAuth issues, user activity |
| `mcp__groundcover__query_traces` | Latency issues, 5xx errors, verifying provider callbacks, finding full URLs |
| `mcp__groundcover__query_events` | Pod crashes, OOMKills, restarts, Kubernetes-level issues |
| `mcp__groundcover__query_monitors` | Checking what alerts/monitors exist for ATS |
| `mcp__groundcover__query_issues` | Active alert firings, historical alert instances |

### Tool Parameters

All query tools accept:
- **`query`** (required) — gcQL query string. Must start with `*` or a filter. Always include `| limit N`.
- **`period`** — ISO 8601 duration. Default `PT1H`. **Always set to `P7D`** for ATS investigation unless user specifies otherwise.
- **`start`** / **`end`** — RFC3339 timestamps for specific time ranges (alternative to `period`)

### Example Tool Calls

**Logs — find refresh errors for a user:**
```
tool: mcp__groundcover__query_logs
query: "* | filter client_fhir_person_id:<person_id> service_slug:<slug> | sort by (_time desc) | limit 20"
period: "P7D"
```

**Traces — find slow requests:**
```
tool: mcp__groundcover__query_traces
query: "* | filter service.name:aperture-token-service duration_seconds>5 | sort by (duration_seconds desc) | limit 20"
period: "P7D"
```

**Events — check for pod crashes:**
```
tool: mcp__groundcover__query_events
query: "* | filter involved_object.name:*aperture-token* type:Warning | sort by (_time desc) | limit 30"
period: "P7D"
```

**Monitors — check ATS alerts:**
```
tool: mcp__groundcover__query_monitors
query: "monitor_name:*aperture*"
```

---

## ATS Workloads and Log Sources

ATS has **two workloads** in groundcover:

| Workload | What It Handles | When to Query |
|----------|----------------|---------------|
| `aperture-token-service` | Main API (GraphQL, OAuth callbacks, REST endpoints) | Connection creation, OAuth flows, API errors, user-facing issues |
| `aperture-token-service-pipelines` | Batch pipelines (token refresh jobs) | Token refresh failures, scheduled job issues, EXPIRED status investigations |

---

## Log Categories — What Exists and When to Use Each

### 1. Request Lifecycle Logs (API workload)

| funcName | Content Pattern | Use When |
|----------|----------------|----------|
| `before_request` | `[BEFORE_REQUEST] Started: {method} {path} {request_id} {body}` | Seeing what API calls a user made, GraphQL operation names |
| `log_request` | `[AFTER_REQUEST] Completed: {method} {path} Status: {code} Elapsed: {time}s` | Finding slow/failed requests, response status codes |
| `wrapper` | `{"function": "...", "kwargs": {...}, "return": {...}}` | REST endpoint calls and their return values |

### 2. OAuth Callback Logs (API workload)

| funcName | Content Pattern | Level | Use When |
|----------|----------------|-------|----------|
| `handle_callback_generic` | `[HANDLE_CALLBACK_GENERIC] Error in query params` | critical | Provider rejected the OAuth request (has `query_params.error`) |
| `handle_callback_generic` | `[HANDLE_CALLBACK_GENERIC] No stored data found` | critical | State mismatch — OAuthCallback not found in MongoDB |
| `handle_callback_generic` | `[HANDLE_CALLBACK_GENERIC] Unable to authenticate request passed to callback` | critical | Token exchange failed (check `error` attribute for exception message) |
| `handle_callback_generic` | `[HANDLE_CALLBACK_GENERIC] Received request at...` | info | Successful OAuth callback |

### 3. Token Refresh Logs (Pipelines workload)

| funcName | Content Pattern | Level | Use When |
|----------|----------------|-------|----------|
| `handle_token_refresh_response` | `[REFRESH_TOKENS] Token refreshed successfully` | info | Confirming successful refreshes |
| `handle_token_refresh_error` | `[REFRESH_TOKENS] Error refreshing token, setting status to EXPIRED` | error | Token marked EXPIRED — terminal refresh failure |
| `handle_token_refresh_error` | `[REFRESH_TOKENS] Error refreshing token` | error | Transient refresh error (status unchanged, will retry) |
| `handle_token_refresh_error` | `[REFRESH_TOKENS] Token already expired` | info | Token was already EXPIRED before refresh attempt |
| `process_refresh_tokens_results` | `[REFRESH_TOKENS] Refresh token results:` | info | Summary of refresh outcome (has `code`, `status`, `prev_status`) |
| `handle_token_refresh_response` | `access_token not found in successful token refresh response` | error | Provider returned success but no access_token |
| `refresh_oauth_token` | `[REFRESH_TOKENS] Empty refresh_token but token is not disconnected` | error | Token record has no refresh_token |

### 4. Connection Hub / Config Logs (API workload)

| funcName | Content Pattern | Use When |
|----------|----------------|----------|
| `make_request_with_retries` | `Response for url={endpoint}, params={params} is status_code: {code}` | Verifying provider config fetches from Connection Hub |

### 5. Auth/JWT Logs (API workload)

| Content Pattern | Level | Use When |
|----------------|-------|----------|
| `Error in validating JWT token: Invalid key id: {kid}` | error | JWT validation failures |
| `Required items not found in auth token. Falling back to using id_token` | warning | Missing claims in JWT |

### 6. FHIR / Subscription Logs (API workload)

| funcName | Use When |
|----------|----------|
| `fetch_batch` | SubscriptionStatus lookups — checking sync state |

### 7. Kafka Event Logs (API workload)

| Content Pattern | Level | Use When |
|----------------|-------|----------|
| `Error occurred while publishing kafka event` | exception | Kafka publish failures |
| `Failed to emit kafka event on token status update` | error | Token status event failures |

---

## Structured Attributes for Direct Filtering

These fields are directly filterable in gcQL (no wildcards needed):

| Attribute | Logged By | Example Values |
|-----------|-----------|----------------|
| `client_fhir_person_id` | refresh results, callback errors | UUID |
| `service_slug` | refresh results, callback errors | provider identifier string |
| `member_id` | refresh results | UUID |
| `bwell_fhir_person_id` | refresh results | UUID |
| `funcName` | all structured logs | function name |
| `code` | process_refresh_tokens_results | Refreshed, RefreshError, ClientRefreshError, etc. |
| `status` | refresh results | New, Data Retrieved, Expired, etc. |
| `prev_status` | refresh results | same values |
| `connection_version` | refresh/callback errors | integer |
| `interop_type` | refresh errors | hapi, oauth, etc. |
| `query_params.error` | callback errors | access_denied, server_error |
| `query_params.error_description` | callback errors | Provider-specific message |

---

## Groundcover Query Patterns

### Query Rules (learned from production use)

- **Always set `period: "P7D"`** in tool calls (7 days) unless user specifies otherwise
- Use structured attribute filters — more reliable than content wildcards
- Use `level:error` field filter, NOT `content:*error*`
- Keep to 1-2 `content:*...*` filters max — 3+ often cause query failures
- If a query fails, simplify and filter results programmatically
- Refresh logs come from `aperture-token-service-pipelines`, NOT the main API workload
- When using attribute filters like `client_fhir_person_id:`, omit the workload filter (attributes work across both)

### Essential Queries by Scenario (all use `mcp__groundcover__query_logs` with `period: "P7D"`)

**All activity for a user (structured attributes — PREFERRED):**
```
* | filter client_fhir_person_id:<person_id> | sort by (_time desc) | limit 30
```

**All activity for a user (API-level — catches GraphQL/callbacks):**
```
* | filter workload:aperture-token-service content:*<client_person_id>* | sort by (_time desc) | limit 30
```

**Token refresh results for a user:**
```
* | filter client_fhir_person_id:<person_id> service_slug:<slug> | sort by (_time desc) | limit 20
```

**Refresh failures (terminal — token marked EXPIRED):**
```
* | filter service_slug:<slug> funcName:handle_token_refresh_error | sort by (_time desc) | limit 20
```

**OAuth callback errors:**
```
* | filter workload:aperture-token-service content:*callback* level:error | sort by (_time desc) | limit 30
```

**Recent API errors:**
```
* | filter workload:aperture-token-service level:error | sort by (_time desc) | limit 50
```

**Recent pipeline errors:**
```
* | filter workload:aperture-token-service-pipelines level:error | sort by (_time desc) | limit 50
```

**Error rate overview:**
```
* | filter workload:aperture-token-service | stats by (level) count() | limit 10
```

### Trace Queries (use `mcp__groundcover__query_traces` with `period: "P7D"`)

Service name: `aperture-token-service`

```
# 5xx errors
* | filter service.name:aperture-token-service http.status_code:5* | sort by (_time desc) | limit 50

# Slow requests
* | filter service.name:aperture-token-service duration_seconds>5 | sort by (duration_seconds desc) | limit 20

# OAuth callbacks (verify provider called back)
* | filter service.name:aperture-token-service http.target:*callback* | sort by (_time desc) | limit 20

# Generate URL calls
* | filter service.name:aperture-token-service http.target:*generate_url* | sort by (_time desc) | limit 20
```

Key trace field: `resource_name` contains the **full URL with query parameters** — critical for OAuth debugging (shows client_id, redirect_uri, scopes, PKCE params).

### Event Queries (use `mcp__groundcover__query_events` with `period: "P7D"`)

```
# Pod crashes, OOMs, restarts
* | filter involved_object.name:*aperture-token* type:Warning | sort by (_time desc) | limit 30
```

### Tracing Request Origins (Who Called This Endpoint?)

Use `otelTraceID` from logs → query traces with `* | filter trace_id:<id> is_root_span:true | limit 5` → root span has `client.address` (caller IP), `user_agent.original`, and `url.path`.

---

## Architecture Quick Reference

| Layer | Key Files |
|-------|-----------|
| Entry points | `autoapp.py`, `wsgi.py` |
| App factory | `aperture_token_service/app.py` |
| Settings | `aperture_token_service/settings.py` |
| OAuth flow | `aperture_token_service/oauth/views.py`, `interactor.py`, `utils.py` |
| Token CRUD | `aperture_token_service/token/views.py`, `interactor.py` |
| GraphQL | `aperture_token_service/graphql/schema.py`, `token/graphql/resolvers.py` |
| Error handling | `aperture_token_service/graphql/error_handler.py`, `token_service_exception.py` |
| Connection logic | `aperture_token_service/token/interactor.py`, `oauth/hapi_interactor.py` |
| Direct connections | `aperture_token_service/token/direct_connection/` |
| Device connections | `aperture_token_service/oauth/device_interactor.py` |
| Token refresh | `aperture_token_service/oauth/utils.py` (lines ~800+) |
| Constants | `aperture_token_service/commons/constants.py`, `oauth/constants.py` |
| Models | `aperture_token_service/token/models.py`, `oauth/models.py` |
| FHIR subscriptions | `aperture_token_service/commons/subscription_util.py` |
| Kafka events | `aperture_token_service/commons/utils.py` |

---

## Error Codes Quick Reference

### GraphQL Errors

| Exception Class | HTTP Status | FHIR Code | When It Occurs |
|----------------|-------------|-----------|----------------|
| `BadValueError` | 400 | `value` | Invalid input |
| `AuthorizationError` | 401 | `security` | Invalid credentials, expired JWT |
| `ForbiddenError` | 403 | `forbidden` | Access denied |
| `NotFoundError` | 404 | `not-found` | Resource not found |
| `InternalServerError` | 500 | `exception` | Server error, third-party failure |
| `GatewayTimeoutError` | 504 | `timeout` | External provider timeout |

### Token Refresh Process Codes

| Code | Meaning | Status Change |
|------|---------|---------------|
| `REFRESHED` | Success | Token updated |
| `REFRESH_ERROR` | Provider rejected refresh | Token marked EXPIRED |
| `CLIENT_REFRESH_ERROR` | Transient provider error | Status unchanged (retry later) |
| `REFRESH_NOT_REQUIRED` | Token not yet expired | No action |
| `MISSING_REFRESH_TOKEN` | No refresh_token stored | Skip |
| `MISSING_CONNECTION_DATA` | Token lacks required fields | Skip |

### Error Messages That Trigger EXPIRED Status

When provider returns these during refresh → token marked EXPIRED:
- "Invalid refresh token" / "invalid_grant" / "Invalid grant"
- "Invalid Credentials" / "Invalid authorization"
- "The refresh token is invalid or has expired" / "Refresh token expired"
- "Invalid or expired refresh token" / "The refresh token is no longer active"
- "unknown, invalid, or expired refresh token"
- "User data access grant expired" / "Authentication Failed" / "Patient ID not found"

HTTP 400, 401, 403 from provider also trigger EXPIRED.

---

## Connection Statuses

| Token Status | User-Facing Status | Meaning |
|-------------|-------------------|---------|
| `NEW` | CONNECTED | Just created |
| `RETRIEVING_DATA` | CONNECTED | Syncing |
| `DATA_RETRIEVED` | CONNECTED | Data synced |
| `EXPIRED` | EXPIRED | Token expired |
| `DISCONNECTED` | DISCONNECTED | User disconnected |
| `ACCESS_ENDED` | ACCESS_ENDED | Consent revoked |
| `DELETED` | DELETED | User deleted |
| `DATA_DELETED` | DELETED | Data deletion done |

---

## Debugging Checklist

1. **Check logs** — Use groundcover with structured attributes first, fall back to content search
2. **Check traces** — For latency, 5xx, or "did the provider call back?" questions
3. **Check events** — For pod crashes, OOMs, restarts
4. **Check token status in MongoDB** — `Token` collection stores connection state (AES-encrypted fields)
5. **Check FHIR resources** — SubscriptionStatus tracks sync state
6. **Check Connection Hub** — Provider config (client_id, URLs, scopes) from external config service
7. **Check environment** — Key env vars: `MONGO_*`, `KAFKA_*`, JWT JWKS URIs, `FHIR_*`, `AES_SECRET_KEY`

---

## Further Reading

For deeper dives, read these reference files:
- `references/logging-patterns.md` — Complete map of all ATS log statements with funcNames, content patterns, structured attributes, and workloads
- `references/groundcover-queries.md` — gcQL query patterns, gotchas, parsing large results, and investigation workflows
- `references/error-codes.md` — Complete error code reference with source file locations
- `references/connection-flows.md` — Detailed connection flow diagrams and state machines
