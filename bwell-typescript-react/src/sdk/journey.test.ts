import { describe, expect, test } from "vitest";
import type { BWellSDK, Person } from "@icanbwell/bwell-sdk-ts";
import { getNetworkSearchState, getRecommendedConnections, isZipCode, zipFromProfile } from "@/sdk/journey";

// Only the SDK (the external boundary) is faked; the functions under test run for real.
const ok = (data: unknown) => ({ data, hasError: () => false });
const task = (status: string, extra: Record<string, unknown> = {}) => ({ resource: { status, ...extra } });

function sdkWith(parts: { tasks?: unknown[]; consentUpdated?: string; careTeams?: unknown[] }): BWellSDK {
  return {
    activity: { getTasks: async () => ok({ entry: parts.tasks ?? [] }) },
    user: {
      getConsents: async () =>
        ok({ entry: parts.consentUpdated ? [{ resource: { status: "active", meta: { lastUpdated: parts.consentUpdated } } }] : [] }),
    },
    health: { getCareTeams: async () => ok({ entry: parts.careTeams ?? [] }) },
  } as unknown as BWellSDK;
}

describe("getNetworkSearchState", () => {
  test.each([
    ["completed", "done"],
    ["COMPLETED", "done"],
    ["failed", "failed"],
    ["rejected", "failed"],
    ["cancelled", "failed"],
    ["entered-in-error", "failed"],
    ["ENTERED_IN_ERROR", "failed"],
    ["in-progress", "running"],
    ["IN_PROGRESS", "running"],
    ["on-hold", "pending"],
    ["requested", "pending"],
  ])("maps the newest task status %s to %s", async (status, expected) => {
    expect(await getNetworkSearchState(sdkWith({ tasks: [task(status)] }))).toBe(expected);
  });

  test("returns none when there are no tasks", async () => {
    expect(await getNetworkSearchState(sdkWith({}))).toBe("none");
  });

  test.each([
    ["an old failed task", "failed", "2024-01-01T00:00:00Z", "completed", "2024-06-01T00:00:00Z", "done"],
    ["an old completed task", "completed", "2024-01-01T00:00:00Z", "in-progress", "2024-06-01T00:00:00Z", "running"],
  ])("only the newest task counts: %s", async (_label, oldStatus, oldAt, newStatus, newAt, expected) => {
    const tasks = [task(newStatus, { authoredOn: newAt }), task(oldStatus, { authoredOn: oldAt })];
    expect(await getNetworkSearchState(sdkWith({ tasks }))).toBe(expected);
  });

  test.each([
    ["before the consent (a previous search)", "2024-01-01T00:00:00Z", "pending"],
    ["after the consent (the current search)", "2024-09-01T00:00:00Z", "done"],
  ])("a completed task last modified %s", async (_label, modifiedAt, expected) => {
    const sdk = sdkWith({ tasks: [task("completed", { lastModified: modifiedAt })], consentUpdated: "2024-06-01T00:00:00Z" });
    expect(await getNetworkSearchState(sdk)).toBe(expected);
  });
});

describe("getRecommendedConnections", () => {
  const team = (member: unknown) => ({ resource: { id: "team1", participant: [{ member }] } });

  test.each([
    ["a string alias", { alias: "epic_sandbox", display: "Epic" }, "epic_sandbox"],
    ["a list alias (FHIR Organization.alias)", { alias: ["epic_sandbox"], display: "Epic" }, "epic_sandbox"],
    ["a list alias on the member resource", { resource: { alias: ["epic_sandbox"] }, display: "Epic" }, "epic_sandbox"],
  ])("uses a single string connectionId from %s", async (_label, member, expected) => {
    const [connection] = await getRecommendedConnections(sdkWith({ careTeams: [team(member)] }));
    expect(connection.connectionId).toBe(expected);
    expect(connection.id).toBe(`team1:${expected}`);
  });

  test.each([
    ["no alias", { display: "Epic" }],
    ["an empty alias list", { alias: [], display: "Epic" }],
  ])("skips a member with %s", async (_label, member) => {
    expect(await getRecommendedConnections(sdkWith({ careTeams: [team(member)] }))).toEqual([]);
  });
});

describe("zip codes", () => {
  test.each([
    ["12345", true],
    ["1234", false],
    ["123456", false],
    ["12a45", false],
  ])("isZipCode(%s) is %s", (zip, expected) => {
    expect(isZipCode(zip)).toBe(expected);
  });

  test.each([
    ["prefers a home address", [{ use: "work", postalCode: "11111" }, { use: "home", postalCode: "22222-1234" }], "22222"],
    ["falls back to another address", [{ use: "work", postalCode: "11111" }], "11111"],
    ["ignores invalid postal codes", [{ use: "home", postalCode: "ABC" }], undefined],
    ["handles no addresses", [], undefined],
  ])("zipFromProfile %s", (_label, address, expected) => {
    expect(zipFromProfile({ address } as unknown as Person)).toBe(expected);
  });
});
