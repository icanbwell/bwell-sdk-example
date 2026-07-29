# ATS Logging Patterns — Complete Reference

## Log Architecture

ATS has two workloads that emit logs to groundcover:
- **`aperture-token-service`** — main Flask API (handles requests, callbacks, GraphQL)
- **`aperture-token-service-pipelines`** — batch pipeline (token refresh, scheduled jobs)

## Log Structure

Every log entry in groundcover has:
- `timestamp` — when it was emitted
- `level` — info, error, warning, critical
- `workload` — which deployment emitted it
- `content` — the log message text (searchable with `content:*...*`)
- `body` — full JSON-structured log (parse for details)
- `string_attributes` — structured extra fields (directly filterable!)
- `float_attributes` — numeric extra fields

## Logging Layers (in order of request processing)

### 1. WSGI Middleware (`middleware.py`)
- **What**: Logs raw environ dict for every non-excluded request
- **Level**: info
- **funcName**: `_log`
- **Content pattern**: Raw WSGI environ (HTTP headers, method, path)
- **Not useful for debugging** — too low-level, no structured attributes

### 2. Before/After Request Interceptor (`app.py:261-289`)
- **What**: Logs start/end of every HTTP request with timing

**BEFORE_REQUEST** (line 268):
- **Content**: `[BEFORE_REQUEST] Started: {method} {path} {request_id} {request_body}`
- **funcName**: `before_request`
- **Structured attributes**: None (just content)
- **Very useful**: Shows GraphQL operation names in body, REST endpoint paths, request IDs

**AFTER_REQUEST** (line 282):
- **Content**: `[AFTER_REQUEST] Completed: {method} {path} {request_id} Status: {status_code} Elapsed: {time}s`
- **funcName**: `log_request`
- **Very useful**: Shows response status codes and request duration

### 3. `@commons_logging.log(logger)` Decorator (`commons/logging.py`)
- **What**: Wraps REST view functions, logs function path + optional args/return
- **funcName**: `wrapper`
- **Content**: A dict like `{"function": "aperture_token_service.token.views.get_all_tokens", "kwargs": {...}, "return": {...}}`
- **Applied to**: Most REST endpoints in `token/views.py`
- **Useful for**: Seeing which REST endpoint was called and what it returned (masked)

### 4. `make_request_with_retries` (`oauth/utils.py:140`)
- **What**: All requests to Connection Hub (integration hub)
- **funcName**: `make_request_with_retries`
- **Content**: `Response for url={endpoint}, params={params} is status_code: {code}`
- **Level**: info (success), info (retry/error)
- **Useful for**: Verifying Connection Hub config fetches work

### 5. OAuth Flow Logs (`oauth/views.py`, `oauth/interactor.py`)

**Callback Error — Provider rejection** (line 273):
- **funcName**: `handle_callback_generic`
- **Content**: `[HANDLE_CALLBACK_GENERIC] Error in query params`
- **Level**: critical
- **Structured attributes**: `client_fhir_person_id`, `member_id`, `service_slug`, `query_params.error`, `query_params.error_description`
- **When**: Provider redirects back with `?error=...`

**Callback Error — No stored data** (line 259):
- **Content**: `[HANDLE_CALLBACK_GENERIC] No stored data found for the request`
- **Level**: critical
- **When**: State parameter doesn't match any OAuthCallback in MongoDB

**Callback Error — Login failure** (line 311):
- **Content**: `[HANDLE_CALLBACK_GENERIC] Unable to authenticate request passed to callback`
- **Level**: critical
- **Structured attributes**: `service_slug`, `connection_version`, `bwell_fhir_person_id`, `client_fhir_person_id`, `query_params` (list of param names), `error` (exception message)
- **When**: `handle_login()` raises any exception. Common case: `'str' object has no attribute 'get'` — means authlib returned a string instead of dict from token exchange (provider returned 400 with non-standard body). Check trace for POST to token endpoint status code.
- **Distinguishing from provider rejections**: `query_params` contains `['code', 'state']` (valid callback with code) vs provider rejections where `query_params` has `error` key

**Callback Success** (line 283):
- **Content**: `[HANDLE_CALLBACK_GENERIC] Received request at {url} for client_person: {id}, member_id: {id}, service_slug: {slug}`
- **Level**: info
- **funcName**: `handle_callback_generic`

