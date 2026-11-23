import {
  BWellSDK,
  CreateConsentRequest,
  CreateVerificationUrlRequest,
  DataSourceRequest,
  GetConsentsRequest,
  GetTasksRequest,
  HealthDataRequest,
  SearchHealthResourcesRequest,
  UpdateProfileRequest,
} from "@icanbwell/bwell-sdk-ts";
import type { MemberConnection, Person, Consent } from "@icanbwell/bwell-sdk-ts";
import { CLEAR_CALLBACK_URL } from "@/config/app";
import { unwrapQuery, unwrapTransaction } from "@/sdk/results";
import { clearSmartConnectAgreement, markSmartConnectAgreed } from "@/sdk/smartConnectIntent";

export type ConsentCategory = "tos" | "ias:import:records";
export type VerificationState = "validated" | "in-process" | "unverified";

const lower = (v: string | null | undefined) => (v ?? "").toLowerCase();

async function searchConsents(sdk: BWellSDK, category: ConsentCategory): Promise<Consent[]> {
  const bundle = unwrapQuery(await sdk.user.getConsents(new GetConsentsRequest({ category })));
  return (bundle.entry ?? []).map((e) => e?.resource).filter((c): c is Consent => !!c);
}

/**
 * The current consent for a category, or undefined. Creating a consent marks the previous one
 * inactive, so the current one is the newest *active* consent. The query returns the whole history
 * (and no `dateTime`), so filter to active and order by `meta.lastUpdated`.
 */
export async function getLatestConsent(sdk: BWellSDK, category: ConsentCategory): Promise<Consent | undefined> {
  const updated = (c: Consent) => Date.parse(c.meta?.lastUpdated ?? c.dateTime ?? "") || 0;
  const consents = await searchConsents(sdk, category);
  return consents.filter((c) => lower(c.status) === "active").sort((x, y) => updated(y) - updated(x))[0];
}

export const isPermitted = (c: Consent | undefined) =>
  !!c && lower(c.status) === "active" && lower(c.provision?.type) === "permit";

async function createConsent(sdk: BWellSDK, category: ConsentCategory, permit: boolean) {
  return unwrapTransaction(
    await sdk.user.createConsent(
      new CreateConsentRequest({ status: "ACTIVE", provision: permit ? "PERMIT" : "DENY", category }),
    ),
  );
}

export const acceptTermsOfService = (sdk: BWellSDK) => createConsent(sdk, "tos", true);

/** Thrown when Smart Connect is requested for a user who hasn't passed IAL2 identity verification. */
export class VerificationRequiredError extends Error {
  constructor() {
    super("Smart Connect™ requires identity verification.") // plain string, so the ™ glyph stands in for the superscript;
  }
}

/**
 * The only way to create an IAS_IMPORT_RECORDS PERMIT consent: it re-checks IAL2 verification
 * first, so a consent can never be recorded for an unverified user.
 */
export async function enableSmartConnect(sdk: BWellSDK) {
  if ((await getVerificationState(sdk)) !== "validated") throw new VerificationRequiredError();
  return createConsent(sdk, "ias:import:records", true);
}

export const disableSmartConnect = (sdk: BWellSDK) => createConsent(sdk, "ias:import:records", false);

export async function getConnections(sdk: BWellSDK): Promise<MemberConnection[]> {
  return unwrapQuery(await sdk.connection.getMemberConnections());
}

export async function getOauthRedirectUrl(sdk: BWellSDK, connectionId: string): Promise<string> {
  return unwrapQuery(await sdk.connection.getOauthUrl(new DataSourceRequest({ connectionId }))).redirectUrl;
}

/** Direct IAS connections are re-activated in place; OAuth (PROA / indirect IAS) ones need the user to sign in again. */
export const isDirectConnection = (c: MemberConnection) => lower(c.integrationType) === "direct_ias";

export async function activateDirectConnection(sdk: BWellSDK, connectionId: string) {
  return unwrapTransaction(await sdk.connection.activateDirectConnection(new DataSourceRequest({ connectionId })));
}

export async function disconnect(sdk: BWellSDK, connectionId: string) {
  return unwrapTransaction(await sdk.connection.disconnectConnection(new DataSourceRequest({ connectionId })));
}

export async function deleteConnection(sdk: BWellSDK, connectionId: string) {
  return unwrapTransaction(await sdk.connection.deleteConnection(new DataSourceRequest({ connectionId })));
}

