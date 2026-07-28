# ATS Connection Flows — Detailed Reference

## Connection Creation Flows

### 1. OAuth Connection (INDIRECT_IAS / PROA)

**Entry point:** `GET /api/v1.0/oauth/generate_url/<service_slug>/[<category>]`

```
Client → generate_url (JWT required)
  → Fetch provider config from Connection Hub
  → Create OAuthCallback in MongoDB (state, csrf, jwt, metadata)
  → Return authorize URL

Client → Opens authorize URL at provider
Provider → Redirects to callback with ?state=...&code=...

ATS callback:
  → Decode base64 state → { csrf_token, id }
  → Load OAuthCallback by id (and delete it)
  → Verify CSRF + JWT
  → Exchange code for tokens at provider's token_url
  → Encrypt and store Token in MongoDB
  → Create FHIR Subscription resources
  → Emit TOKEN_CREATED Kafka event
  → Redirect to portal with status_code
```

### 2. HAPI Clinical Connection (GraphQL)

**Entry point:** GraphQL mutation `create_connection(connection_id, username, password)`

```
GraphQL resolver → create_connection
  → Determine integration_type from Connection Hub config
  → If DIRECT → delegate to create_direct_connection()
  → Otherwise → delegate to hapi_interactor.connect_to_data_source()

connect_to_data_source():
  → Get human_id from third-party integrator
  → Call HAPI with credentials (username/password)
  → Get connection result code (Success, AuthError, TFA, etc.)
  → If Success:
    → Store encrypted token
    → Create FHIR subscription resources
    → Emit TOKEN_CREATED Kafka event
    → Return Connection object
  → If Error:
    → Map HumanAPIConnectionCode to appropriate GraphQL error
    → Raise error (AuthorizationError, BadValueError, etc.)
```

### 3. Direct Connection

**Entry point:** GraphQL mutation `create_connection(connection_id)` where connection_id matches a registered direct provider

```
GraphQL resolver → create_connection
  → Detect DIRECT integration_type
  → create_direct_connection(connection_id, member context)
    → Look up DirectConnectionProvider by connection_id
    → provider.process():
      → Create FHIR Subscription resource
      → Create FHIR SubscriptionTopic resource
      → Create FHIR SubscriptionStatus resource (with extensions for metadata)
      → Emit RESOLVE_PATIENT Kafka event
    → Return DirectConnectionResult(status=CONNECTED, sync_status=PENDING)
```

### 4. Device Connection

**Entry point:** `GET /api/v1.0/oauth/generate_url/<service_slug>/device`

```
Client → generate_url with category=device
  → Fetch device marketplace config
  → Generate signed state with HMAC
  → Build marketplace URL with state
  → Return marketplace URL

User → Connects device in marketplace
Marketplace → Redirects to ATS callback

ATS device callback:
  → Validate state signature (HMAC)
  → Validate state timestamp (not expired, not future)
  → Extract user context from state
  → Store device connection token
  → Emit TOKEN_CREATED event
```

## Connection Disconnection Flow

**Entry points:**
- GraphQL: `disconnect_connection(connection_id)`
- REST: `POST /token/disconnect_user_token/<service_slug>`

```
disconnect_token():
  → Load token by connection_id/service_slug + member context
  → Set status = DISCONNECTED
  → Clear refresh_token and token fields
  → For device connections only: attempt revoke at provider (best-effort)
  → Emit CONNECTION_STATUS Kafka event
  → Return updated status
```

## Connection Deletion Flow

**Entry points:**
- GraphQL: `delete_connection(connection_id)`
- REST: `DELETE /token/delete_user_token/<service_slug>`

```
request_delete_user_token():
  → Load token
  → For device connections: call _revoke_device_connection() (best-effort)
  → Set status = DELETED
  → Clear token fields
  → Emit DATA_CONNECTION_DELETED Kafka event
  → Update FHIR SubscriptionStatus
  → Return deleted status
```

## Direct Connection Activation

**Entry point:** GraphQL mutation `activate_direct_connection(connection_id)`

```
activate_direct_connection():
  → Load SubscriptionStatus from FHIR
  → If not found → raise NotFoundError
  → Check if in DELETING state → raise BadValueError ("deletion in progress")
  → Check FHIR Consent resource for deny status → raise BadValueError ("consent revoked")
  → Update SubscriptionStatus: token-status = CONNECTED
  → If was DATA_DELETED: reset data-connection-status
  → Emit DIRECT_CONNECTION_REACTIVATED Kafka event
  → Return activation confirmation
```

## Token Refresh Flow

**Trigger:** Periodic refresh via `start_refresh_tokens()` or CLI command

```
start_refresh_tokens():
  → Query tokens where:
    - status NOT IN (DISCONNECTED, DELETED, DATA_DELETED)
    - has refresh_token (non-empty)
    - expiry approaching or expired
  → For each token:
    → refresh_oauth_token():
      → Build refresh request (refresh_token, client_id, client_secret)
      → POST to provider's token_url
      → If success (handle_token_refresh_response):
        → Update access_token
        → Update refresh_token (if new one provided)
        → Recalculate expiry
        → Emit TOKEN_REFRESHED event
      → If error (handle_token_refresh_error):
        → Check error message/status against known patterns
        → If terminal error → mark EXPIRED (REFRESH_ERROR)
        → If transient error → leave status (CLIENT_REFRESH_ERROR)
```

## State Transitions

```
NEW → RETRIEVING_DATA → DATA_RETRIEVED (happy path)
     ↗                    ↓
Any status → EXPIRED (refresh failure)
Any status → DISCONNECTED (user action)
Any status → DELETED → DATA_DELETED (user action)
Any status → ACCESS_ENDED (consent revoked)
DISCONNECTED/EXPIRED → reconnect via new OAuth flow → NEW
DELETED (direct) → activate_direct_connection → CONNECTED (if consent valid)
```

## Key Database Fields (Token Model)

| Field | Description |
|-------|-------------|
| `member_id` | Member identifier |
| `client_fhir_person_id` | Client's FHIR Person ID |
| `bwell_fhir_person_id` | b.well's FHIR Person ID |
| `ch_service_id` / `service_slug` | Connection identifier |
| `token` | Encrypted access token |
| `refresh_token` | Encrypted refresh token |
| `token_expiry` | Token expiration timestamp |
| `status` | Current connection status |
| `fhir_url` | Provider's FHIR endpoint |
| `fhir_version` | FHIR version (R4, etc.) |
| `integration_type` | DIRECT, PROA, INDIRECT_IAS, IAL2 |
| `interop_type` | Interop classification |
| `source_id_prefix` | Namespace for FHIR resources |
| `categories` | Data source categories |