**Legacy callback** (line 167):
- **Content**: `[HANDLE_CALLBACK] OAuth callback data not found`
- **Level**: error

**Token exchange** (line 850-863 in interactor.py):
- **Content**: `[OAUTH_INTERACTOR] POST Request to {token_url}` and `[OAUTH_INTERACTOR] {status} response with content...`
- **Level**: info
- **Only for**: `grant_type == "authorization_code"` (not refresh)

**OAuth callback metadata failure** (interactor.py:126):
- **Content**: `[OAUTH_CALLBACK] Failed to upsert OAuth callback metadata`
- **Level**: error
- **Structured attributes**: `service_slug`, `bwell_fhir_person_id`, `client_fhir_person_id`, `category`

### 6. Token Refresh Logs (`oauth/utils.py`)

**All refresh logs use structured `extra` fields that become `string_attributes` in groundcover.**

**Refresh success** (line 710):
- **funcName**: `handle_token_refresh_response`
- **Content**: `[REFRESH_TOKENS] Token refreshed successfully for user`
- **Level**: info
- **Attributes**: `member_id`, `service_slug`, `bwell_fhir_person_id`, `client_fhir_person_id`, `status`, `prev_status`, `connection_version`

**Refresh error — terminal (token marked EXPIRED)** (line 872):
- **funcName**: `handle_token_refresh_error`
- **Content**: `[REFRESH_TOKENS] Error refreshing token, setting status to EXPIRED`
- **Level**: error (with exception traceback)
- **Attributes**: `exception`, `member_id`, `bwell_fhir_person_id`, `client_fhir_person_id`, `interop_type`, `service_slug`, `connection_version`, `error` (response text), `prev_status`

**Refresh error — already expired** (line 867):
- **funcName**: `handle_token_refresh_error`
- **Content**: `[REFRESH_TOKENS] Token already expired`
- **Level**: info
- **Attributes**: same as above

**Refresh error — transient (status unchanged)** (line 898):
- **funcName**: `handle_token_refresh_error`
- **Content**: `[REFRESH_TOKENS] Error refreshing token`
- **Level**: error (with exception traceback)
- **Attributes**: same as above
- **Code**: `CLIENT_REFRESH_ERROR`

**Refresh result summary** (line 1108):
- **funcName**: `process_refresh_tokens_results`
- **Content**: `[REFRESH_TOKENS] Refresh token results:`
- **Level**: info
- **Attributes**: `code`, `service_slug`, `member_id`, `bwell_fhir_person_id`, `client_fhir_person_id`, `status`, `prev_status`
- **Code values**: Refreshed, RefreshError, RefreshNotRequired, ClientRefreshError, MissingRefreshToken, MissingConnectionData

**Missing access_token in response** (line 726):
- **funcName**: `handle_token_refresh_response`
- **Content**: `[REFRESH_TOKENS] access_token not found in successful token refresh response, setting status to EXPIRED`
- **Level**: error
- **Attributes**: includes `token_response` dict

**Empty refresh_token** (line 980):
- **funcName**: `refresh_oauth_token`
- **Content**: `[REFRESH_TOKENS] Empty refresh_token but token is not disconnected`
- **Level**: error
- **Attributes**: `service_slug`, `bwell_fhir_person_id`, `client_fhir_person_id`, `connection_version`, `token_status`

**Unable to refresh expired non-disconnected token** (in refresh_tokens function):
- **funcName**: `refresh_tokens`
- **Content**: `[REFRESH_TOKENS] Unable to refresh expired token which is not disconnected`
- **Level**: error

### 7. Patient ID Extraction (`oauth/utils.py:1530+`)

**Patient ID empty** (line ~1545):
- **Content**: `[EXTRACT_PATIENT_ID] Unable to get patient_id from token response`
- **Level**: error
- **Note**: Only logs when patient_id is EMPTY, not when malformed

### 8. GraphQL Resolver Logs

**get_oauth_url** (oauth/graphql/resolvers.py):
- Errors at levels: warning (Kafka event fail), critical (portal URL fetch fail, portal URL not configured, URL generation fail, invalid response)
- **Content patterns**: `get_oauth_url: Failed to...`, `get_oauth_url: Portal redirect URL not configured...`

**get_member_connections** (token/graphql/resolvers.py):
- Errors: warning (invalid status filters), critical (DB fetch fail, subscription status batch fail, token mapping fail)
- **Content patterns**: `get_member_connections: ...`

