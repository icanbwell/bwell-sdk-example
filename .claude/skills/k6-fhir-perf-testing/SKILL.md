---
name: k6-fhir-perf-testing
description: >-
  Run k6 Cloud performance/load tests against the b.well FHIR server and report latency stats.
  Use this whenever the user wants to run a k6 cloud test, load-test a FHIR endpoint (e.g. $merge,
  search, read), measure P90/P95/Avg latency, get a fresh FHIR access token for k6, fetch or parse
  k6 Cloud run metrics, build a sized $merge payload, or pre-create base resources for load testing.
  Triggers on phrases like "run the k6 test", "load test the merge endpoint", "get the latency
  stats for run X", "perf test the FHIR server", "how slow is $merge at N resources", or any request
  involving k6 cloud runs, FHIR tokens for k6, or k6_run_metrics. Covers the full loop: token →
  k6 cloud run → fetch metrics → verify the run actually completed.
---

# k6 Cloud Performance Testing — b.well FHIR Server

This skill captures the end-to-end workflow for running k6 Cloud load tests against the b.well FHIR
server and reporting clean latency numbers. The core loop is always the same four steps; the helper
scripts live in `fhir-server/` in the k6 repo.

## The four-step loop

```
1. token   = python3 fhir-server/get_access_token.py        # fetch ONCE per batch
2. run     = k6 cloud run -e JWT=$token ... <script>.js      # capture the run URL
3. metrics = python3 fhir-server/k6_run_metrics.py <run_id>  # P90/P95/Avg + verify
4. report  = relay P95/P90/Avg + the run link
```

Do these one test at a time, in the **foreground**, undisturbed (see Gotchas). For a matrix of
tests, fetch the token once and reuse it across the whole batch.

## Prerequisites (check once)

- **k6 installed** and **logged in to k6 Cloud**: `k6 cloud login` (or a token in the k6 config at
  `~/Library/Application Support/k6/config.json`). `k6_run_metrics.py` reads this token; it never
  needs the FHIR token.
- **A refresh token configured** for `get_access_token.py` — either pasted into the script's
  `REFRESH_TOKEN` (gitignored sidecar `.refresh_token.<env>` takes over after first rotation) or
  exported as `REFRESH_TOKEN`. If the token is missing/expired you'll get a 401 in k6 `setup()`.
- The k6 script has a `cloud:` block in `options` (projectID + run name). Reuse the same run `name`
  across a batch so they group together in Grafana.

## Step 1 — Get the FHIR access token (once per batch)

```bash
JWT=$(python3 fhir-server/get_access_token.py 2>/tmp/tok_err.txt)
echo "exit=$? len=${#JWT}"   # expect a ~900+ char token; if empty, cat /tmp/tok_err.txt
echo "$JWT" > /tmp/k6_jwt.txt # reuse across the batch
```

Why fetch once: the Okta refresh token **rotates on every call**. Fetching a token per-test races
the rotation and can invalidate the chain mid-batch (→ `invalid_grant`, then 401s in setup). One
fetch, reuse the value.

**Token safety (non-negotiable):** the access token goes to STDOUT only and is handed straight to
k6. Never echo it into logs, never paste it into a wiki/PR/Slack, never commit it. The refresh token
is a credential too — keep it in the gitignored sidecar or an env var, never in committed code.

## Step 2 — Run the k6 Cloud test

```bash
cd <dir with the script>
JWT=$(cat /tmp/k6_jwt.txt)
k6 cloud run -e JWT="$JWT" -e <SCRIPT_ENV...> <script>.js 2>&1 | tee /tmp/run.log >/dev/null
grep -oE 'https://[^ ]*runs/[0-9]+' /tmp/run.log | head -1   # the run URL / id
```

