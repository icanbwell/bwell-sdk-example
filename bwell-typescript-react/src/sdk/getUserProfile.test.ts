import { beforeEach, describe, expect, test, vi } from "vitest";
import { getSdk } from "@/sdk/bWellSdk";
import { getUserProfile } from "./getUserProfile";

vi.mock("@/sdk/bWellSdk", () => ({ getSdk: vi.fn() }));

const mockGetSdk = vi.mocked(getSdk);

const withProfile = (data: unknown): void => {
  mockGetSdk.mockReturnValue({
    user: { getProfile: vi.fn().mockResolvedValue({ data }) },
  } as unknown as ReturnType<typeof getSdk>);
};

beforeEach(() => {
  mockGetSdk.mockReset();
});

describe("getUserProfile", () => {
  test("returns fully-defaulted fields when the SDK is not initialized", async () => {
    mockGetSdk.mockReturnValue(undefined);

    const profile = await getUserProfile();

    expect(profile.id).toBeNull();
    expect(profile.firstName).toBe("");
    expect(profile.lastName).toBe("");
    expect(profile.email).toBe("");
    expect(profile.city).toBe("");
    expect(profile.addressStreet).toBe("");
  });

  test("flattens FHIR name, address, and telecom into scalar fields", async () => {
    withProfile({
      id: "u1",
      gender: "female",
      birthDate: "1990-01-01",
      language: "en",
      name: [{ given: ["Ada", "B"], family: "Lovelace" }],
      address: [{ line: ["1 Main St"], city: "Metropolis", state: "NY", postalCode: "10001" }],
      telecom: [
        { system: "phone", use: "home", value: "111" },
        { system: "phone", use: "mobile", value: "222" },
        { system: "phone", use: "work", value: "333" },
        { system: "email", value: "ada@example.com" },
      ],
    });

    const profile = await getUserProfile();

    expect(profile.firstName).toBe("Ada");
    expect(profile.lastName).toBe("Lovelace");
    expect(profile.addressStreet).toBe("1 Main St");
    expect(profile.city).toBe("Metropolis");
    expect(profile.state).toBe("NY");
    expect(profile.postalCode).toBe("10001");
    expect(profile.homePhone).toBe("111");
    expect(profile.mobilePhone).toBe("222");
    expect(profile.workPhone).toBe("333");
    expect(profile.email).toBe("ada@example.com");
    expect(profile.id).toBe("u1");
    expect(profile.gender).toBe("female");
  });

  test("removes the nested FHIR parent fields it flattened", async () => {
    withProfile({
      name: [{ given: ["Grace"], family: "Hopper" }],
      address: [{ line: ["x"], city: "c" }],
      telecom: [{ system: "email", value: "g@example.com" }],
    });

    const profile = await getUserProfile() as Record<string, unknown>;

    expect(profile.name).toBeUndefined();
    expect(profile.address).toBeUndefined();
    expect(profile.telecom).toBeUndefined();
  });

  test("selects the correct telecom entry by system and use", async () => {
    withProfile({
      telecom: [
        { system: "phone", use: "mobile", value: "MOBILE" },
        { system: "phone", use: "home", value: "HOME" },
        { system: "email", value: "E@example.com" },
      ],
    });

    const profile = await getUserProfile();

    expect(profile.mobilePhone).toBe("MOBILE");
    expect(profile.homePhone).toBe("HOME");
    expect(profile.workPhone).toBe("");
    expect(profile.email).toBe("E@example.com");
  });

  // Parameter sensitivity: a different profile input yields the correspondingly different output.
  test("output reflects the specific profile returned by the SDK", async () => {
    withProfile({ name: [{ given: ["Grace"], family: "Hopper" }] });

    const profile = await getUserProfile();

    expect(profile.firstName).toBe("Grace");
    expect(profile.lastName).toBe("Hopper");
    expect(profile.email).toBe("");
  });

  test("falls back to empty strings when name/address/telecom are absent", async () => {
    withProfile({ id: "only-id" });

    const profile = await getUserProfile();

    expect(profile.id).toBe("only-id");
    expect(profile.firstName).toBe("");
    expect(profile.addressStreet).toBe("");
    expect(profile.homePhone).toBe("");
  });
});