### 9. Auth/JWT Validation (`oauth/auth_handler.py`)

- **Content**: `Error in validating JWT token: Invalid key id: {kid}`
- **Level**: error
- **Content**: `Required items not found in auth token. Falling back to using id_token`
- **Level**: warning

### 10. HTTP Exception Handler (`commons/utils.py:393`)

- **Content**: `Timeout received during request` (for timeouts)
- **Content**: `Invalid status_code received during request` (for HTTP errors)
- **Level**: error
- **Attributes**: `status_code`, `response_error`

### 11. Kafka Event Publishing (`commons/utils.py`)

- **Content**: `Error occurred while publishing kafka event`
- **Level**: exception (with traceback)
- **Content**: `Failed to emit kafka event on token status update for service_slug: {slug}`

### 12. Device Connection Logs (`oauth/device_interactor.py`)

Has 31 log statements. Key patterns:
- Device connection creation/disconnection errors
- Marketplace state validation
- Device token storage

### 13. HAPI/Clinical Connection Logs (`oauth/hapi_interactor.py`)

12 log statements covering:
- connect_to_data_source errors
- HAPI token refresh
- Connection code mapping

### 14. Direct Connection Logs (`token/direct_connection/`)

- Direct connection provider-specific logging
- FHIR Subscription resource creation

### 15. Subscription Status Helper (`commons/subscription_status_service_helper.py`)

- **funcName**: `fetch_batch`
- Logs FHIR SubscriptionStatus fetch results

## Traces in Groundcover

ATS is instrumented with OpenTelemetry. Traces are available via `mcp__groundcover__query_traces`.

**Service name**: `aperture-token-service`

Key trace attributes:
- `http.status_code` — response status
- `http.method` — GET, POST, etc.
- `http.url` / `http.target` — the endpoint path
- `resource_name` — **IMPORTANT**: contains the FULL URL including query parameters (e.g., `GET https://provider.example.com/authorize?client_id=...&redirect_uri=...&code_challenge=...`)
- `duration_seconds` — request duration

### Trace Investigation Patterns

**OAuth flow investigation using traces:**

