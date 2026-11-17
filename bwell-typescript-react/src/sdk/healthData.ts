import {
  AllergyIntoleranceGroupsRequest, BWellSDK, CarePlanGroupsRequest, ConditionGroupsRequest,
  DiagnosticReportLabGroupsRequest, EncounterGroupsRequest, HealthDataRequest, ImmunizationGroupsRequest,
  LabGroupsRequest, MedicationGroupsRequest, ProcedureGroupsRequest, VitalSignGroupsRequest,
} from "@icanbwell/bwell-sdk-ts";
import type { HealthDataTypeId } from "@/config/app";
import { unwrapPartialQuery } from "@/sdk/results";

export type Paging = { page: number; pageSize: number };

/** One line in a health data list, whichever view it came from. */
export type HealthRow = {
  id: string;
  title: string;
  date?: string;
  /** Secondary text: sources, status, class… */
  detail?: string;
  /** Refined view only: how many underlying records were merged into this row. */
  recordCount?: number;
  /** Full object, shown when the row is expanded. */
  json: unknown;
};

export type HealthPage = {
  rows: HealthRow[];
  /** Total across all pages, when the server reports one. */
  total: number | null;
  /** Set when the server returned data alongside an error, so some records may be missing. */
  warning?: string;
};
export type HealthView = "refined" | "raw";

type Fetcher = (sdk: BWellSDK, paging: Paging) => Promise<HealthPage>;
type Definition = { refined: Fetcher; raw: Fetcher };

type Paged = { pageNumber?: number | null; totalItems?: number | null } | null | undefined;
const totalOf = (info: Paged, fallback?: number | null) => info?.totalItems ?? fallback ?? null;

const clean = (items: (string | null | undefined)[] | null | undefined) =>
  Array.from(new Set((items ?? []).filter((x): x is string => !!x)));

// ---- Refined: the get[Resource]Groups methods (b.well compositions) -------------------------

type Group = {
  id?: string | null;
  name?: string | null;
  references?: (string | null)[] | null;
  sourceDisplay?: (string | null)[] | null;
};

function groupRows<G extends Group>(resources: G[], date: (g: G) => string | null | undefined, extra?: (g: G) => string | undefined): HealthRow[] {
  return resources.map((g, i) => ({
    id: g.id ?? `group-${i}`,
    title: g.name || "Unnamed",
    date: date(g) ?? undefined,
    detail: [extra?.(g), clean(g.sourceDisplay).join(", ")].filter(Boolean).join(" · ") || undefined,
    recordCount: g.references?.length,
    json: g,
  }));
}

const display = (c: { display?: string | null } | null | undefined) => c?.display ?? undefined;

// ---- Raw: the plain FHIR resources ---------------------------------------------------------

const text = (cc: any): string | undefined =>
  cc?.text || cc?.coding?.find((c: any) => c?.display)?.display || cc?.coding?.[0]?.code || undefined;

/** Best-effort one-line description of any FHIR resource we list. */
function describe(resource: any): { title: string; date?: string; detail?: string } {
  const title =
    text(resource?.code) ?? text(resource?.vaccineCode) ?? text(resource?.medicationCodeableConcept) ??
    text(resource?.type?.[0]) ?? text(resource?.type) ?? text(resource?.category?.[0]) ?? resource?.resourceType ?? "Record";
  const date =
    resource?.onsetDateTime ?? resource?.recordedDate ?? resource?.effectiveDateTime ?? resource?.occurrenceDateTime ??
    resource?.performedDateTime ?? resource?.authoredOn ?? resource?.issued ?? resource?.period?.start ?? resource?.performedPeriod?.start;
  const detail = [resource?.status?.coding?.[0]?.code ?? (typeof resource?.status === "string" ? resource.status : undefined),
    text(resource?.clinicalStatus), resource?.valueQuantity ? `${resource.valueQuantity.value ?? ""} ${resource.valueQuantity.unit ?? ""}`.trim() : undefined]
    .filter(Boolean).join(" · ");
  return { title, date: date ?? undefined, detail: detail || undefined };
}

type RawResponse = { entry: ({ id?: string | null; resource?: unknown } | null)[] | null; total: number | null; pagingInfo: Paged };

function rawPage({ data: r, warning }: { data: RawResponse; warning?: string }): HealthPage {
  const rows = (r.entry ?? []).flatMap((e, i): HealthRow[] => {
    if (!e?.resource) return [];
    const d = describe(e.resource);
    return [{ id: e.id ?? (e.resource as { id?: string }).id ?? `raw-${i}`, ...d, json: e.resource }];
  });
  return { rows, total: totalOf(r.pagingInfo, r.total), warning };
}