- Use `k6 cloud run <script>` (k6 v0.5x+/v2 syntax), **not** `k6 run` (that's local).
- **Shell quoting:** assign `JWT=$(...)` on its own line, then use `-e JWT="$JWT"`. Do NOT use the
  prefix form `JWT=$(...) k6 cloud run -e JWT="$JWT" ...` — the `$JWT` expands before the assignment,
  sending an empty token → 401.
- The run id is the trailing number in the Grafana URL (`.../runs/7887122`).
- For the `merge.js` script in this repo the env vars are: `RESOURCE_SIZE_KB` (selects which base
  resource to fetch), and either `RESOURCE_COUNT=N` (fixed count) or `PAYLOAD_SIZE_MB=M` (derives
  count). Optional: `FHIR_BASE_URL`, `LOAD_ZONE`. See the header of
  `fhir-server/composition/merge.js`.

## Step 3 — Fetch the latency metrics

```bash
python3 fhir-server/k6_run_metrics.py <run_id>
# optional 2nd arg: a different trend metric (default http_req_duration)
```

Prints P90/P95/Avg/P99/min/max for `http_req_duration` plus the run link and a PASS/FAIL verdict.

- The script **waits for `processing_status == 2`** (cloud finished aggregating). Querying earlier
  returns preliminary values that drift — that's why early numbers sometimes disagree with final ones.
- **If P90/P95/Avg come back `n/a` but min/max are present, just re-run the same command.** The
  aggregate query occasionally returns before all percentiles settle; a second call returns clean
  values. (Min/max present = the run definitely executed.)

## Step 4 — Verify it actually ran, then report

The last line of `k6_run_metrics.py` is the verdict:

```
Ran:    150s | merges ran: yes -> PASS (ran ~2 min)
```

`PASS` requires `duration >= 115s` AND a non-null Avg. Trust the **trend metric (http_req_duration)**
as the "did it run" signal — the counter metrics (`http_reqs`/`iterations`) aggregate unreliably in
the cloud API and can read 0 even on a good run. A run showing `aborted by user` in Grafana was
killed by a SIGTERM (see Gotchas) — re-run it.

When reporting, give **P95 / P90 / Avg** and the run link. P95 is the headline number.

## Patterns this workflow relies on

### Fetch-replicate (fast per-iteration payloads)
Don't generate payload JSON inside k6 — it's heavy and can overload the cloud agent at large sizes.
Instead: pre-create ONE canonical resource on the server, `http.get` it once in `setup()`, then each
iteration shallow-copies it N times and rewrites only the `id` (fresh `uuidv4()`) in place. This
keeps each iteration cheap and lets a single 1-VU stream push 40 MB+ bodies. See `merge.js`:
`setup()` returns `{base, count}`; the module-scope `payload` is built once and ids are rewritten
per iteration.

### Pre-created base resources
`fhir-server/composition/create_base_resources.py` creates one base Composition per size
(`k6-test-8mb`, `k6-test-1mb`, `k6-test-80kb`, `k6-test-10kb`, `k6-test-3kb`) so the k6 script can
fetch-and-replicate them. Run it once before a campaign:

```bash
JWT=$(python3 fhir-server/get_access_token.py) \
  FHIR_URL=https://fhir-merge.dev.bwell.zone/4_0_0 \
  python3 fhir-server/composition/create_base_resources.py [sizes...]
```

Key detail: the server **inflates** resources on read (~7×, adding `_uuid`/`_sourceAssigningAuthority`
to every reference), so the script builds each base at `target / INFLATION` (default 6.7) — small
enough to store under Mongo's 16 MB limit, but reading back at ~target size. The base-id mapping must
match `BASE_IDS` in `merge.js`.

## Gotchas (learned the hard way)

- **Run in the foreground, undisturbed.** A backgrounded k6 cloud run gets a SIGTERM when the agent
  acts/messages mid-run, and Grafana shows it `aborted by user`. Run each test as one blocking
  command and don't do other work until it returns. Set a generous Bash timeout (~7 min for a 2-min
  test + setup), e.g. `timeout: 420000`.
- **Fetch the token once per batch** (rotation — see Step 1).
- **`k6 cloud run`, not `k6 run`** (cloud vs local).
- **Re-query metrics on `n/a`** (Step 3).
- **`$merge` returns HTTP 200 even when individual resources fail** — status alone doesn't prove a
  successful merge. For perf work we only care that the request ran for the full duration, which the
  verdict line confirms; don't over-check merge success unless correctness is the goal.
- **`response.read()` (full body) can hang** in sandboxed/VPN-restricted shells. To size a response,
  read the `Content-Length` header instead of the body (this is why `setup()` uses Content-Length for
  the base resource size).

## Quick reference — full single test

```bash
# once per batch
JWT=$(python3 fhir-server/get_access_token.py); echo "$JWT" > /tmp/k6_jwt.txt

# per test (foreground)
cd fhir-server/composition
JWT=$(cat /tmp/k6_jwt.txt)
k6 cloud run -e JWT="$JWT" -e RESOURCE_SIZE_KB=80 -e RESOURCE_COUNT=50 merge.js 2>&1 | tee /tmp/r.log >/dev/null
RUN=$(grep -oE 'runs/[0-9]+' /tmp/r.log | head -1 | cut -d/ -f2)

# metrics (re-run if P95 is n/a)
cd /Users/shubhamgoel/repos/k6
python3 fhir-server/k6_run_metrics.py "$RUN"
```