export async function getVerificationState(sdk: BWellSDK): Promise<VerificationState> {
  // The call succeeds with null data for a user who has never started verification.
  const result = unwrapTransaction(await sdk.user.getVerificationStatus()) as { status?: unknown } | null;
  const raw = result?.status;
  const status = lower(typeof raw === "string" ? raw : (raw as { coding?: { code?: string }[] } | null | undefined)?.coding?.[0]?.code);
  if (status === "validated" || status === "attested") return "validated";
  if (status === "in-process") return "in-process";
  return "unverified";
}

export async function createVerificationUrl(sdk: BWellSDK, callbackUrl?: string): Promise<string> {
  return unwrapTransaction(await sdk.user.createVerificationUrl(new CreateVerificationUrlRequest({ callbackUrl })));
}

/** The CLEAR identity verification URL, for the caller to open (see useExternalFlow). */
export function getClearVerificationUrl(sdk: BWellSDK): Promise<string> {
  return createVerificationUrl(sdk, CLEAR_CALLBACK_URL);
}

/** True once the user is identity-verified and has an active Smart Connect consent. */
export async function isSmartConnectEnabled(sdk: BWellSDK): Promise<boolean> {
  const [consent, verification] = await Promise.all([getLatestConsent(sdk, "ias:import:records"), getVerificationState(sdk)]);
  return isPermitted(consent) && verification === "validated";
}

export type SmartConnectStart = "searching" | "waiting-verification" | "needs-verification";

/**
 * Called when the user agrees to Smart Connect. Checks verification in the background, then either
 * creates the consent right away (which starts the network search) or reports that the user must
 * verify with CLEAR first. The caller opens CLEAR from a click (popups need one).
 */
export async function startSmartConnect(sdk: BWellSDK): Promise<SmartConnectStart> {
  const state = await getVerificationState(sdk);
  if (state === "validated") {
    if (!isPermitted(await getLatestConsent(sdk, "ias:import:records"))) await enableSmartConnect(sdk);
    clearSmartConnectAgreement();
    return "searching";
  }
  // Remember the agreement; the consent is created once they're verified.
  markSmartConnectAgreed();
  return state === "in-process" ? "waiting-verification" : "needs-verification";
}

export type RecommendedConnection ={ id: string; title: string; connectionId: string };

/** Connections the network search found that still need the user to sign in (OAuth). */
export async function getRecommendedConnections(sdk: BWellSDK): Promise<RecommendedConnection[]> {
  const response = unwrapQuery(
    // groupCode is what the SDK sends as the care team category filter.
    await sdk.health.getCareTeams(
      new HealthDataRequest({ page: 0, pageSize: 50, groupCode: { value: { code: "recommended-connections" } } }),
    ),
  );
  // FHIR Organization.alias is a list of strings, so the API can return ["epic_on_fhir_sandbox"] rather than a bare string.
  type Alias = string | string[];
  type Member = { alias?: Alias; display?: string; resource?: { alias?: Alias; organizationName?: string; name?: string } };
  const out: RecommendedConnection[] = [];
  for (const entry of response.entry ?? []) {
    const team = entry?.resource as { id?: string; participant?: { member?: Member }[] } | undefined;
    for (const participant of team?.participant ?? []) {
      const member = participant?.member;
      // The connection id is the organization's alias. The SDK's query returns it on the member's
      // resource; some builds expose it directly on the member.
      // getOauthUrl needs a single string connectionId, so take the first alias when it is a list.
      const rawAlias = member?.alias ?? member?.resource?.alias;
      const alias = Array.isArray(rawAlias) ? rawAlias.find((a) => typeof a === "string" && a) : rawAlias;
      if (!team?.id || !alias) continue;
      const title = member?.display || member?.resource?.organizationName || member?.resource?.name || alias;
      out.push({ id: `${team.id}:${alias}`, title, connectionId: alias });
    }
  }
  return out;
}

export type TaskState = "pending" | "running" | "done" | "failed" | "none";