const refinedPage = <G extends Group>({ data: r, warning }: { data: { resources: G[]; pagingInfo: Paged }; warning?: string }, date: (g: G) => string | null | undefined, extra?: (g: G) => string | undefined): HealthPage =>
  ({ rows: groupRows(r.resources, date, extra), total: totalOf(r.pagingInfo), warning });

const req = (p: Paging) => ({ page: p.page, pageSize: p.pageSize });

export const HEALTH_DEFINITIONS: Record<HealthDataTypeId, Definition> = {
  conditions: {
    refined: async (sdk, p) => refinedPage(unwrapPartialQuery(await sdk.health.getConditionGroups(new ConditionGroupsRequest(req(p)))), (g) => g.recordedDate),
    raw: async (sdk, p) => rawPage(unwrapPartialQuery(await sdk.health.getConditions(new HealthDataRequest(req(p))))),
  },
  medications: {
    refined: async (sdk, p) => refinedPage(unwrapPartialQuery(await sdk.health.getMedicationGroups(new MedicationGroupsRequest(req(p)))), (g) => g.authoredOn, (g) => display(g.status)),
    raw: async (sdk, p) => rawPage(unwrapPartialQuery(await sdk.health.getMedicationStatements(new HealthDataRequest(req(p))))),
  },
  allergies: {
    refined: async (sdk, p) => refinedPage(unwrapPartialQuery(await sdk.health.getAllergyIntoleranceGroups(new AllergyIntoleranceGroupsRequest(req(p)))), (g) => g.recordedDate, (g) => display(g.criticality)),
    raw: async (sdk, p) => rawPage(unwrapPartialQuery(await sdk.health.getAllergyIntolerances(new HealthDataRequest(req(p))))),
  },
  labs: {
    refined: async (sdk, p) => refinedPage(unwrapPartialQuery(await sdk.health.getLabGroups(new LabGroupsRequest(req(p)))), (g) => g.effectiveDateTime),
    raw: async (sdk, p) => rawPage(unwrapPartialQuery(await sdk.health.getLabs(new HealthDataRequest(req(p))))),
  },
  vitals: {
    refined: async (sdk, p) => refinedPage(unwrapPartialQuery(await sdk.health.getVitalSignGroups(new VitalSignGroupsRequest(req(p)))), (g) => g.effectiveDateTime),
    raw: async (sdk, p) => rawPage(unwrapPartialQuery(await sdk.health.getVitalSigns(new HealthDataRequest(req(p))))),
  },
  immunizations: {
    refined: async (sdk, p) => refinedPage(unwrapPartialQuery(await sdk.health.getImmunizationGroups(new ImmunizationGroupsRequest(req(p)))), (g) => g.occurrenceDateTime),
    raw: async (sdk, p) => rawPage(unwrapPartialQuery(await sdk.health.getImmunizations(new HealthDataRequest(req(p))))),
  },
  procedures: {
    refined: async (sdk, p) => refinedPage(unwrapPartialQuery(await sdk.health.getProcedureGroups(new ProcedureGroupsRequest(req(p)))), (g) => g.performedDate),
    raw: async (sdk, p) => rawPage(unwrapPartialQuery(await sdk.health.getProcedures(new HealthDataRequest(req(p))))),
  },
  encounters: {
    refined: async (sdk, p) => refinedPage(unwrapPartialQuery(await sdk.health.getEncounterGroups(new EncounterGroupsRequest(req(p)))), (g) => g.date, (g) => display(g.class) ?? display(g.type)),
    raw: async (sdk, p) => rawPage(unwrapPartialQuery(await sdk.health.getEncounters(new HealthDataRequest(req(p))))),
  },
  "care-plans": {
    refined: async (sdk, p) => refinedPage(unwrapPartialQuery(await sdk.health.getCarePlanGroups(new CarePlanGroupsRequest(req(p)))), (g) => g.period?.start),
    raw: async (sdk, p) => rawPage(unwrapPartialQuery(await sdk.health.getCarePlans(new HealthDataRequest(req(p))))),
  },
  reports: {
    refined: async (sdk, p) => refinedPage(unwrapPartialQuery(await sdk.health.getDiagnosticReportLabGroups(new DiagnosticReportLabGroupsRequest(req(p)))), (g) => g.issued ?? g.effectiveDateTime),
    raw: async (sdk, p) => rawPage(unwrapPartialQuery(await sdk.health.getDiagnosticReports(new HealthDataRequest(req(p))))),
  },
};

export const fetchHealthPage = (sdk: BWellSDK, type: HealthDataTypeId, view: HealthView, paging: Paging) =>
  HEALTH_DEFINITIONS[type][view](sdk, paging);
