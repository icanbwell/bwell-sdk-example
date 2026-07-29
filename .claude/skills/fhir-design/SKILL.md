---
name: fhir-design
description: Use when modeling data in FHIR or authoring/scoping an FDR (FHIR Design Review) — a new FHIR resource usage, a new profile or extension, mapping a healthcare concept to FHIR, or any change to how b.well structures FHIR resources. Walks the author through the FHIR conformance + IG-conformance + feasibility rubric and the FDR process so the design is standards-conformant, IG-conformant, and survives b.well's data volumes before it reaches the FHIR SME.
---

# FHIR design authoring (FDR)

You help an engineer produce a FHIR design that would pass the **FDR (FHIR Design Review)** — b.well's governance process for new/novel FHIR usage, owned by the FHIR SME and distinct from the Tech Design Review. The recurring FHIR failures are specific and repeat: a bespoke resource where a standard one exists, placeholder canonical URLs, wrong datatypes/bindings, no named IG profile, and — the expensive one — a model that is *conformant but infeasible* at b.well's volumes. Your job is to catch all of that **before** the FDR is submitted.

The rubric is the source of truth — read it, don't restate it:
- **`rubrics/fhir-feasibility-rubric.md`** — the three axes you're designing to pass: conformance (`#rub-fhir-conformance`), IG conformance (`#rub-fhir-ig`), feasibility at scale (`#rub-fhir-feasibility`), plus derived-resource fidelity/tenancy (`#rub-fhir-fidelity`) and process (`#rub-fhir-process`).
- Supporting: `decision-guides/abstraction-and-reduction.md` (name the general problem first), `decision-guides/datastore-selection.md#dg-fhir-not-fsm` (FHIR is not an operational/FSM store), `rubrics/tech-design-rubric.md` if the change also needs a TDR.

These live in the org `.github` repo; if they aren't in the current repo, read them from `icanbwell/.github`.

## Step 0 — is an FDR needed, and does one already exist?

New or novel FHIR resource usage, a new profile, or a new extension → **FDR required** (owner: FHIR SME), per `AGENTS.md` (FHIR Design Review process) and `rubrics/fhir-feasibility-rubric.md#rub-fhir-process`. Reading/following an existing pattern → no FDR.

**Before authoring anything, search for an existing FDR** (don't reinvent one). Discover the Atlassian cloudId at runtime (`getAccessibleAtlassianResources` — **never hardcode it**), then `searchConfluenceUsingCql` in the ENTARCH space for an FDR covering this resource/use-case. If one exists, the job is to *follow* it (and flag any divergence), not write a new one.

## How to run it

Work the rubric's axes in order — don't jump to field mapping before the resource choice and IG are settled.

1. **Name the general problem, then the resource (abstraction first).** Before picking a resource, do the `decision-guides/abstraction-and-reduction.md` move: what is this, generically? Then reconcile with the **standard FHIR resource** (`#rub-fhir-standard-resource`) — EOB data → `ExplanationOfBenefit`, not a bespoke "Composition"; and confirm you're not using FHIR as an operational/FSM store (`#rub-fhir-not-operational`, `datastore-selection.md#dg-fhir-not-fsm`).
2. **Name the IG and profile (`#rub-fhir-ig-profile`).** Build on the committed IG, not a bare R4 base: **US Core** (Patient/Practitioner/Observation/Condition…), **CARIN BB** (Coverage/ExplanationOfBenefit), **Da Vinci** (payer exchange/prior-auth/CDex), **NDH** (provider directory). Set `meta.profile`; honor must-support elements and the profile's tighter cardinality/bindings.
3. **Conformance details (`#rub-fhir-conformance`).** Real canonical URLs (`fhir.icanbwell.com` or HL7 — never `example.org`); spec datatypes and correct bindings (RxNorm meds, LOINC labs/vitals, US-NPI practitioners, UCUM quantities); native fields before extensions (`#rub-fhir-extensions`); create/update/conditional-update + duplicate-detection semantics defined (`#rub-fhir-update-semantics`).
4. **Feasibility at scale — the axis that fails silently (`#rub-fhir-feasibility`).** Estimate resource count, creation rate, and steady-state total; is the model O(patients), O(encounters), or O(patients×definitions)? Bound it. Account for write amplification (**every FHIR write = a new version + an AuditEvent** on a single-worker merge path); call out high-churn writes (per-event re-tag, delete+recreate, true-up-all). Watch query fan-out / N+1 / unbounded `$everything`.
5. **Derived-resource fidelity & tenancy (`#rub-fhir-fidelity`).** If the design composes/derives resources: propagate source `meta.security` (owner/access/vendor/`sourceAssigningAuthority`), emit `Provenance`, don't collapse to one bucket; tenant filter on every path incl. every pagination chunk; resolve sensitivity/consent before a derived resource goes user-facing.

## Ground every claim against real FHIR — verify, don't trust

You have tool access. FHIR designs fail on details that are checkable, so check them (this is the authoring twin of `/fhir-design-review`'s adversarial pass):
- **Resolve the IG profile for real.** Don't assert "US Core conformant" — pull the actual profile (`hl7.org/fhir/us/core/…`) and confirm the must-support elements and cardinality/bindings you're claiming. Fetch the canonical URLs you cite and confirm they resolve.
- **Check existing Helix profiles.** Look for an existing `fhir.icanbwell.com` StructureDefinition for this resource and build on it rather than diverging silently.
- **Ground the cardinality in real numbers.** If the model is per-X, count N(X) in the actual system (e.g. current `Task`/resource counts) rather than guessing — a growth estimate built on a real count is the difference between "feasible" and the 148M-Task failure (EA-2330).
- **Anything you can't verify becomes an open item with an owner** — never assert it in the FDR.

## Output — a submittable FDR

Produce the FDR in b.well's expected shape (FHIR SME as reviewer), covering: use case; resource selection with the standard-resource reconciliation and alternatives-with-reasons (not "benefits" bullets); named IG + base/Helix profile; field mapping table with cardinalities; extensions (each justified, real canonical URL, defined type/value set); CodeSystem/ValueSet bindings; update/duplicate semantics; **a feasibility section with the resource-count & growth math**; derived-resource fidelity/tenancy where applicable; and an open-questions tracker with owners. Then **self-grade against `rubrics/fhir-feasibility-rubric.md`** and show which criteria pass and which need work — a design is *not feasible-approved* if any C-axis (feasibility) criterion fails, however conformant it is. Don't invent profiles, canonical URLs, or approvals; mark unknowns as owned open items.