export async function getNetworkSearchState(sdk: BWellSDK): Promise<TaskState> {
  const tasks = unwrapQuery(
    await sdk.activity.getTasks(
      new GetTasksRequest({
        code: { value: { code: "network-data-retrieval" } },
        performer: { values: [{ system: "https://www.icanbwell.com/performerType", code: "system" }] },
        page: 0,
        pageSize: 50,
      } as never),
    ),
  );
  type TaskLike = { status?: unknown; authoredOn?: string; lastModified?: string; meta?: { lastUpdated?: string } };
  const stamp = (t: TaskLike) => Date.parse(t.authoredOn ?? t.lastModified ?? t.meta?.lastUpdated ?? "") || 0;
  // When the task last changed (a task updated in place keeps its old authoredOn).
  const modified = (t: TaskLike) => Date.parse(t.lastModified ?? t.meta?.lastUpdated ?? t.authoredOn ?? "") || 0;
  // The query returns every earlier search too (and has no sort), so only the newest task counts.
  const newest = (tasks.entry ?? [])
    .map((e) => e?.resource as TaskLike | undefined)
    .filter((t): t is TaskLike => !!t)
    .sort((a, b) => stamp(b) - stamp(a))[0];
  if (!newest) return "none";
  // FHIR Task.status; normalised because the API may return enum-style values (IN_PROGRESS).
  const state = ((): TaskState => {
    switch (lower(String(newest.status ?? "")).replace(/_/g, "-")) {
      case "completed": return "done";
      case "failed":
      case "rejected":
      case "cancelled":
      case "entered-in-error": return "failed";
      case "in-progress": return "running";
      default: return "pending";
    }
  })();
  // A finished task that last changed before the current consent belongs to an earlier search, so
  // keep waiting for the one the consent just started.
  if (state === "done" || state === "failed") {
    const consentAt = Date.parse((await getLatestConsent(sdk, "ias:import:records"))?.meta?.lastUpdated ?? "") || 0;
    if (modified(newest) && modified(newest) < consentAt) return "pending";
  }
  return state;
}

export type ConnectionSearchResult = { id: string; name: string; type: string; connectionId?: string; logoUrl?: string };

export const isZipCode = (zip: string) => /^\d{5}$/.test(zip);

/** First 5-digit postal code on the profile, preferring a home address. */
export function zipFromProfile(person: Person): string | undefined {
  const addresses = (person.address ?? []).filter((a): a is NonNullable<typeof a> => !!a);
  const ordered = [...addresses.filter((a) => a.use === "home"), ...addresses.filter((a) => a.use !== "home")];
  for (const a of ordered) {
    const zip = a.postalCode?.trim().slice(0, 5);
    if (zip && isZipCode(zip)) return zip;
  }
  return undefined;
}

export async function searchConnections(sdk: BWellSDK, term: string, zip?: string): Promise<ConnectionSearchResult[]> {
  const data = unwrapQuery(
    await sdk.search.searchHealthResources(
      new SearchHealthResourcesRequest({
        search: term,
        page: 0,
        pageSize: 20,
        client: [{ config: "bwell_proa", dataSets: ["CONNECTHUB", "NPPES"] }],
        filters: { includePopulatedProaOnly: true },
        orderBy: [{ field: "relevance", order: "desc" }],
        // The SDK types SearchLocation as lat/lon only, but the API accepts a zip code alone.
        ...(zip && isZipCode(zip) ? { searchLocation: { zipCode: zip } as never } : {}),
      }),
    ),
  );
  return (data.results ?? []).map((r: any, i: number) => ({
    id: r.id ?? `result-${i}`,
    name: r.content ?? r.display ?? "Unnamed source",
    type: r.type ?? "",
    connectionId: Array.isArray(r.endpoint) ? r.endpoint[0]?.name : undefined,
    logoUrl: r.logoUrl ?? undefined,
  }));
}

export async function getProfile(sdk: BWellSDK): Promise<Person> {
  return unwrapQuery(await sdk.user.getProfile());
}

/** Blank names are left out of the request, so they can never overwrite the stored name. */
export async function updateProfileName(sdk: BWellSDK, firstName: string, lastName: string) {
  const fields = {
    ...(firstName.trim() ? { firstName: firstName.trim() } : {}),
    ...(lastName.trim() ? { lastName: lastName.trim() } : {}),
  };
  return unwrapTransaction(await sdk.user.updateProfile(new UpdateProfileRequest(fields as never)));
}

export async function getHealthSummary(sdk: BWellSDK) {
  return unwrapQuery(await sdk.health.getHealthSummary());
}

export async function deleteAccount(sdk: BWellSDK) {
  return unwrapTransaction(await sdk.user.delete());
}
