# b.well TypeScript SDK Sample App

This is a sample TypeScript Web application meant to be used in testing the b.well TypeScript SDK.

It is built with react, redux + redux toolkit, vite, and material-ui.

## System Requirements
- Node 22 (or higher)
  - Install via [nvm](https://github.com/nvm-sh/nvm) or direct from [node's site](https://nodejs.org/).
 
## How to run it

1. `npm i` (if you haven't run it before)
2. `npm run dev`

## How to run the tests

1. `npm run e2e`

A .env file will need to be present in this directory. See .env.example for the keys that will need to be supported.

### Docker Devcontainer

It is also recommended that you run tests in a container to ensure consistent execution across environments. If you are using VS Code, follow these steps:

1. Navigate into this folder (bwell-typescript-react)
2. Click the little green button in the lower-left corner of your screen
3. Select "Reopen in container"
4. You are now running in a Docker container. `npm run e2e` should run the tests, and a report will open in your web browser once the tests finish running.

See also: https://playwright.dev/docs/docker

## Notes

* On Health Data pages, clicking a row in a "Groups" grid will filter the rows shown in the second grid. For example, clicking "Cashew Nuts" under "Allergy Intolerance Groups" should filter the "Allergy Intolerances" grid to show only cashew-related incidents.
* On Lab and Medication pages, it should be possible to view "Knowlege" associated with a given detail row. Clicking on a row under "Labs" or "Medications" (not their corresponding Groups) will cause the "Knowledge" associated with that row to be retrieved and displayed, if available.
* Command-clicking on a selected row in one of the grids will deselect it.
* Global application state is automatically saved to local storage and rehydrated at the start of the session; this means the application will recall its state when you return to it, including page number and HTML/JSON toggle status for each grid.

---

## SDK Features

This section documents how each SDK feature works in this app — including real request/response shapes, common pitfalls, and debugging tips. The goal is to get you to a working implementation in under 10 minutes per feature.

All SDK calls in this app go through a wrapper layer in `src/sdk/`. **Always use these wrappers** rather than calling `sdk.user.*` directly — they normalize inconsistent SDK response shapes into a predictable interface.

---

### Consents

#### Overview

The Consents feature lets you fetch a user's existing consent records and create new ones. A consent represents a user's agreement (or denial) to a specific data-sharing policy, identified by a category (e.g., `TOS`) and a provision (`PERMIT` or `DENY`).

**Key files:**
- `src/sdk/consents.ts` — SDK wrapper functions and response normalizer
- `src/pages/Consents.tsx` — UI component

#### Prerequisites

- The SDK must be initialized before any consent call. If `getSdk()` returns `null`, all calls fail.
- The user must be authenticated. Consent calls are user-scoped and will fail or return empty without a valid session.

```ts
const sdk = getSdk();
if (!sdk) {
  throw new Error("SDK not initialized. Check your bWellSdk setup.");
}
```

---

#### Fetching Consents

```ts
import { getConsents, type SdkCallResult } from "@/sdk/consents";

const response: SdkCallResult<any> = await getConsents();

if (!response.ok) {
  console.error("Failed to fetch consents:", response.error);
  return;
}

// response.data is a FHIR-like Bundle
const entries = response.data?.entry ?? [];

for (const entry of entries) {
  const resource = entry.resource;
  console.log("Consent ID:", resource?.id);
  console.log("Status:", resource?.status);
  console.log("Category code:", resource?.category?.[0]?.coding?.[0]?.code);
  console.log("Provision type:", resource?.provision?.type);
}
```

**Always check `response.ok` before reading `response.data`.** When the call fails, `response.data` is `null`.

#### Bundle Structure

`response.data` is a FHIR-like Bundle. Consent records live in `response.data.entry`:

```
response.data
  └── entry: Array
        └── [n]
              ├── id?: string
              └── resource
                    ├── id: string                          → Consent ID
                    ├── status: string                      → "active" or "ACTIVE" (inconsistent — see Pitfalls)
                    ├── category[0].coding[0].code: string  → CategoryCode (e.g. "TOS")
                    ├── provision.type: string              → "PERMIT" or "DENY"
                    ├── patient.reference: string           → Patient reference
                    ├── organization[0].reference: string   → Organization reference
                    ├── performer[0].reference: string      → Fallback organization ref
                    └── meta.lastUpdated: string            → ISO timestamp
```

---

#### Creating a Consent

```ts
import { createConsent, type SdkCallResult } from "@/sdk/consents";
import type { CategoryCode, ConsentProvisionType } from "@icanbwell/bwell-sdk-ts";

const response: SdkCallResult<any> = await createConsent({
  category: "TOS" as CategoryCode,
  provision: "PERMIT" as ConsentProvisionType,
  // status defaults to "ACTIVE" if omitted
  // organizationId: "org-123" — optional
});

if (!response.ok) {
  console.error("Failed to create consent:", response.error);
  return;
}

console.log("Consent created:", response.data);
```

**Field reference:**

| Field | Type | Required | Default | Notes |
|---|---|---|---|---|
| `category` | `CategoryCode` | Yes | — | Use values from `categoryCodeValues` exported by the SDK |
| `provision` | `ConsentProvisionType` | Yes | — | `"PERMIT"` or `"DENY"` only |
| `status` | `ConsentStatus` | No | `"ACTIVE"` | Safe to omit in most cases |
| `organizationId` | `string` | No | `undefined` | Format is ambiguous — try plain ID first; if rejected, try `"Organization/<id>"` |

**Important:** The wrapper constructs a `CreateConsentRequest` class instance internally before passing it to the SDK. Do not call `sdk.user.createConsent()` with a plain object — it will fail. Always use the `createConsent()` wrapper, or instantiate the class explicitly:

```ts
import { CreateConsentRequest } from "@icanbwell/bwell-sdk-ts";

const request = new CreateConsentRequest({
  status: "ACTIVE",
  provision: "PERMIT",
  category: "TOS",
});

await sdk.user.createConsent(request);
```

All valid category codes are exported by the SDK and useful for populating dropdowns:

```ts
import { categoryCodeValues } from "@icanbwell/bwell-sdk-ts";
```

---

#### Response Structure

The SDK does not return raw data directly. Every call returns a `SdkCallResult`:

```ts
type SdkCallResult<T = unknown> = {
  ok: boolean;       // true if the call succeeded
  data: T | null;    // unwrapped payload (e.g. the FHIR Bundle)
  error: unknown;    // error details if ok === false
  raw: unknown;      // original SDK result, for debugging only
  shape: {           // inspection metadata, for debugging only
    hasSuccessFn: boolean;
    hasFailureFn: boolean;
    hasDataFn: boolean;
    hasErrorFn: boolean;
    keys: string[];
    constructorName: string | null;
  };
};
```

Under the hood, the SDK can return two different result shapes depending on version or environment:

- **Method-style:** `result.success()`, `result.data()`, `result.error()`, `result.failure()`
- **Property-style:** `result.success`, `result.data`, `result.error`, `result.failure`

`normalizeSdkResult` in `src/sdk/consents.ts` handles both transparently. Use `response.ok` and `response.data` — never reach into `response.raw` in application code.

---

#### Common Pitfalls

**1. SDK not initialized**

Symptom: All calls return `ok: false` with `"SDK not initialized."`.

Fix: Confirm `getSdk()` returns a non-null value after authentication completes.

---

**2. Passing a plain object to `sdk.user.createConsent()` directly**

Symptom: Server rejects the request or a runtime error is thrown.

Fix: Use the `createConsent()` wrapper, or construct a `CreateConsentRequest` instance explicitly. Never pass a plain object.

---

**3. Reading `response.data` without checking `response.ok`**

Symptom: Crash when `response.data` is `null` on a failed call.

Fix:
```ts
const response = await getConsents();
if (!response.ok) return; // always guard first
const entries = response.data?.entry ?? [];
```

---

**4. Assuming `entry` is always present**

Symptom: Crash when the user has no consents — `response.data.entry` is `undefined`.

Fix:
```ts
const entries = Array.isArray(response.data?.entry) ? response.data.entry : [];
```

---

**5. Inconsistent `status` casing**

Symptom: A filter like `resource.status === "ACTIVE"` misses records where the server returned `"active"`.

Fix: Normalize on read:
```ts
const status = (resource?.status ?? "").toUpperCase();
```

---

**6. `organizationId` format ambiguity**

Symptom: Consent is created but not linked to the expected organization; the value appears silently ignored.

Fix: If a plain ID doesn't work, try the FHIR reference format: `"Organization/<id>"`. Always trim whitespace before sending:
```ts
const organizationId = rawInput.trim() || undefined;
```

---

#### Debugging Tips

**Inspect the raw SDK result shape:**
```ts
const response = await getConsents();
console.log("shape:", response.shape);
// shape.hasSuccessFn: true  → SDK returned methods (.success(), .data())
// shape.hasSuccessFn: false → SDK returned properties (.success, .data)
// shape.constructorName     → identifies the SDK result class
console.log("raw:", response.raw);
```

**Validate SDK initialization:**
```ts
const sdk = getSdk();
console.log("SDK initialized:", !!sdk);
console.log("getConsents available:", typeof sdk?.user?.getConsents);
console.log("createConsent available:", typeof sdk?.user?.createConsent);
```

**Validate a create payload before sending:**
```ts
const request = new CreateConsentRequest({
  status: "ACTIVE",
  provision: "PERMIT",
  category: "TOS",
});
console.log("Request instance:", request);
console.log("Keys:", Object.keys(request));
```

---

#### Adding a New SDK Feature

Follow this pattern when wiring up any new SDK capability:

**Step 1 — Verify the raw call works first**

Before building UI, confirm the method returns something useful:
```ts
const sdk = getSdk();
const result = await sdk?.user.someNewMethod();
console.log("raw:", result);
console.log("keys:", result ? Object.keys(result) : []);
console.log("constructor:", result?.constructor?.name);
```

Check whether the result uses methods (`.data()`, `.success()`) or plain properties — this determines how `normalizeSdkResult` will handle it.

**Step 2 — Write an SDK wrapper in `src/sdk/`**

Model it after `src/sdk/consents.ts`. Return `SdkCallResult` and call `normalizeSdkResult` on the raw response.

**Step 3 — Map the bundle to flat rows for the UI**

```ts
function mapBundleToRows(payload: any): FeatureRow[] {
  const entries = Array.isArray(payload?.entry) ? payload.entry : [];
  return entries.map((entry: any, index: number) => ({
    id: entry?.resource?.id ?? `row-${index}`,
    // map other fields from entry.resource
  }));
}
```

**Step 4 — Build the component**

Use `SdkCallResult` — never access `.raw` in component code. Treat TypeScript types as a reference, not a runtime contract; confirm actual field names from console logs before building rendering logic.

**Step 5 — Test edge cases explicitly**

- SDK not initialized
- Empty bundle (`entry: []` or `entry` missing)
- `response.ok === false` with a visible error state
