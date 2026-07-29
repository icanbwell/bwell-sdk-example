# ATS Error Codes — Complete Reference

## Source File Locations

| File | What It Contains |
|------|-----------------|
| `aperture_token_service/graphql/error_handler.py` | GraphQL error classes, OperationOutcomeCode enum, ATSErrorCode enum |
| `aperture_token_service/commons/constants.py` | Error message constants, HTTP exception tuple |
| `aperture_token_service/oauth/constants.py` | ConnectionCodes, DeviceConnectionCodes, RefreshProcessCodes |
| `aperture_token_service/token/status.py` | Token Status enum |
| `aperture_token_service/token_service_exception.py` | Base TokenServiceException class |
| `aperture_token_service/oauth/jwt_validator.py` | JwtValidatorException |
| `aperture_token_service/oauth/jwks_validation.py` | JwksUrlValidationError |

## OperationOutcome Response Format

GraphQL errors return FHIR OperationOutcome:

```json
{
  "resourceType": "OperationOutcome",
  "issue": [
    {
      "severity": "error",
      "code": "<fhir-code>",
      "details": {
        "text": "<error message>",
        "coding": [
          {
            "code": "<ATS_ERROR_CODE>",
            "display": "<display text>"
          }
        ]
      }
    }
  ]
}
```

## HTTP Status to FHIR Code Mapping

```python
OPERATION_OUTCOME_CODE_MAP = {
    400: "value",
    401: "security",
    403: "forbidden",
    404: "not-found",
    500: "exception",
    504: "timeout",
}
```

## Token Refresh Error Patterns

The function `handle_token_refresh_error()` in `oauth/utils.py` determines whether to mark a token as EXPIRED based on:

1. **HTTP Status**: 400, 401, 403 → EXPIRED
2. **Error message contains** (case-insensitive matching on response body):
   - "Invalid refresh token"
   - "invalid_grant"
   - "Invalid grant"
   - "Invalid Credentials"
   - "Invalid authorization"
   - "The refresh token is invalid or has expired"
   - "Refresh token expired"
   - "Invalid or expired refresh token"
   - "The refresh token is no longer active"
   - "refresh token is invalid"
   - "unknown, invalid, or expired refresh token"
   - "User data access grant expired"
   - "Authentication Failed"
   - "Patient ID not found"

When these patterns match → `RefreshProcessCodes.REFRESH_ERROR` → Token status set to EXPIRED.

For other errors (e.g., 500 from provider, network timeout) → `RefreshProcessCodes.CLIENT_REFRESH_ERROR` → Token status unchanged (will retry on next refresh cycle).

## HTTP Exception Handling

The unified handler `commons/utils.py::handle_http_exception()`:

- `requests.exceptions.Timeout` / `httpx.TimeoutException` → logs timeout, returns 504
- `requests.HTTPError` / `httpx.HTTPStatusError` → logs with status code and response body, returns the status code
- All other `requests.RequestException` / `httpx.HTTPError` → logs generic error

## Flask Error Handlers (REST API)

Registered in `app.py`:
- 401 → renders `401.html` template
- 404 → renders `404.html` template
- 500 → renders `500.html` template
- `TokenServiceException` → renders `token_service_exception.html` with error details, returns 500