The `resource_name` field is critical for OAuth debugging because it shows the full authorize URL with all params (client_id, redirect_uri, scopes, PKCE code_challenge). This helps verify:
- Correct client_id being sent
- Correct redirect_uri (must match provider's registered URI)
- Correct scopes requested
- PKCE parameters present (for providers that require it)

```gcql
# Find slow requests
* | filter service.name:aperture-token-service duration_seconds>5 | sort by (duration_seconds desc) | limit 20

# Find 5xx errors
* | filter service.name:aperture-token-service http.status_code:5* | sort by (_time desc) | limit 50

# Find specific endpoint issues
* | filter service.name:aperture-token-service http.target:/graphql http.status_code:5* | sort by (_time desc) | limit 20

# Find callback traces (verify provider called back)
* | filter service.name:aperture-token-service http.target:*callback* | sort by (_time desc) | limit 20

# Find OAuth URL generation traces
* | filter service.name:aperture-token-service http.target:*generate_url* | sort by (_time desc) | limit 20

# Find callbacks for a specific service_slug (resource_name contains full URL with query params)
* | filter service.name:aperture-token-service resource_name:*<service_slug>* http.target:*callback* | sort by (_time desc) | limit 20
```

### Diagnosing "Provider Never Called Back"

When investigating OAuth connections that don't complete:

1. **Check if getOauthUrl succeeded** — look for `funcName:wrapper` logs with `getOauthUrl` in the content, or traces with `http.target:*generate_url*`
2. **Check for callback traces** — query traces with `http.target:*callback*` in the same time window
3. **If no callback trace exists** — the issue is at the provider side:
   - Provider may have revoked the client_id
   - redirect_uri mismatch in provider's config
   - PKCE verification failure
   - Provider internal error
   - User abandoned the flow at the provider's login page
4. **Check the `resource_name` from getOauthUrl trace** — it shows the full authorize URL sent to the provider, which reveals what client_id, redirect_uri, and scopes were used

### Tracing Request Origins (Who Called This Endpoint?)

When you need to find what service or client triggered a request to ATS (e.g., concurrent refresh calls, unexpected API usage):

**Step 1: Get the `otelTraceID` from the log entry**

ATS logs include `otelTraceID` in structured body. Use it to query traces:
```gcql
* | filter trace_id:<otelTraceID> is_root_span:true | limit 5
```

**Step 2: The root span reveals the caller**

The root span (where `is_root_span:true` and `parent_id:""`) is always the **traefik-internal EntryPoint** — it contains:
- `client.address` — IP of the calling pod/service
- `user_agent.original` — framework used by caller (e.g., `Python/3.12 aiohttp/3.13.3`)
- `server.address` — target hostname (e.g., `aperture-token-service-pipelines.prod.bwell.zone`)
- `url.path` — the endpoint called
- `http.request.body.size` — size of the request body
- `http.request.method` — HTTP method

**Step 3: Identify the calling service from client IP**

The `client.address` is the pod IP of the caller. To identify which service it belongs to:
- Different client IPs for the same endpoint = multiple replicas of the calling service
- Same body size + same endpoint from different IPs = coordinated (or uncoordinated) batch calls

**Key insight**: If `is_root_span:true` filter returns empty, walk the parent chain manually:
```gcql
* | filter span_id:<parent_id_from_previous_span> | limit 5
```

**Traefik span hierarchy** (from innermost to outermost):
```
ATS handler span (workload:aperture-token-service-pipelines)
  └─ ReverseProxy (kind:client, traefik → ATS pod)
       └─ Metrics (kind:internal)
            └─ Service (kind:internal, has traefik.service.name)
                 └─ Headers (kind:internal)
                      └─ Router (kind:internal, has http.route with Host matcher)
                           └─ EntryPoint [ROOT] (kind:server, has client.address + user_agent)
```

**Example: Finding source of concurrent refresh requests**
```gcql
# 1. From logs, get the otelTraceID of each refresh
* | filter client_fhir_person_id:<id> service_slug:<slug> funcName:process_refresh_tokens_results | sort by (_time desc) | limit 10

# 2. For each trace ID, find the root span
* | filter trace_id:<trace_id> is_root_span:true | limit 5

# 3. Compare client.address values — different IPs = different caller pods
# Same user_agent + different client.address = same service, multiple replicas
```

## Query Strategy by Issue Type

| Issue Type | Primary Query | Workload | Key funcName |
|-----------|---------------|----------|--------------|
| User can't connect (OAuth) | `client_fhir_person_id:<id>` or `content:*<person_id>*` | aperture-token-service | `handle_callback_generic` |
| Token refresh failing | `client_fhir_person_id:<id> service_slug:<slug>` | aperture-token-service-pipelines | `process_refresh_tokens_results`, `handle_token_refresh_error` |
| API errors | `workload:aperture-token-service level:error` | aperture-token-service | varies |
| Slow requests | Use traces: `service.name:aperture-token-service duration_seconds>5` | — | — |
| Connection Hub config issues | `content:*make_request_with_retries*` or `funcName:make_request_with_retries` | aperture-token-service | `make_request_with_retries` |
| GraphQL errors | `content:*BEFORE_REQUEST*` + `content:*graphql*` | aperture-token-service | `before_request` |
| JWT/Auth failures | `content:*validating JWT*` or `content:*Forbidden*` | aperture-token-service | auth_handler |
| Kafka publish failures | `content:*kafka*` level:error | aperture-token-service | — |

## Structured Attributes Available for Direct Filtering

These can be used as `attribute_name:value` in gcQL filters (no wildcards needed):

| Attribute | Logged By | Values |
|-----------|-----------|--------|
| `client_fhir_person_id` | refresh results, callback errors, refresh errors | UUID |
| `service_slug` | refresh results, callback errors, refresh errors | provider identifier string |
| `member_id` | refresh results, refresh errors | UUID |
| `bwell_fhir_person_id` | refresh results, refresh errors | UUID |
| `funcName` | all structured logs | function name string |
| `code` | process_refresh_tokens_results | Refreshed, RefreshError, RefreshNotRequired, etc. |
| `status` | refresh results | New, Data Retrieved, Expired, etc. |
| `prev_status` | refresh results | same values |
| `connection_version` | refresh errors, callback errors | integer |
| `interop_type` | refresh errors | hapi, oauth, etc. |
| `query_params.error` | callback errors | access_denied, server_error |
| `query_params.error_description` | callback errors | Provider-specific message |
