import { describe, expect, test } from "vitest";
import { apiHostForKey, decodeEnv } from "@/sdk/clientKeyEnv";

const keyFor = (payload: unknown) => btoa(JSON.stringify(payload));

describe("apiHostForKey", () => {
  test.each([
    ["a prod key", keyFor({ env: "prod" }), "https://api.prod.icanbwell.com"],
    ["a client-sandbox key", keyFor({ env: "client-sandbox" }), "https://api.client-sandbox.icanbwell.com"],
    ["a key without an env", keyFor({ other: 1 }), "https://api.client-sandbox.icanbwell.com"],
    ["an old style key", "not-base64-json", "https://api.client-sandbox.icanbwell.com"],
    ["an env that isn't a hostname label", keyFor({ env: "evil.com/x" }), "https://api.client-sandbox.icanbwell.com"],
    ["no key", undefined, "https://api.client-sandbox.icanbwell.com"],
  ])("uses the right host for %s", (_label, key, expected) => {
    expect(apiHostForKey(key)).toBe(expected);
  });

  test("decodeEnv reads the env from the key", () => {
    expect(decodeEnv(keyFor({ env: "prod" }))).toBe("prod");
  });
});
